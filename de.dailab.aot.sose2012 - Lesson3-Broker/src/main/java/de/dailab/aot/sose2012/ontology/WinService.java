package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class WinService implements IFact {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5943119522708650967L;
	public final Boolean value;
	public final Long creationDate;
	
	public WinService() {
		value = null;
		creationDate = null;
		
	}
	
	public WinService(Boolean b) {
		this.value = b;
		this.creationDate = System.currentTimeMillis();
	}

	public Boolean getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "WinService [value=" + value + ", creationDate=" + creationDate
				+ "]";
	}


}