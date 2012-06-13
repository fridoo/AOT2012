package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class CallForProposal implements IFact {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5059054491494272221L;
	private final Task<?> task;
	private final QualityOfService quality;
	
	public CallForProposal() {
		this.task = null;
		this.quality = null;
	}

	
	public CallForProposal(Task<?> task, QualityOfService quality) {
		this.task = task;
		this.quality = quality;
	}

	public QualityOfService getQuality() {
		return quality;
	}

	public Task<?> getTask() {
		return task;
	}


	@Override
	public String toString() {
		return quality +" "+ task;
	}

}
