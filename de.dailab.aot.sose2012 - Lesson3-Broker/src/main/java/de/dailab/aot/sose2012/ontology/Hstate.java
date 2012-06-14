package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Hstate implements IFact {
	
	private static final long serialVersionUID = -2118798899359275361L;
	private final Integer state;
	
	public Hstate() {
		state = null;
	}
	
	public Hstate(Integer i) {
		state = i;
	}

	public Integer getState() {
		return state;
	}
	
}
