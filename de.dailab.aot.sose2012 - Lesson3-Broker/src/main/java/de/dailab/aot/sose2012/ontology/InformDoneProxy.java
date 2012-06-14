package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class InformDoneProxy<T> implements IFact {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2585420304555605839L;
	private final IAgentDescription senderID;
	private final T task;
	
	public InformDoneProxy() {
		this.senderID = null;
		this.task = null;
	}
	
	public InformDoneProxy(IAgentDescription senderID, T task) {
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
