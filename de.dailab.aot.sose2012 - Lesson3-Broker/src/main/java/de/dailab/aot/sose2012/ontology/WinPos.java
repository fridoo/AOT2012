package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class WinPos implements IFact {

	private static final long serialVersionUID = -6372366171007517062L;
	
	private final Boolean value;
	
	public WinPos() {
		value = null;
	}
	
	public WinPos(Boolean b) {
		this.value = b;
	}

	public Boolean getValue() {
		return value;
	}


}
