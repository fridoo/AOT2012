package de.dailab.gridworld.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Field implements IFact {

	static final long serialVersionUID = -951504972954286903L;

	private final Random random = new Random(System.identityHashCode(this));
	public final Integer width;

	public final Integer height;
	
	private final Position[][] cells;
	
	public Field() {
		this.width = null;
		this.height = null;
		this.cells = null;
	}
	
	public Field(int width, int height, int goldPerCent) {
		assert width > 1;
		assert height > 1;
		assert goldPerCent >= 0 && goldPerCent <= 100;
		this.width = width;
		this.height = height;
		
		cells = new Position[width][height];
		for (int x= 0; x < width; x++) {
			for (int y= 0; y < width; y++) {
				
				Position p = new Position(x, y);
				p.gold = random.nextInt(100) < goldPerCent;
				cells[x][y] = p;
			}
		}
	}
	
	public Position getCell(int x, int y) {
		assert x >= 0 && x < width; 
		assert y >= 0 && y < height;
		return cells[x][y];
	}
	
	public Collection<Position> getAllCells() {
		Collection<Position> positions = new ArrayList<Position>(width * height);
		for (Position[] poss : cells) {
			positions.addAll(Arrays.asList(poss));
		}
		return positions;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Field) {
			Field other = (Field) obj;
			return (other.width == null || other.width == width) &&
					(other.height == null || other.height == height);
		}
		return false;
	}
}
