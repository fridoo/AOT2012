package de.dailab.aot.sose2011.specialist;

import java.util.HashSet;

import it.sauronsoftware.feed4j.bean.Feed;
import it.sauronsoftware.feed4j.bean.FeedItem;
import it.sauronsoftware.feed4j.bean.RawElement;
import de.dailab.aot.sose2011.blackboard.BlackboardAgentBean;
import de.dailab.aot.sose2012.ontology.IFeed;
import de.dailab.aot.sose2012.ontology.IFeedItem;

public class FeedAgentBean extends BlackboardAgentBean {

	IFeed iFeedTPL = new IFeed();
	IFeed iFeed;
	Feed feed;
	long time = System.currentTimeMillis();
	HashSet<String> items = new HashSet<String>();
	String cat = null;

	@Override
	public void execute() {
		iFeed = blackboard.read(iFeedTPL);
		if (iFeed == null) {
			return;
		}
		// Wenn 24 Stunden vergangen sind soll die interne Liste gelöscht werden
		if (System.currentTimeMillis() - time > 86400000) {
			items = new HashSet<String>();
			time = System.currentTimeMillis();
		}
		feed = iFeed.getFeed();
		for (int i = 0; i < feed.getItemCount(); ++i) {
			FeedItem item = feed.getItem(i);
			if (!items.contains(item.getGUID())) {
				IFeedItem msg = null;
				RawElement rawElement = item.getElement("", "category");
				if (rawElement != null && rawElement.getValue() != null) {
					msg = new IFeedItem(item, rawElement.getValue().toLowerCase());
					log.info(rawElement.getValue().toLowerCase());
				} else {
					msg = new IFeedItem(item, "keine");
				}
				blackboard.write(msg);
				items.add(item.getGUID());
				log.debug(item.getTitle());
			} else {
				log.debug("items contained " + item.getTitle() + " already");
			}
		}
	}

}
