package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Inform<T> implements IFact {

	private static final long serialVersionUID = 7235524095190187627L;
	private final T value;
	private final IAgentDescription senderID;
	private final int inReplyToID;

	public Inform() {
		this.value = null;
		this.senderID = null;
		this.inReplyToID = -1;
	}
	
	public Inform(T value, IAgentDescription agent) {
		this.value = value;
		this.senderID = agent;
		this.inReplyToID = -1;
	}

	public Inform(T value, IAgentDescription agent, int inreplyto) {
		this.value = value;
		this.senderID = agent;
		this.inReplyToID = inreplyto;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}

	public T getValue() {
		return value;
	}

	public int getInReplyToID() {
		return inReplyToID;
	}

	
}