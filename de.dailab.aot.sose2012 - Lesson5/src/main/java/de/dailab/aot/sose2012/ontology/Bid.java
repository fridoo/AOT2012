package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Bid implements IFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -992452818105864703L;
	private final IAgentDescription senderID;
	private final int bid;
	private final int auctionID;
	
	public Bid() {
		this.bid = -1;
		this.senderID = null;
		this.auctionID = -1;
	}
	
	public Bid(IAgentDescription senderID, int bid, int auctionID) {
		this.bid = bid;
		this.senderID = senderID;
		this.auctionID = auctionID;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}

	public int getBid() {
		return bid;
	}

	public int getAuctionID() {
		return auctionID;
	}
	
	

}
