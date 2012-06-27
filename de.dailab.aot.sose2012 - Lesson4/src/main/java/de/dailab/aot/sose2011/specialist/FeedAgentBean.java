package de.dailab.aot.sose2011.specialist;

import java.util.HashSet;

import it.sauronsoftware.feed4j.bean.Feed;
import it.sauronsoftware.feed4j.bean.FeedItem;
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

	// TODO Kategorie auslesen und in IFeedItem speichern, im Moment bleibt es
	// null.
	// Außerdem werden noch Nachrichten doppelt reingeschrieben und es scheinen
	// nur Nachrichten von einem Feed anzukommen.
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
				// cat = item.getElementValue(item.getNamespaceURI(),"category");
				IFeedItem msg = new IFeedItem(item, cat);
				blackboard.write(msg);
				items.add(item.getGUID());
				log.debug(item.getTitle());
			} else {
				log.debug("items contained " + item.getTitle());
			}
		}
	}

}
