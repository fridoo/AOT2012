package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Failure implements IFact {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5021779951399558093L;
	private final IAgentDescription proposer;
	private final Task<?> referencedTask;
	private final String exception;
	
	public Failure() {
		this.proposer = null;
		this.referencedTask = null;
		this.exception = null;
	}
	
	public Failure(IAgentDescription proposer, Task<?> referencedTask, String exception) {
		this.proposer = proposer;
		this.referencedTask = referencedTask;
		this.exception = exception;
	}

	public Task<?> getReferencedTask() {
		return referencedTask;
	}

	public IAgentDescription getProposer() {
		return proposer;
	}
	
	public String getException() {
		return exception;
	}
}
