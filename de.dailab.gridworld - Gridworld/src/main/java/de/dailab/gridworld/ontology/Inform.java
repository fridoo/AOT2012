package de.dailab.gridworld.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Inform<T> implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3141226436635585086L;
	private final T value;
	private final IAgentDescription agent;

	public Inform() {
		this.value = null;
		this.agent = null;
	}
	
	public Inform(T value, IAgentDescription agent) {
		this.value = value;
		this.agent = agent;
	}
	
	public IAgentDescription getAgent() {
		return agent;
	}

	public T getValue() {
		return value;
	}

}
