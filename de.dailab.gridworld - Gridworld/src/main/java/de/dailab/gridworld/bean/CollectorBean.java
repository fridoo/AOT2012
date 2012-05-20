package de.dailab.gridworld.bean;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
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
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * The collector gets goldfield-positions from explorer bean, collects the gold
 * in order to bring it to the home-field.
 * 
 * 
 * @author Mitch
 *
 */
public class CollectorBean extends AbstractAgentBean implements ResultReceiver {

	// used actions
	private IActionDescription initAction;
	private IActionDescription moveAction;
	private IActionDescription takeGoldAction;
	private IActionDescription dropGoldAction;

	// group variables
	public static final Group group = Group.G2;
	public static final String GROUP_ADDRESS = "G2";
	private IGroupAddress groupAddress = null;

	// states
	private Position ownPositionTemplate;
	private Position current;
	private HashSet<Point> possibleDestinations = new HashSet<Point>();
	private HashSet<Point> possibleMoves = new HashSet<Point>();
	private final Role role = Role.COLLECTOR;
	public Point next;
	public Point nextDestination;
	public boolean gold = false;
	private final Point home = new Point(0, 0);

	// inform-msg template
	private final static JiacMessage INF = new JiacMessage(new Inform<Object>());
	// message observer
	private final SpaceObserver<IFact> observer = new InformObserver();

	@Override
	public void doInit() throws Exception {
		// Bereitstellung eines Templates fuer die Position des eigenen Agenten
		this.ownPositionTemplate = new Position(null, null, null,
				thisAgent.getAgentDescription());
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
	}

	/**
	 * join group, get actions, attach observer
	 */
	@Override
	public void doStart() throws Exception {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		this.initAction = thisAgent.searchAction(new Action(
				GridworldAgentBean.ACTION_INIT_TEAM_POSITION));
		this.takeGoldAction = thisAgent.searchAction(new Action(
				GridworldAgentBean.ACTION_TAKE_GOLD));
		this.dropGoldAction = thisAgent.searchAction(new Action(
				GridworldAgentBean.ACTION_DROP_GOLD));
		this.moveAction = thisAgent.searchAction(new Action(
				GridworldAgentBean.ACTION_MOVE));
		
		this.memory.attach(observer, INF);
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

		log.debug("Hat gold?: "+ gold);
		if(possibleDestinations != null) {
			for ( Point p : possibleDestinations ) {
				log.debug("goldpunkte: " + p);
			}
		}
		
		if (current == null) {
			this.invoke(initAction, new Serializable[] { group, role }, this);
		}
		if (current != null) {

			// get current position
			Point cPoint = new Point(Integer.valueOf(current.x),
					Integer.valueOf(current.y));
			log.debug("current= " + cPoint.toString() + " gold? "
					+ current.gold);

			// if he carries gold move to Home position and drop it there
			if (gold) {
				if (home.equals(cPoint)) {
					invoke(dropGoldAction, new Serializable[] { current });
					this.gold = false;
				} else {
					HashSet<Point> possibleMoves = getPossibleMoves(cPoint);
					next = smallestDistance(possibleMoves, this.home);
					this.invoke(moveAction, new Serializable[] { current,
									Direction.getDirection(next.x - cPoint.x,
											next.y - cPoint.y) }, this);
				}
			} else {
				// if there's gold on the current field, take it
				if (current.gold) {
					invoke(takeGoldAction, new Serializable[] { current });
					possibleDestinations.remove(cPoint);
					gold = true;
				} else {
					// get the closest field with gold
					nextDestination = smallestDistance(possibleDestinations,
							cPoint);
					log.debug("nextDestination: " + nextDestination);

					// get possible moves
					possibleMoves = getPossibleMoves(cPoint);

					// get the best next move
					if (nextDestination != null) {
						next = smallestDistance(possibleMoves, nextDestination);
						this.invoke(
								moveAction,
								new Serializable[] {
										current,
										Direction.getDirection(next.x
												- cPoint.x, next.y - cPoint.y) },
								this);
						if (nextDestination.equals(next)) {
							possibleDestinations.remove(next);
						}
					log.debug("next: " + next);
					log.debug("Direction: " + Direction.getDirection(next.x, next.y));
					}
				}
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

		if (GridworldAgentBean.ACTION_INIT_TEAM_POSITION.equals(result
				.getAction().getName())) {
			if (result.getFailure() == null) {
				Position myPosition = (Position) result.getResults()[0];
				log.debug(myPosition.toString());
				memory.write(myPosition);
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
	 * returns the one point out of a set, which position is closest to another
	 * give point
	 * 
	 * @param destinations
	 *            a set of points to choose from
	 * @param currentPoint
	 *            the point to which position all others will be compared
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
	 * returns a set of points, which are in the direct surroundigs of a given
	 * point
	 * 
	 * @param currentPoint
	 * @return
	 */
	private HashSet<Point> getPossibleMoves(Point currentPoint) {
		HashSet<Point> surroundings = new HashSet<Point>();
		for (int i = -1; i < 2; ++i) {
			for (int j = -1; j < 2; ++j) {
				surroundings.add(new Point(currentPoint.x + i, currentPoint.y
						+ j));
			}
		}
		return surroundings;
	}

	/**
	 * looks for information about about gold fields
	 * 
	 * @author Mitch
	 * 
	 */
	final class InformObserver implements SpaceObserver<IFact> {

		static final long serialVersionUID = -1143799774862165996L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					@SuppressWarnings("unchecked")
					Inform<Object> i = (Inform<Object>) ((JiacMessage) object)
							.getPayload();
					if (i.getValue() instanceof HashSet<?>) {
						@SuppressWarnings("unchecked")
						HashSet<Point> dest = (HashSet<Point>) i.getValue();
						possibleDestinations.addAll(dest);
					}
				}
			}
		}
	}
}
