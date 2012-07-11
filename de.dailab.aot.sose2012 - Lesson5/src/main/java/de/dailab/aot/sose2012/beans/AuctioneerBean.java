package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2012.ontology.Bid;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.InformWin;
import de.dailab.aot.sose2012.ontology.RegisterForAuction;
import de.dailab.aot.sose2012.ontology.StartOfAuction;
import de.dailab.aot.sose2012.entities.ItemForSale;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class AuctioneerBean extends AbstractAgentBean implements
		ResultReceiver {
	
	// used actions
	private IActionDescription send;
	
	// group variables
	public static final String GROUP_ADDRESS = "auction";
	private IGroupAddress groupAddress = null;
	
	// msg template
	private final static JiacMessage BID = new JiacMessage(new Bid());
	private final static JiacMessage REGISTER = new JiacMessage(new RegisterForAuction());
	
	// message observer
	private final SpaceObserver<IFact> observerBID = new BidObserver();
	private final SpaceObserver<IFact> observerREG = new RegisterObserver();
	
	private List<IAgentDescription> registeredBidders;
	private List<ItemForSale> itemsToSell;
	private int NUM_OF_ITEMS_TO_SELL = 8;
	private boolean auctionRunning = false;
	private boolean postedWinners = false;
	// the index of the item in itemsToSell that is currently being sold
	private int currentAuctionIndex = -1;
	private final long BID_TIMEOUT = 2000; 
	private long nextDeadline = 0;
	

	@Override
	public void doInit() throws Exception {
		this.groupAddress = CommunicationAddressFactory.createGroupAddress(GROUP_ADDRESS);
	}

	@Override
	public void doStart() throws Exception {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		// create the items to sell
		this.itemsToSell = new ArrayList<ItemForSale>(NUM_OF_ITEMS_TO_SELL);
		for (int i = 0; i < NUM_OF_ITEMS_TO_SELL; i++) {
			this.itemsToSell.add(new ItemForSale("item_" + i, 0));
		}
		
		this.auctionRunning = false;
		this.currentAuctionIndex = -1;
		this.registeredBidders = new ArrayList<IAgentDescription>(3);
		
		this.memory.attach(observerBID, BID);
		this.memory.attach(observerREG, REGISTER);
	}

	@Override
	public void doStop() throws Exception {
		Action leave = this.retrieveAction(ICommunicationBean.ACTION_LEAVE_GROUP);
		ActionResult result = this.invokeAndWaitForResult(leave,
				new Serializable[] { groupAddress });
		if (result.getFailure() != null) {
			this.log.error("could not leave temp-group: " + result.getFailure());
		}
	}

	@Override
	public void execute() {
		
		if (this.postedWinners) { // all items sold and winners posted
			return;
		}
		
		if (auctionRunning && System.currentTimeMillis() > this.nextDeadline) { // deadline is expired
			// stop auction
			log.debug("Auction stoped");
			this.auctionRunning = false;
			
			if (this.itemsToSell.get(currentAuctionIndex).getCurrentlyHighestBid() != null
					&& this.itemsToSell.get(currentAuctionIndex).getCurrentlyHighestBid().getSenderID() != null) {
				// inform all agents about winner
				InformWin infWin = new InformWin(thisAgent.getAgentDescription(), this.itemsToSell.get(currentAuctionIndex));
				JiacMessage infWinMsg = new JiacMessage(infWin);
				this.invoke(send, new Serializable[] { infWinMsg, this.groupAddress });
			} else { // no bid present for the item, items could not be sold
				log.info("item " + this.itemsToSell.get(currentAuctionIndex).getName() + " could not be sold");
			}
			
		}
		
			
		if (!auctionRunning) { // start next auction or end of all auctions
			this.currentAuctionIndex++;
			if ( this.currentAuctionIndex >= 0 
					&& this.currentAuctionIndex < this.itemsToSell.size() ) { // at least one more item to sell
				// start new auction
				log.info("Starting auction number: " + this.currentAuctionIndex 
						+ " for item " + this.itemsToSell.get(currentAuctionIndex).getName());
				StartOfAuction soa = new StartOfAuction(thisAgent.getAgentDescription(), 
						this.currentAuctionIndex, this.itemsToSell.get(currentAuctionIndex));
				JiacMessage soaMsg = new JiacMessage(soa);
				this.invoke(send, new Serializable[] { soaMsg, this.groupAddress });
				this.nextDeadline = System.currentTimeMillis() + BID_TIMEOUT + 2000;
				this.auctionRunning = true;
				
			} else { // no more items to sell
				if (!this.postedWinners) {
					this.postedWinners = true;
					log.info("---- End Of All Auctions ----");
					for (ItemForSale ifs : this.itemsToSell) {
						log.info(ifs.getName() + " was bought by " 
						+ ((ifs.getCurrentlyHighestBid().getSenderID() != null) ? ifs.getCurrentlyHighestBid().getSenderID().getName() : "NO-BUYER")
						+ " for " + ((ifs.getCurrentlyHighestBid().getSenderID() != null) ? ifs.getCurrentlyHighestBid().getBid() : "-") + " AE");
					}
				}
			}
		}
		
	}

	@Override
	public void receiveResult(ActionResult result) {
		String resultActionName = result.getAction().getName();
		if (ICommunicationBean.ACTION_JOIN_GROUP.equals(resultActionName)) {
			if (result.getFailure() != null) {
				this.log.error("could not join chat group address: "
						+ result.getFailure());
			}
		} else if (ICommunicationBean.ACTION_SEND.equals(resultActionName)) {
			if (result.getFailure() != null) {
				this.log.debug("could not send msg" + result.getFailure());
			}
		}
	}
	

	
	final class BidObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7429494904157842669L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					Bid bid = (Bid) ((JiacMessage) object).getPayload();
					// only accept registered Bidders and an auction must be running
					if (auctionRunning && registeredBidders.contains(bid.getSenderID())) { 
						// is the bid for the current auction and the amount higher than the current price
						if (currentAuctionIndex == bid.getAuctionID() 
							&& bid.getBid() > itemsToSell.get(currentAuctionIndex).getCurrentlyHighestBid().getBid() ) { 
							log.debug("Valid Bid received from " + bid.getSenderID().getName() + " amount: " + bid.getBid());
							// bid accepted
							itemsToSell.get(currentAuctionIndex).setCurrentlyHighestBid(bid);
							// inform all bidders about new bid
							Inform<Bid> inf = new Inform<Bid>(bid, thisAgent.getAgentDescription());
							JiacMessage infMsg = new JiacMessage(inf);
							invoke(send, new Serializable[] { infMsg, groupAddress });
							// set new deadline
							nextDeadline = System.currentTimeMillis() + BID_TIMEOUT;
						} else {
							log.debug("Bid " + bid.getBid() + " from " 
									+ bid.getSenderID().getName() + " not accepted, bid not valid");
						}
					} else {
						log.debug("Bid not accepted from " + bid.getSenderID().getName() + ", no auction or not registered");
					}
					
				}
			}
		}
	}

	final class RegisterObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4763457929220578956L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					RegisterForAuction rfa =  (RegisterForAuction) ((JiacMessage)object).getPayload();
					if (auctionRunning && currentAuctionIndex == rfa.getAuctionID()) {
						log.info(rfa.getSenderID().getName() + " registered to auction " + currentAuctionIndex);
						AuctioneerBean.this.registeredBidders.add(rfa.getSenderID());
					}
				}
			}
		}
	}
	
}
	

	
