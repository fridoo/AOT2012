package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Proxy implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7583831021191671932L;

	private final IAgentDescription senderID;
	private final IFact message;
	
	public Proxy() {
		this.senderID = null;
		this.message = null;
	}
	
	public Proxy(IAgentDescription senderID, IFact message) {
		this.senderID = senderID;
		this.message = message;
	}


	public IAgentDescription getSenderID() {
		return senderID;
	}


	public IFact getMessage() {
		return message;
	}
	
	
}
