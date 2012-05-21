package de.dailab.gridworld.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Request<T> implements IFact{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5909808163471638514L;
	private final T value;
	private final IAgentDescription agent;
	private final Double priority;
	
	public Request() {
		this.value = null;
		this.agent = null;
		this.priority = null;
	}
	
	public Request(T value, IAgentDescription agent) {
		this.value = value;
		this.agent = agent;
		this.priority = null;
	}
	
	public Request(T value, IAgentDescription agent, double priority) {
		this.value = value;
		this.agent = agent;
		this.priority = priority;
	}
	
	public Double getPriority() {
		return priority;
	}

	public IAgentDescription getAgent() {
		return agent;
	}

	public T getValue() {
		return value;
	}

}
