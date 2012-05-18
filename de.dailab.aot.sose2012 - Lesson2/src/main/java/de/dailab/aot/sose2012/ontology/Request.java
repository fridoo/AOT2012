package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Request<T> implements IFact{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5909808163471638514L;
	private final T value;
	private final IAgentDescription agent;
	
	public Request() {
		this.value = null;
		this.agent = null;
	}

	
	public Request(T value, IAgentDescription agent) {
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
