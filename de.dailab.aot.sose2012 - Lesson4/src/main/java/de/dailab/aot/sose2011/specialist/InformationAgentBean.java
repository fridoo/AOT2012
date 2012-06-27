package de.dailab.aot.sose2011.specialist;

import java.util.HashSet;

import it.sauronsoftware.feed4j.bean.Feed;
import it.sauronsoftware.feed4j.bean.FeedItem;
import de.dailab.aot.sose2011.blackboard.BlackboardAgentBean;
import de.dailab.aot.sose2012.ontology.IFeed;
import de.dailab.aot.sose2012.ontology.IFeedItem;

public class InformationAgentBean extends BlackboardAgentBean{

	IFeed iFeedTPL = new IFeed();
	IFeed iFeed;
	Feed feed;
	long time = System.currentTimeMillis();
	HashSet<FeedItem> items = new HashSet<FeedItem>();
	String cat = null;

	// TODO Kategorie auslesen und in IFeedItem speichern, im Moment bleibt es null.
	@Override
	public void execute() {
		iFeed = blackboard.read(iFeedTPL);
		if(iFeed == null) {
			return;
		}
		//Wenn 24 Stunden vergangen sind soll die interne Liste gelöscht werden
		if(System.currentTimeMillis() - time > 86400000) {
			items = new HashSet<FeedItem>();
			time = System.currentTimeMillis();
		}
		feed = iFeed.getFeed();
		for (int i = 0; i < feed.getItemCount(); ++i) {
			FeedItem item = feed.getItem(i);
			for (int j= 0; j<item.getAttributeCount(); ++j) {
				if (!items.contains(item)) {
					cat = item.getElementValue(item.getNamespaceURI(), "category");
					IFeedItem msg = new IFeedItem(item, cat);
					blackboard.write(msg);
					items.add(item);
					log.debug(item.getTitle());
				}
			}
		}
	}

	
}
