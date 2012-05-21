package de.dailab.gridworld.bean;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import de.dailab.gridworld.ontology.Direction;
import de.dailab.gridworld.ontology.Group;
import de.dailab.gridworld.ontology.Inform;
import de.dailab.gridworld.ontology.Position;
import de.dailab.gridworld.ontology.Request;
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
	private IActionDescription send;

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
	private HashMap<Point, Double> taken;
	private JiacMessage transport;
	
	// inform-msg template
	private final static JiacMessage INF = new JiacMessage(new Inform<Object>());
	// info observer
	private final SpaceObserver<IFact> infObserver = new InformObserver();
	// inform-msg template
	private final static JiacMessage REQ = new JiacMessage(new Request<Object>());
	// info observer
	private final SpaceObserver<IFact> reqObserver = new RequestObserver();

	@Override
	public void doInit() throws Exception {
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
		this.send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		this.memory.attach(infObserver, INF);
		this.memory.attach(reqObserver, REQ);
	}

	@Override
	public void execute() {
		log.debug("같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같");
		current = null;
		next = null;
		nextDestination = null;
		possibleMoves = null;
		taken = new HashMap<Point, Double>();

		// reading current position from memory
		current = memory.read(ownPositionTemplate);

		log.debug("Hat gold?: "+ gold);
		logList(possibleDestinations, "goldpunkte: ");
		
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
					transport = new JiacMessage(new Request<Point>(home, thisAgent.getAgentDescription(), 10));
					this.invoke(send,
							new Serializable[] { transport, this.groupAddress });
				} else {
					move(cPoint, home);
				}
			} else {
				// if there's gold on the current field, take it
				if (current.gold) {
					invoke(takeGoldAction, new Serializable[] { current });
					possibleDestinations.remove(cPoint);
					gold = true;
					transport = new JiacMessage(new Request<Point>(cPoint, thisAgent.getAgentDescription(), 10));
					this.invoke(send,
							new Serializable[] { transport, this.groupAddress });
				} else {
					// get the closest field with gold
					nextDestination = smallestDistance(possibleDestinations,
							cPoint);
					log.debug("nextDestination: " + nextDestination);

					if (nextDestination != null) {
						move(cPoint, nextDestination);
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
	 * Moves one field from a given point to a give destination.
	 * Receives Positions other agents want to move to ant tries to avoid them.
	 * @param currentPoint
	 * @param destination
	 */
	private void move(Point currentPoint, Point destination) {
		double priority = Math.random() * 10;
		
		// get possible moves
		possibleMoves = getPossibleMoves(currentPoint);
		
		// get the best next move not taken from anyone
		next = smallestDistance(possibleMoves, destination);
		transport = new JiacMessage(new Request<Point>(next, thisAgent.getAgentDescription(), priority));
		this.invoke(send, new Serializable[] { transport, this.groupAddress });
		
		synchronized (taken) {
			while(taken.size() < 1 ) {
				try {
					taken.wait();
					while (taken.containsKey(next)) {
						if(taken.get(next) > priority) {
							priority = Math.random() * 10;
							possibleMoves.remove(next);
							next = smallestDistance(possibleMoves, destination);
							transport = new JiacMessage(new Request<Point>(next, thisAgent.getAgentDescription(), priority));
							this.invoke(send,
									new Serializable[] { transport, this.groupAddress });
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// move
		if(!next.equals(currentPoint)) {
			invoke(moveAction, new Serializable[] { current,
							Direction.getDirection(next.x - currentPoint.x,
									next.y - currentPoint.y) }, this);
		}
		log.debug("next: " + next);
		log.debug("Direction: " + Direction.getDirection(next.x, next.y));
	}
	
	@Override
	public void doStop() throws Exception {
		Action leave = this
				.retrieveAction(ICommunicationBean.ACTION_LEAVE_GROUP);
		ActionResult result = this.invokeAndWaitForResult(leave,
				new Serializable[] { groupAddress });
		if (result.getFailure() != null) {
			this.log.error("could not leave temp-group: " + result.getFailure());
		}
	}

	/**
	 * debug helper
	 * @param set
	 * @param s
	 */
	private void logList(Set<Point> set, String s) {
		for (Point p : set) {
			log.debug(s + p.x + " " + p.y);
		}
		if (set.isEmpty()) {
			log.debug(s + "is empty");
		}
	}

	/**
	 * looks for information about about gold fields
	 * 
	 * @author Mitch
	 * 
	 */
	final class InformObserver implements SpaceObserver<IFact>  {

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
					/* if you get an information set of points it's from
					 * explorerAgent - add it to your possible destinations
					 */
					if (i.getValue() instanceof HashSet<?>) {
						@SuppressWarnings("unchecked")
						HashSet<Point> dest = (HashSet<Point>) i.getValue();
						possibleDestinations.addAll(dest);
					/* if you get a information about a single point, it's from
					 * another agent who picked up gold - so remove this field
					 * of your possible destinations
					 */
					} else if (i.getValue() instanceof Point) {
						Point p = (Point) i.getValue();
						possibleDestinations.remove(p);
					} 
				}
			}
		}
	}
	
	/**
	 * looks for information about about gold fields
	 * 
	 * @author Mitch
	 * 
	 */
	final class RequestObserver implements SpaceObserver<IFact>  {

		static final long serialVersionUID = -1143799774862165996L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					@SuppressWarnings("unchecked")
					Request<Object> i = (Request<Object>) ((JiacMessage) object)
							.getPayload();
					if (i.getValue() instanceof Point) {
						if (!i.getAgent().equals(thisAgent.getAgentDescription())) {
							Point p = (Point) i.getValue();
							synchronized (taken) {
								taken.put(p, i.getPriority());
								taken.notify();
							}
							log.debug("added " + p + " to taken");
						}
					} 
				}
			}
		}
	}
}
