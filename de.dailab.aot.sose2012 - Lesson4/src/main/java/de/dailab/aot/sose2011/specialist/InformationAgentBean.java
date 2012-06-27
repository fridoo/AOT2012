package de.dailab.aot.sose2011.specialist;

import it.sauronsoftware.feed4j.bean.FeedItem;

import java.util.HashSet;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2011.blackboard.BlackboardAgentBean;
import de.dailab.aot.sose2012.ontology.IFeedItem;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class InformationAgentBean extends BlackboardAgentBean {

	HashSet<IFeedItem> items;
	private String category = "sport";
	
	private IFeedItem iFeedItemTpl;
	private final SpaceObserver<IFact> observer = new MyObserver();
	private int counter = 0;
	
	

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	
	
	@Override
	public void doStart() throws Exception {
		log.info("Starte InfoAgent mit Kategorie: " + category);
		iFeedItemTpl = new IFeedItem(null, category);
		this.blackboard.attach(this.observer, iFeedItemTpl);
	}

	@Override
	public void execute() {
//		items = (HashSet<IFeedItem>) blackboard.removeAll(iFeedItemTpl);
//		if(items.isEmpty()) {
//			return;
//		}
//		for (IFeedItem setItem : items) {
//			FeedItem item = setItem.getFeedItem();
//			log.info("........ Kategorie ... " + setItem.getCategory() );
//			log.info(item.getTitle());
//			log.info(item.getDescriptionAsText());
//			log.info(">>> For more information visit: " + item.getLink());
//			log.info("'''''''''''''''''''''''''");
//		}
//		log.debug("-----------------------------------------------");
	}
	
	final class MyObserver implements SpaceObserver<IFact> {

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof IFeedItem) {
					IFeedItem setItem =  (IFeedItem) object;
					
					if (setItem.getCategory().equals(InformationAgentBean.this.category)) {
						FeedItem item = setItem.getFeedItem();
						log.info("........ Kategorie ... " + setItem.getCategory() );
						log.info(item.getTitle());
						log.info(item.getDescriptionAsText());
						log.info(">>> For more information visit: " + item.getLink());
						log.info("'''''''''''''''''''''''''");
					} else {
						log.info(++counter + " not my category: " + setItem.getCategory());
					}
					
				}

			}
		}
	}
	
	
}
