package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Inform<T> implements IFact {

	private static final long serialVersionUID = 7235524095190187627L;
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