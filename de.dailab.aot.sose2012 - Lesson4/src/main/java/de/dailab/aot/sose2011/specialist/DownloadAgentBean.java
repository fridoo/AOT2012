package de.dailab.aot.sose2011.specialist;

import java.net.URL;

import it.sauronsoftware.feed4j.FeedException;
import it.sauronsoftware.feed4j.FeedParser;
import it.sauronsoftware.feed4j.bean.Feed;
import it.sauronsoftware.feed4j.bean.FeedItem;

import java.net.MalformedURLException;
import de.dailab.aot.sose2011.blackboard.BlackboardAgentBean;
import de.dailab.aot.sose2012.ontology.IFeed;

public class DownloadAgentBean extends BlackboardAgentBean {
	
	URL[] url = new URL[5];
	Feed oldFeed, tmpFeed;
	IFeed iFeedTpl = new IFeed();
	
	@Override	
	public void doInit() throws Exception {
		super.doInit();
	    try {
	        url[0] = new URL("http://www.sueddeutsche.de/app/service/rss/alles/rss.xml");
	        url[1] = new URL("http://www.taz.de/!p3270;rss/");
	        url[2] = new URL("http://www.heise.de/newsticker/heise-atom.xml");
	        url[3] = new URL("http://rss.slashdot.org/Slashdot/slashdot");
	        url[4] = new URL("http://newsfeed.zeit.de/all");
	    } catch (MalformedURLException ex) {
	        log.debug("MalFormedUrlException");
	        url=null;
	    }
	}

	@Override
	public void execute() {
		if(url==null) {
			return;
		}
		log.debug("Download Execution");
		Feed feed = new Feed();
		int i = 0;
		try {
			for (i = 0; i < url.length; ++i) {
				tmpFeed = FeedParser.parse(url[i]);
				for (int j = 0; j < tmpFeed.getItemCount(); ++j) {
					FeedItem item = tmpFeed.getItem(j);
					feed.addItem(item);
				}
				log.debug("parsed feed " + i + " total feedcount: " + feed.getItemCount());
			}
		} catch (FeedException e) {
			log.debug("FeedException for Feed " + url[i].getHost().toString());
			feed = null;
		}
		if(feed==null || feed.equals(oldFeed)) {
			return;
		}
		IFeed iFeed = new IFeed(feed, null); // ändern
		blackboard.removeAll(iFeedTpl);
		blackboard.write(iFeed);
		log.debug("added new feed to Bb");
		oldFeed = feed;
	}
	
}
