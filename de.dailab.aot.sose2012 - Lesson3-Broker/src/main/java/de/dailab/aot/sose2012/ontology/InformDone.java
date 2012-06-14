package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class InformDone<T> implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7029579924965654650L;

	
	private final IAgentDescription senderID;
	private final T task;
	
	public InformDone() {
		this.senderID = null;
		this.task = null;
	}
	
	public InformDone(IAgentDescription senderID, T task) {
		super();
		this.senderID = senderID;
		this.task = task;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}

	public T getTask() {
		return task;
	}
	
	
}
