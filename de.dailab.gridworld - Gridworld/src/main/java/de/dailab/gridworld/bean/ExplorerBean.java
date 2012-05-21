package de.dailab.gridworld.bean;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import de.dailab.gridworld.ontology.Direction;
import de.dailab.gridworld.ontology.Group;
import de.dailab.gridworld.ontology.Inform;
import de.dailab.gridworld.ontology.Position;
import de.dailab.gridworld.ontology.Role;
import de.dailab.gridworld.world.GridworldAgentBean;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * The explorer explores the gridworld and sends findings of gold to the collector.
 * The explorer moves to the next unexplored Field.
 * 
 * 
 * @author Mitch
 *
 */
public class ExplorerBean extends AbstractAgentBean implements ResultReceiver {

	// used actions
	private IActionDescription initAction;
	private IActionDescription exploreAction;
	private IActionDescription moveAction;
	private IActionDescription send;

	// group variables
	public static final Group group = Group.G2;
	public static final String GROUP_ADDRESS = "G2";
	private IGroupAddress groupAddress = null;

	// states
	private Position ownPositionTemplate;
	private Position current;
	private HashSet<Point> visited = new HashSet<Point>();
	private HashSet<Point> possibleDestinations = new HashSet<Point>();
	private HashSet<Point> possibleMoves = new HashSet<Point>();
	private final Role role = Role.EXPLORER;
	public Point next;
	public Point nextDestination;

	@Override
	public void doInit() throws Exception {
		// Bereitstellung eines Templates fuer die Position des eigenen Agenten
		this.ownPositionTemplate = new Position(null, null, null,
				thisAgent.getAgentDescription());
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
	}

	/**
	 * join group, get actions
	 */
	@Override
	public void doStart() throws Exception {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		this.send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		this.initAction = thisAgent.searchAction(new Action(
				GridworldAgentBean.ACTION_INIT_TEAM_POSITION));
		this.exploreAction = thisAgent.searchAction(new Action(
				GridworldAgentBean.ACTION_EXPLORE));
		this.moveAction = thisAgent.searchAction(new Action(
				GridworldAgentBean.ACTION_MOVE));
	}

	@Override
	public void execute() {
		log.debug("같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같");
		current = null;
		next = null;
		nextDestination = null;
		possibleMoves = null;
		
		// reading current position from memory
		current = memory.read(ownPositionTemplate);

		if (current == null) {
			this.invoke(initAction, new Serializable[] { group, role }, this);
		}
		if (current != null) {
			
			// die Umgebung mittels "explore" erforschen
			this.invoke(exploreAction, new Serializable[] { current }, this);
			
			// get current position
			Point cPoint = new Point(Integer.valueOf(current.x),Integer.valueOf(current.y));
			visited.add(cPoint);
			log.debug("current= " + cPoint.toString() + " gold? " + current.gold);
			
			// get all unexplored fields
			Set<Position> neighbourhoodPositions = memory.readAllOfType(Position.class);
			addDisjointPositions(neighbourhoodPositions, visited, possibleDestinations);
			logAllLists();

			// get the closest unexplored field
			nextDestination = smallestDistance(possibleDestinations, cPoint);
			log.debug("nextDestination: " + nextDestination);
			
			// get possible moves
			possibleMoves = getPossibleMoves(neighbourhoodPositions);
			
			// get the next best move 
			if (nextDestination != null) {
				next = smallestDistance(possibleMoves, nextDestination);
				this.invoke(moveAction, new Serializable[] { current, Direction.getDirection(next.x-cPoint.x, next.y-cPoint.y) }, this);
				if(nextDestination.equals(next)) {
					possibleDestinations.remove(next);
				}
			log.debug("next: " + next);
			log.debug("Direction: " +Direction.getDirection(next.x, next.y));
			}
		}
	}

