package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class RejectProposal implements IFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1472237375405349694L;
	private final Task<?> task;

	public RejectProposal() {
		this.task = null;
	}
	
	public RejectProposal(Task<?> task) {
		this.task = task;
	}

	public Task<?> getTask() {
		return task;
	}

	@Override
	public String toString() {
		return "Der Task " + task.getId() +" wurde rejected";
	}
	
	
}

