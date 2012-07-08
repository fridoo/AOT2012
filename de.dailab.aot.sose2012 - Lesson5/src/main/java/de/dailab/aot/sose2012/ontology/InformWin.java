package de.dailab.aot.sose2012.ontology;

import de.dailab.aot.sose2012.entities.ItemForSale;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class InformWin implements IFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 331767235866240652L;
	private final IAgentDescription senderID;
	private final ItemForSale itemBought;
	
	
	public InformWin() {
		this.senderID = null;
		this.itemBought = null;
	}

	public InformWin(IAgentDescription senderID, ItemForSale itemBought) {
		this.senderID = senderID;
		this.itemBought = itemBought;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}

	public ItemForSale getItemBought() {
		return itemBought;
	}
	
	
}