	/**
	 * ReceiveResult wird aufgerufen, wenn ein Action-Aufruf ein Resultat
	 * zurueckliefert.
	 * 
	 * @param result
	 *            action invocation result
	 */
	@Override
	public void receiveResult(ActionResult result) {
		log.debug("Action result received: " + result.getAction().getName());

		if (GridworldAgentBean.ACTION_INIT_TEAM_POSITION.equals(result.getAction()
				.getName())) {
			if (result.getFailure() == null) {
				Position myPosition = (Position) result.getResults()[0];
				log.debug(myPosition.toString());
				memory.write(myPosition);
			}
		} else if (GridworldAgentBean.ACTION_EXPLORE.equals(result.getAction()
				.getName())) {
			if (result.getFailure() == null) {
				@SuppressWarnings("unchecked")
				Set<Position> neighbourhood = (Set<Position>) result
						.getResults()[0];
				sendPoints(neighbourhood);
				for (Position pos : neighbourhood) {
					log.debug("Explored position: " + pos.toString());
					Position template = new Position(pos.x, pos.y);
					if (memory.read(template) != null) {
						memory.update(template, pos);
					} else {
						memory.write(pos);
					}
				}
			}
		} else if (GridworldAgentBean.ACTION_MOVE.equals(result.getAction()
				.getName())) {
			// move(..)
			if (result.getFailure() == null) {
				Position newPosition = (Position) result.getResults()[0];
				Position template = new Position(newPosition.x, newPosition.y);
				memory.removeAll(new Position(null, null, null, thisAgent
						.getAgentDescription()));
				if (memory.read(template) != null) {
					memory.update(template, newPosition);
				} else {
					memory.write(newPosition);
				}
			}
		}
		if (result.getFailure() != null) {
			log.warn("Action Failure: " + result.getFailure().toString());
		}
	}

	/**
	 * helper for debugging all point-lists
	 */
	private void logAllLists() {
		logList(this.visited, "visited: ");
		logList(this.possibleDestinations, "possible: ");
	}
	
	private void logList(Set<Point> set, String s) {
		for (Point p : set) {
			log.debug(s + p.x + " " + p.y);
		}
	}
	
	/**
	 * Adds all positions from one set, which don't occure in another set, to a third set.
	 * The disjoint set of originSet and compareSet will be added to destinationSet
	 * @param originSet 
	 * @param compareSet
	 * @param destinationSet
	 */
	private void addDisjointPositions(Set<Position> originSet, Set<Point> compareSet, Set<Point> destinationSet) {
		for (Position pos : originSet) {
			boolean flag = false;
			Point o = new Point(Integer.valueOf(pos.x), Integer.valueOf(pos.y));
			for (Point p : compareSet) {
				if (o.x == p.x && o.y == p.y) {
					flag = true;
				}
			}
			if (!flag) {
				destinationSet.add(new Point(o.x,o.y));
			}
		}
	}
	
	/**
	 * returns the one point out of a set, which position is closest to another give point
	 * @param destinations a set of points to choose from
	 * @param currentPoint the point to which position all others will be compared
	 * @return
	 */
	private Point smallestDistance(Set<Point> destinations, Point currentPoint) {
		double dist = Integer.MAX_VALUE;
		Point closest = null;
		for (Point p : destinations) {
			double nextdist = (Math.abs(p.x - currentPoint.x))
					+ (Math.abs(currentPoint.y - p.y));
			if (nextdist < dist) {
				closest = p;
				dist = nextdist;
			}
		}
		return closest;
	}
	
	/**
	 * returns a set of points, which are in the direct surroundigs of the current position
	 * @param currentPoint
	 * @return
	 */
	private HashSet<Point> getPossibleMoves(Set<Position> neighbourhood) {
		HashSet<Point> surroundings = new HashSet<Point>();
		for (Position p : neighbourhood) {
			surroundings.add(new Point(Integer.valueOf(p.x), Integer.valueOf(p.y)));
		}
		return surroundings;
	}
	
	/**
	 * sends all goldpositions of a given set to everyone
	 * @param set
	 */
	private void sendPoints(Set<Position> set) {
		HashSet<Point> goldPoints = new HashSet<Point>();
		for (Position p : set) {
			if ( p.gold ) {
				goldPoints.add(new Point(Integer.valueOf(p.x), Integer.valueOf(p.y)));
			}
		}
		JiacMessage transport = new JiacMessage(new Inform<HashSet<Point>>(goldPoints, thisAgent.getAgentDescription()));
		this.invoke(send,
				new Serializable[] { transport, this.groupAddress });
	}
}
