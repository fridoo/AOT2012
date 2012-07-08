package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Refuse<T> implements IFact {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5356049511864333769L;
	private final IAgentDescription senderID;
	private final T referencedTask;
	
	public Refuse() {
		this.senderID = null;
		this.referencedTask = null;
	}
	
	public Refuse(IAgentDescription proposer, T referencedTask) {
		this.senderID = proposer;
		this.referencedTask = referencedTask;
	}

	public T getReferencedTask() {
		return referencedTask;
	}

	public IAgentDescription getProposer() {
		return senderID;
	}
}
