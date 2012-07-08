package de.dailab.aot.sose2012.ontology;

import de.dailab.aot.sose2012.entities.ItemForSale;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class StartOfAuction implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3829892738569835778L;
	private final int auctionID;
	private final ItemForSale itemForSell;
	private final IAgentDescription senderID;
	
	public StartOfAuction() {
		this.auctionID = -1;
		this.itemForSell = null;
		this.senderID = null;
	}
	
	public StartOfAuction(IAgentDescription senderID, int auctionID, ItemForSale itemForSale) {
		this.senderID = senderID;
		this.auctionID = auctionID;
		this.itemForSell = itemForSale;
	}

	public int getAuctionID() {
		return auctionID;
	}

	public ItemForSale getItemForSell() {
		return itemForSell;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}
	
	
}
