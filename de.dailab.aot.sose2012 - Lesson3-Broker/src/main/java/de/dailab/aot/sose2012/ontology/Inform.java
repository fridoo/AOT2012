package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Inform<T> implements IFact {

	private static final long serialVersionUID = 7235524095190187627L;
	private final T value;
	private final IAgentDescription agent;
	private final int inReplyToID;

	public Inform() {
		this.value = null;
		this.agent = null;
		this.inReplyToID = -1;
	}
	
	public Inform(T value, IAgentDescription agent) {
		this.value = value;
		this.agent = agent;
		this.inReplyToID = -1;
	}

	public Inform(T value, IAgentDescription agent, int inreplyto) {
		this.value = value;
		this.agent = agent;
		this.inReplyToID = inreplyto;
	}

	public IAgentDescription getAgent() {
		return agent;
	}

	public T getValue() {
		return value;
	}

	public int getInReplyToID() {
		return inReplyToID;
	}

	
}