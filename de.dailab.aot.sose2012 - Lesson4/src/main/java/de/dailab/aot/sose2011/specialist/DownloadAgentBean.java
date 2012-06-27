package de.dailab.aot.sose2011.specialist;

import java.net.URL;

import it.sauronsoftware.feed4j.FeedException;
import it.sauronsoftware.feed4j.FeedParser;
import it.sauronsoftware.feed4j.bean.Feed;
import java.net.MalformedURLException;
import de.dailab.aot.sose2011.blackboard.BlackboardAgentBean;

public class DownloadAgentBean extends BlackboardAgentBean {
	
	URL url = null;
	Feed feed, oldFeed;
	
	@Override	
	public void doInit() throws Exception {
		super.doInit();
	    try {
	        url = new URL("http://www.sueddeutsche.de/app/service/rss/alles/rss.xml");
	    } catch (MalformedURLException ex) {
	        log.debug("MalFormedUrlException");
	        url = null;
	    }
	}

	@Override
	public void execute() {
		if(url==null) {
			return;
		}
		try {
			feed = FeedParser.parse(url);
		} catch (FeedException e) {
			log.debug("FeedException");
			feed = null;
		}
		if(feed==null || feed.equals(oldFeed)) {
			return;
		}
		
		blackboard.write(fact)
	}

	
	
	
}
