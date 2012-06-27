package de.dailab.aot.sose2011.specialist;

import it.sauronsoftware.feed4j.bean.FeedItem;

import java.util.HashSet;

import de.dailab.aot.sose2011.blackboard.BlackboardAgentBean;
import de.dailab.aot.sose2012.ontology.IFeedItem;

public class InformationAgentBean extends BlackboardAgentBean {

	private IFeedItem iFeedItemTpl;
	HashSet<IFeedItem> items;
	private String category = "sport";

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	
	
	@Override
	public void doStart() throws Exception {
		log.info(category);
		iFeedItemTpl = new IFeedItem(null, category);
	}

	@Override
	public void execute() {
		items = (HashSet<IFeedItem>) blackboard.removeAll(iFeedItemTpl);
		if(items.isEmpty()) {
			return;
		}
		for (IFeedItem setItem : items) {
			FeedItem item = setItem.getFeedItem();
			log.info("........ Kategorie ... " + setItem.getCategory() );
			log.info(item.getTitle());
			log.info(item.getDescriptionAsText());
			log.info(">>> For more information visit: " + item.getLink());
			log.info("'''''''''''''''''''''''''");
		}
		log.debug("-----------------------------------------------");
	}
	
	
}
