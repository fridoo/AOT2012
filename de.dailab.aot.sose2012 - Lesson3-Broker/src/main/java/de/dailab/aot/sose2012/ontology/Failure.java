package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Failure<T> implements IFact {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5021779951399558093L;
	private final IAgentDescription senderID;
	private final T referencedTask;
	private final String exception;
	
	public Failure() {
		this.senderID = null;
		this.referencedTask = null;
		this.exception = null;
	}
	
	public Failure(IAgentDescription proposer, T referencedTask, String exception) {
		this.senderID = proposer;
		this.referencedTask = referencedTask;
		this.exception = exception;
	}

	public T getReferencedTask() {
		return referencedTask;
	}

	public IAgentDescription getProposer() {
		return senderID;
	}
	
	public String getException() {
		return exception;
	}
}
