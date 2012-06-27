package de.dailab.aot.sose2012.ontology;

import it.sauronsoftware.feed4j.bean.Feed;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class IFeed implements IFact{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7094873408755398532L;
	
	private final String category;
	private final Feed feed;
	
	

	public String getCategory() {
		return category;
	}

	public Feed getFeed() {
		return feed;
	}

	public IFeed() {
		this.category = null;
		this.feed = null;
	}
	
	public IFeed(Feed feed, String cat) {
		this.feed = feed;
		this.category = cat;
	}

}
