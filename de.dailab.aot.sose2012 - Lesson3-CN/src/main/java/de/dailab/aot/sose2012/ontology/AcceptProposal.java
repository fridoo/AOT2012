package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class AcceptProposal implements IFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7130776925914259556L;
	private final Task<?> task;

	public AcceptProposal() {
		this.task = null;
	}
	
	public AcceptProposal(Task<?> task) {
		this.task = task;
	}

	public Task<?> getTask() {
		return task;
	}

	@Override
	public String toString() {
		return task.toString();
	}
	
}
