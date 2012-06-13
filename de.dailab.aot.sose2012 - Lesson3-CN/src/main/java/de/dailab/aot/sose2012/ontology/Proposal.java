package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Proposal implements IFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3074375310016907414L;
	public final QualityOfService quality;
	private final IAgentDescription proposer;
	public IAgentDescription getProposer() {
		return proposer;
	}

	private final Task<?> referencedTask;
	
	public Proposal() {
		this(null, null, null);
	}
	
	public Proposal(QualityOfService quality, IAgentDescription proposer, Task<?> referencedTask) {
		this.quality = quality;
		this.proposer = proposer;
		this.referencedTask = referencedTask;
	}
	
	@Override
	public String toString() {
		return "Proposal(proposer=" + proposer.getName() + ", Task "+referencedTask.toString() + ", quality="+ String.valueOf(quality)+")";
	}
	
	public Task<?> getReferencedTask() {
		return referencedTask;
	}
}
