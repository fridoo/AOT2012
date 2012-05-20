package de.dailab.gridworld.world;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.dailab.gridworld.gui.GridworldFrame;
import de.dailab.gridworld.ontology.Direction;
import de.dailab.gridworld.ontology.Field;
import de.dailab.gridworld.ontology.GridworldException;
import de.dailab.gridworld.ontology.Group;
import de.dailab.gridworld.ontology.Position;
import de.dailab.gridworld.ontology.Role;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class GridworldAgentBean extends AbstractAgentBean implements IEffector {

	/**
	 * action parameter:
	 * <ol>
	 * <li>{@link Position} - the current position of the agent</li>
	 * <li>{@link Direction} - the direction to go</li>
	 * </ol>
	 * action results:
	 * <ol>
	 * <li>{@link Position} - the new position after a movement was done</li>
	 * </ol>
	 * action failures:
	 * <ul>
	 * <li>{@link GridworldException} - on any exception while moving, e.g. end
	 * of the grid world or the position is occupied by another agent</li>
	 * <li>{@link Exception} - on any other failures</li>
	 * </ul>
	 */
	public static final String ACTION_MOVE = "Gridworld.Move";

	/**
	 * action parameter:
	 * <ol>
	 * <li>{@link Position} - the position in the grid where to take gold</li>
	 * </ol>
	 * action results: <i>nothing - even no failure</i><br>
	 * action failures:
	 * <ol>
	 * <li>{@link GridworldException} - on any exception while taking gold, e.g.
	 * there is no gold at this {@link Position} or the agent that takes the
	 * gold, is not at this {@link Position}</li>
	 * </ol>
	 */
	public static final String ACTION_TAKE_GOLD = "Gridworld.TakeGold";

	/**
	 * action parameter:
	 * <ol>
	 * <li>{@link Position} - the position in the grid where to take gold</li>
	 * </ol>
	 * action results: <i>nothing - even no failure</i><br>
	 * action failures:
	 * <ol>
	 * <li>{@link GridworldException} - on any exception while drop a gold item,
	 * e.g. the {@link Position} is out of bounds of the grid world.</li>
	 * </ol>
	 */
	public static final String ACTION_DROP_GOLD = "Gridworld.DropGold";

	/**
	 * action parameter:
	 * <ol>
	 * <li>{@link Position} - the position to explore</li>
	 * </ol>
	 * action results:
	 * <ol>
	 * <li>{@link Set}<{@link Position}> - plumbable {@link Position}s around a
	 * certain {@link Position}</li>
	 * </ol>
	 * action failures:
	 * <ol>
	 * <li>{@link GridworldException} - on any exception while exploring
	 * position, e.g. the parameter {@link Position} is out of bounds of the grid
	 * world</li>
	 * </ol>
	 */
	public static final String ACTION_EXPLORE = "Gridworld.Explore";

	/**
	 * action parameter: <i>no parameter</i><br>
	 * action results:
	 * <ol>
	 * <li>{@link Position} - the initial {@link Position} of an agent within the
	 * grind world.</li>
	 * </ol>
	 * action failures: <i>no failures<br>
	 */
	public static final String ACTION_INIT_POSITION = "Gridworld.GetInitialPosition";

	/**
	 * action parameter:
	 * <ol>
	 * <li>{@link Group} - the group identifier</li>
	 * <li>{@link Role} - the role of the agent within the Gridworld</li>
	 * </ol>
	 * action results:
	 * <ol>
	 * <li>{@link Position} - the initial {@link Position} of an agent within the
	 * grind world.</li>
	 * </ol>
	 * action failures: <i>no failures<br>
	 */
	public static final String ACTION_INIT_TEAM_POSITION = "Gridworld.GetInitialTeamPosition";

	private final Random random = new Random(System.identityHashCode(this));

	/*
	 * fields for Gridworld-Match
	 */
	// TODO create setter for Spring
	private int allowedMoveInterval = 500;
	/**
	 * This field enables checks for move interval.
	 * 
	 * @see #move(DoAction)
	 */
	private Map<String, Long> agentsLastMove = new HashMap<String, Long>();
	/**
	 * This field enables team drop count by associate agent IDs to tutorial
	 * group.
	 * 
	 * @see #dropGold(DoAction)
	 */
	private final Map<String, Group> teams = new HashMap<String, Group>();
	/**
	 * This field enables group associated drop count.
	 * 
	 * @see #dropGold(DoAction)
	 */
	private final Map<Group, Integer> count = new HashMap<Group, Integer>();

	private GridworldFrame frame;
	private int worldXmax = 0;
	private int worldYmax = 0;
	private int gridWidth = 0;
	private int goldRatio = 0;

	@Override
	public void doInit() {
		/*
		 * initialize the grid world with the dimensions (worldXmax, worldYmax)
		 */
		final Field field = new Field(this.worldXmax, this.worldYmax, this.goldRatio);
		this.memory.write(field);
		for (final Position pos : field.getAllCells()) {
			this.memory.write(pos);
		}
	}

	@Override
	public void doStart() {
		this.frame = new GridworldFrame(this.worldXmax, this.worldYmax, this.gridWidth);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosed(e);
				IAgentNode node = null;
				if ((node = GridworldAgentBean.this.thisAgent.getAgentNode()) instanceof SimpleAgentNode) {
					try {
						((SimpleAgentNode) node).shutdown();
					}
					catch (LifecycleException lex) {
						System.err.println("could not shut down this agent node: " + lex.getMessage());
					}
				}
			}
		});
		this.frame.setVisible(true);
		this.refreshFrame();
	}

	@Override
	public void doStop() {
		if (this.frame != null) {
			this.frame.setVisible(false);
			this.frame.dispose();
			this.frame = null;
		}
	}

	private void refreshFrame() {
		if (this.frame != null) {
			final Field field = this.memory.read(new Field());
			if (field != null) {
				this.frame.update(field);
			}
			else {
				System.err.println("could not find field");
			}
		}
	}
	
	private void refreshCounter() {
		if (this.frame != null) {
			this.frame.updateCounter(count);
		}
	}

	private void initTeamLocation(final DoAction action) {
		/*
		 * copied code from #initLocation(DoAction)
		 */
		/*
		 * create a template used for reading from agent's memory
		 */
		final Position tpl = new Position();
		tpl.agent = action.getSession().getOriginalAgentDescription();
		Position position = memory.read(tpl);

		if (log.isDebugEnabled()) {
			log.debug("agent (" + tpl.agent.getAid() + ") position memory = " + position);
		}

		/*
		 * if a position was found where the agent is located, return this
		 * location otherwise search for free, collision free position AND assign
		 * the agent to this position
		 */
		if (position == null) {
			do {
				final int x = this.random.nextInt(this.worldXmax);
				final int y = this.random.nextInt(this.worldYmax);
				/*
				 * read a random location (x,y) in the grid world
				 */
				position = this.memory.read(new Position(x, y));
			}
			/*
			 * do the loop, until a free location was found
			 */
			while (!position.gold && position.agent != null);
			/*
			 * mark the location as occupied
			 */
			position.agent = action.getSession().getOriginalAgentDescription();
			/*
			 * additional code for team descriptions
			 */
			try {
				Group group = (Group) action.getParams()[0];
				Role role = (Role) action.getParams()[1];
				/*
				 * update team information for count
				 */
				this.teams.put(position.agent.getAid(), group);
				/*
				 * update team information for GUI
				 */
				frame.updateTeam(position.agent.getAid(), group);
				frame.updateRole(position.agent.getAid(), role);
				
				
			}
			catch (Exception e) {
				log.error("could not update gridworld group or role: " + e.getMessage());
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("assinged agent position = " + position);
		}
		/*
		 * report the location of marked location
		 */
		this.returnResult(action, new Serializable[] { position });
	}

	private void initLocation(final DoAction action) {

		/*
		 * create a template used for reading from agent's memory
		 */
		final Position tpl = new Position();
		tpl.agent = action.getSession().getOriginalAgentDescription();
		Position position = memory.read(tpl);

		if (log.isDebugEnabled()) {
			log.debug("agent (" + tpl.agent.getAid() + ") position from memory = " + position);
		}

		/*
		 * if a position was found where the agent is located, return this
		 * location otherwise search for free, collision free position AND assign
		 * the agent to this position
		 */
		if (position == null) {
			do {
				final int x = this.random.nextInt(this.worldXmax);
				final int y = this.random.nextInt(this.worldYmax);
				/*
				 * read a random location (x,y) in the grid world
				 */
				position = this.memory.read(new Position(x, y));
			}
			/*
			 * do the loop, until a free location was found
			 */
			while (!position.gold && position.agent != null);
			/*
			 * mark the location as occupied
			 */
			position.agent = action.getSession().getOriginalAgentDescription();
		}

		if (log.isDebugEnabled()) {
			log.debug("assinged agent position = " + position);
		}
		/*
		 * report the location of marked location
		 */
		this.returnResult(action, new Serializable[] { position });
	}

	private boolean exists(final Position p) {
		return (p.x >= 0 && p.x < this.worldXmax && p.y >= 0 && p.y < this.worldYmax);
	}

	private boolean exists(final Position p, final Direction d) {
		final Position changed = new Position(p.x + d.dx, p.y + d.dy);
		return this.exists(changed);
	}

	private Position read(final Position p, final Direction d) {
		final Position changed = new Position(p.x + d.dx, p.y + d.dy);
		return this.memory.read(changed);
	}

	private void explore(final DoAction action) {

		try {
			final Position position = (Position) action.getParams()[0];

			final Position tpl = new Position(position.x, position.y, null, null);

			final Position gridPosition = this.memory.read(tpl);

			if (gridPosition == null) {
				this.returnFailure(action, new GridworldException("the position was not found in the grid: " + position,
						position));
				return;
			}

			final HashSet<Position> result = new HashSet<Position>();
			for (Direction d : Direction.values()) {
				if (this.exists(gridPosition, d)) {
					Position p = this.read(gridPosition, d);
					if (p == null) {
						log.warn("Position read from memory is null; original position: " + position + ", direction: " + d);
					}
					result.add(p);
				}
			}

			this.returnResult(action, new Serializable[] { result });
		}
		catch (final Exception e) {
			this.returnFailure(action, e);
		}

	}

	private void move(final DoAction action) {
		try {
			final Position position = (Position) action.getParams()[0];
			final Direction direction = (Direction) action.getParams()[1];

			IAgentDescription movingAgent = action.getSession().getOriginalAgentDescription();

			/*
			 * check for illegal move interval
			 */
			Long last = agentsLastMove.get(movingAgent.getAid());
			if (last == null) {
				agentsLastMove.put(movingAgent.getAid(), System.currentTimeMillis());
			}
			else {
				if ((last + allowedMoveInterval) > System.currentTimeMillis()) {
					this.returnFailure(action, new GridworldException("Illegal move; move interval is "
							+ allowedMoveInterval + " ms", position));
					return;
				}
				else {
					agentsLastMove.put(movingAgent.getAid(), System.currentTimeMillis());
				}
			}

			if (this.exists(position, direction)) {

				final Position move = this.memory.read(new Position(position.x + direction.dx, position.y + direction.dy));
				final Position clear = this.memory.read(new Position(position.x, position.y));
				/*
				 * check for collisions
				 */
				if (move.agent != null) {
					this.returnFailure(action, new GridworldException("this position is already in use.", move));
					return;
				}
				/*
				 * check if the agent actually was at the position before
				 */
				if (!movingAgent.equals(clear.agent)) {
					this.returnFailure(action, new GridworldException("wrong start position", position));
					return;
				}
				else {
					/*
					 * update location of the moving agent
					 */
					move.agent = action.getSession().getOriginalAgentDescription();
					/*
					 * clear old position of the requesting agent
					 */
					
					clear.agent = null;
					/*
					 * return the current location of the agent
					 */
					this.returnResult(action, new Serializable[] { move });
				}
			}
			else {
				final GridworldException e = new GridworldException("the new position is out of bounds of the grid world: "
						+ new Position(position.x + direction.dx, position.y + direction.dy), new Position(position.x
						+ direction.dx, position.y + direction.dy));
				this.returnFailure(action, e);
			}
		}
		catch (final Exception e) {
			this.returnFailure(action, e);
		}
	}

	private void takeGold(final DoAction origin) {

		try {
			final Position position = (Position) origin.getParams()[0];
			if (this.exists(position)) {
				final Position read = this.memory.read(new Position(position.x, position.y));
				if (read.gold) {
					read.gold = Boolean.FALSE;
					this.returnResult(origin, new Serializable[] {});
				}
				else {
					this.returnFailure(origin,
							new GridworldException("location in grid does not contain any gold", position));
				}
			}
			else {
				final GridworldException e = new GridworldException("location in the grid is out of bounds", position);
				this.returnFailure(origin, e);
			}
		}
		catch (final Exception e) {
			this.returnFailure(origin, e);
		}
	}

	private void dropGold(final DoAction origin) {

		try {
			final Position position = (Position) origin.getParams()[0];

			if (this.exists(position)) {
				final Position read = this.memory.read(new Position(position.x, position.y));
				if (read.x == 0 && read.y == 0) {
					/*
					 * this is a drop at HOME location
					 */
					IAgentDescription agent = origin.getSession().getOriginalAgentDescription();
					if (agent != null) {
						Group g = this.teams.get(agent.getAid());
						Integer icount = this.count.get(g);
						if (icount == null) {
							this.count.put(g, new Integer(1));
						}
						else {
							/*
							 * auto-boxing is bull shit, object increment is overhead
							 * :-/
							 */
							++icount;

							this.count.put(g, icount);
							/*
							 * print current team stats
							 */
							log.info(" ### TEAM STATS: " + String.valueOf(count));
						}
						refreshCounter();
					}

					this.returnResult(origin, new Serializable[] {});
				}
				else if (read.gold) {
					this.returnFailure(origin, new GridworldException("there is already a gold, cannot drop", position));
				}
				else {
					// read.gold = true;
					this.returnResult(origin, new Serializable[] {});

				}
			}
			else {
				final GridworldException e = new GridworldException("location in the grid is out of bounds", position);
				this.returnFailure(origin, e);
			}
		}
		catch (final Exception e) {
			this.returnFailure(origin, e);
		}
	}
	
	
//	@Override
//	public void execute() {
//		super.execute();
//		/*
//		 * clean up old agent's
//		 */
//		Field field = this.memory.read(new Field());
//		for(Position position : field.getAllCells() ) {
//			if (position.agent != null) {
//				IAgentDescription tpl = new AgentDescription(position.agent.getAid(), null, null, null, null, null);
//				IAgentDescription found = this.thisAgent.searchAgent(tpl) ;
//				if (found == null) {
//					log.info("removing agent " + position.agent.getAid());
//					position.agent = null;
//				}
//				else {
//					log.info("agent " + found.getAid() + " still exists");
//				}
//			}
//		}
//		frame.update(field);
//	}

	@Override
	public void doAction(final DoAction doAction) throws Exception {

		final String aname = doAction.getAction().getName();

		if (GridworldAgentBean.ACTION_MOVE.equals(aname)) {
			this.move(doAction);
		}
		else if (GridworldAgentBean.ACTION_INIT_POSITION.equals(aname)) {
			this.initLocation(doAction);
		}
		else if (GridworldAgentBean.ACTION_EXPLORE.equals(aname)) {
			this.explore(doAction);
		}
		else if (GridworldAgentBean.ACTION_TAKE_GOLD.equals(aname)) {
			this.takeGold(doAction);
		}
		else if (GridworldAgentBean.ACTION_DROP_GOLD.equals(aname)) {
			this.dropGold(doAction);
		}
		else if (GridworldAgentBean.ACTION_INIT_TEAM_POSITION.equals(aname)) {
			this.initTeamLocation(doAction);
		}

		this.refreshFrame();
	}

	@Override
	public List<? extends IActionDescription> getActions() {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new Action(ACTION_INIT_POSITION, this, new Class[] {}, new Class[] { Position.class }));
		actions.add(new Action(ACTION_EXPLORE, this, new Class[] { Position.class }, new Class[] { Collection.class }));
		actions.add(new Action(ACTION_MOVE, this, new Class[] { Position.class, Direction.class },
				new Class[] { Position.class }));
		actions.add(new Action(ACTION_TAKE_GOLD, this, new Class[] { Position.class }, new Class[] {}));
		actions.add(new Action(ACTION_DROP_GOLD, this, new Class[] { Position.class }, new Class[] {}));
		actions.add(new Action(ACTION_INIT_TEAM_POSITION, this, new Class[] { Group.class, Role.class },
				new Class[] { Position.class }));

		for (Action a : actions) {
			a.setScope(ActionScope.NODE);
		}

		return actions;
	}

	public void setGoldRatio(int goldRatio) {
		this.goldRatio = goldRatio;
	}

	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}

	public void setWorldXmax(int worldXmax) {
		this.worldXmax = worldXmax;
	}

	public void setWorldYmax(final int worldYmax) {
		this.worldYmax = worldYmax;
	}

}
