package de.dailab.gridworld.ontology;

import java.awt.Color;

/**
 * The tutorial groups of the AOT sessions.
 * 
 * @author mib
 * @version SoSe 2011
 */
public enum Group { 
	/**
	 * Group 1 - developer team
	
	 */
	G1(Color.GREEN),
	/**
	 * Group 2 - developer team
	
	 */
	G2(Color.RED),
	/**
	 * Group 3 - developer team
	
	 */
	G3(Color.MAGENTA),
	/**
	 * Group 4 - developer team
	
	 */
	G4(Color.BLUE),
	/**
	 * Group 5 - developer team
	
	 */
	G5(Color.ORANGE),
	/**
	 * Group 6 - developer team
	
	 */
	G6(Color.WHITE),
	
	
	MIB(Color.CYAN),
	JT(Color.BLACK),
	;
	
	public final Color color;
	
	private Group(Color c) {
		color = c;
	}

}
