package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class RegisterForAuction implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1491985110552992537L;
	private final IAgentDescription senderID;
	private final int auctionID;
	
	public RegisterForAuction() {
		this.senderID = null;
		this.auctionID = -1;
	}

	public RegisterForAuction(IAgentDescription senderID, int auctionID) {
		this.senderID = senderID;
		this.auctionID = auctionID;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}

	public int getAuctionID() {
		return auctionID;
	}
	
	
}
