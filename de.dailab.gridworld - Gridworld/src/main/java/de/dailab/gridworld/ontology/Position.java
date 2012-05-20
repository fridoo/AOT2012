package de.dailab.gridworld.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * A place in a 2D world, with X- and Y-coordinates.
 * 
 * @author mib
 * 
 */
public class Position implements IFact {

	public static final Position HOME = new Position(0, 0);

	static final long serialVersionUID = -7651680471571648359L;
	/**
	 * The X-coordinate of a location within a 2D grid
	 */
	public final Integer x;
	/**
	 * The Y-coordinate of a location within a 2D grid
	 */
	public final Integer y;

	public IAgentDescription agent = null;

	public Boolean gold;

	public Position() {
		this(null, null);
	}

	public Position(Integer px, Integer py) {
		this(px, py, null);
	}

	public Position(final Integer px, final Integer py, Boolean gold) {
		this(px, py, gold, null);
	}
	
	public Position(Integer px, Integer py, Boolean gold, IAgentDescription agent) {
		this.x = px;
		this.y = py;
		this.gold = gold;
		this.agent = agent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position other = (Position) obj;
//			if ( other.x == null && other.y == null ) {
//				return (other.gold == null || this.gold == null || other.gold.equals(this.gold))
//				&& (other.agent == null || this.agent == null || other.agent.equals(this.agent));
//			}
//			else {
//				return (other.x.equals(this.x) && other.y.equals(this.x)); 
//			}
			return (this.x == null || this.x.equals(other.x)) && (this.y == null || this.y.equals(other.y));
			
//			return (other.x == null || this.x == null || other.x.equals(this.x))
//			      && (other.y == null || this.y == null || other.y.equals(this.y))
//			      && (other.gold == null || this.gold == null || other.gold.equals(this.gold))
//			      && (other.agent == null || this.agent == null || other.agent.equals(this.agent))
//			      ;
		}
		return false;
	}

	@Override
	public int hashCode() {
		/*
		 * hash code == system address in Java VM memory
		 */
		return System.identityHashCode(this);
	}

	@Override
	public String toString() {
		return "Position[x=" + this.x + ",y=" + this.y + ",agent=" + (this.agent != null ? this.agent.getAid() : "null") + ",gold="
		      + this.gold + "]";
	}

}
