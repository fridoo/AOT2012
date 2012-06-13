package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Refuse implements IFact {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5356049511864333769L;
	private final IAgentDescription proposer;
	private final Task<?> referencedTask;
	
	public Refuse() {
		this.proposer = null;
		this.referencedTask = null;
	}
	
	public Refuse(IAgentDescription proposer, Task<?> referencedTask) {
		this.proposer = proposer;
		this.referencedTask = referencedTask;
	}

	public Task<?> getReferencedTask() {
		return referencedTask;
	}

	public IAgentDescription getProposer() {
		return proposer;
	}
}
