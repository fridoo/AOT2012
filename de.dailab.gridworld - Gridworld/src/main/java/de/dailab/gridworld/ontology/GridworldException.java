package de.dailab.gridworld.ontology;

public class GridworldException extends Exception {

	static final long serialVersionUID = 1450403242476376358L;
	public final Position position;

	public GridworldException(String msg, Position pos) {
		super(msg);
		this.position = pos;
	}

	public GridworldException(String msg, Position pos, Throwable cause) {
		super(msg, cause);
		this.position = pos;
	}

	public Position getPosition() {
		return position;
	}

}
