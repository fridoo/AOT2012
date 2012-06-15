package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Request<T> implements IFact{


	/**
	 * 
	 */
	private static final long serialVersionUID = -2723570574728601432L;
	private final T value;
	private final IAgentDescription senderID;
	private final int requestID;
	
	public Request() {
		this.value = null;
		this.senderID = null;
		this.requestID = -1;
	}

	
	public Request(T value, IAgentDescription agent) {
		this.value = value;
		this.senderID = agent;
		this.requestID = -1;
	}
	
	public Request(T value, IAgentDescription agent, int requestID) {
		this.value = value;
		this.senderID = agent;
		this.requestID = requestID;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}


	public T getValue() {
		return value;
	}


	public int getRequestID() {
		return requestID;
	}
	
	

}
