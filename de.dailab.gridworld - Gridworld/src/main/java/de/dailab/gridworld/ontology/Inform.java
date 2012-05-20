package de.dailab.gridworld.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Inform<T> implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3141226436635585086L;
	private final T value;

	public Inform() {
		this.value = null;
	}
	
	public Inform(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}

}
