package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class WinPos implements IFact {

	private static final long serialVersionUID = -6372366171007517062L;
	
	private final Integer value;
	
	public WinPos() {
		value = null;
	}
	
	public WinPos(Boolean b) {
		if(b) {
			value = 2;
		} else {
			value = 1;
		}
	}

	public int getValue() {
		return value;
	}


}
