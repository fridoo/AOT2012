package de.dailab.aot.sose2012.entities;

import de.dailab.aot.sose2012.ontology.Bid;

public class ItemForSale {
	
	// the name of the item
	private final String name;
	// the actual value of the item (might not be used)
	private final int value;
	// the currently highest bid
	private Bid currentBid;
	
	public ItemForSale() {
		this.name = null;
		this.value = 0;
		this.currentBid = null;
	}

	public ItemForSale(String name, int value) {
		this.name = name;
		this.value = value;
		this.currentBid = new Bid();
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public Bid getCurrentBid() {
		return currentBid;
	}

	public void setCurrentBid(Bid currentBid) {
		this.currentBid = currentBid;
	}

	
}
