package de.dailab.aot.sose2012.ontology;

import it.sauronsoftware.feed4j.bean.FeedItem;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class IFeedItem implements IFact{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8263444030138204110L;

	private final FeedItem feedItem;
	private  final String category;
	
	public String getCategory() {
		return category;
	}

	public FeedItem getFeedItem() {
		return feedItem;
	}
	
	public IFeedItem() {
		this.feedItem = null;
		this.category = null;
	}

	public IFeedItem(FeedItem feedItem, String category) {
		this.feedItem = feedItem;
		this.category = category;
	}
	
}
