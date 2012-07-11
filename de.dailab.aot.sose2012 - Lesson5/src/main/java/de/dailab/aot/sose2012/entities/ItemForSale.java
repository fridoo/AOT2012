package de.dailab.aot.sose2012.entities;

import de.dailab.aot.sose2012.ontology.Bid;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class ItemForSale implements IFact {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2131652073351291966L;
	// the name of the item
	private final String name;
	// the actual value of the item (might not be used)
	private final int value;
	// the currently highest bid
	private Bid currentlyHighestBid;
	
	public ItemForSale() {
		this.name = null;
		this.value = 0;
		this.currentlyHighestBid = null;
	}

	public ItemForSale(String name, int value) {
		this.name = name;
		this.value = value;
		this.currentlyHighestBid = new Bid();
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public Bid getCurrentlyHighestBid() {
		return currentlyHighestBid;
	}

	public void setCurrentlyHighestBid(Bid currentBid) {
		this.currentlyHighestBid = currentBid;
	}

	
}
