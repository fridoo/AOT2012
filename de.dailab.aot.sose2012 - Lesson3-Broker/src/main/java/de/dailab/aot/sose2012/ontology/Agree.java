package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Agree<T> implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3205926996701436664L;

	private final IAgentDescription senderID;
	private final T task;
	
	public Agree() {
		this.senderID = null;
		this.task = null;
	}
	
	public Agree(IAgentDescription senderID, T task) {
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
