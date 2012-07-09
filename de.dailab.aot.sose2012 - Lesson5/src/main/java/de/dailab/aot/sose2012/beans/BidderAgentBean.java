package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.Random;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2012.ontology.Bid;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.InformWin;
import de.dailab.aot.sose2012.ontology.RegisterForAuction;
import de.dailab.aot.sose2012.ontology.StartOfAuction;
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

/**
 * 
 * @author Mitch
 * 
 */
public class BidderAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// used actions
	private IActionDescription send;

	// group variables
	public static final String GROUP_ADDRESS = "auction";
	private IGroupAddress groupAddress = null;

	// msg templates
	private static final JiacMessage STARTAUC = new JiacMessage(new StartOfAuction());
	private static final JiacMessage WIN = new JiacMessage(new InformWin());
	private static final JiacMessage BID = new JiacMessage(new Inform<Bid>());
	
	// message observer
	private final SpaceObserver<IFact> observerSTARTAUC = new StartAuctionObserver();
	private final SpaceObserver<IFact> observerWIN = new InformWinObserver();
	private final SpaceObserver<IFact> observerBID = new InformBidObserver();

	private int ITEMS_TO_BUY = 3;
	private int budget = 500;
	private int itemsLeft = 8;
	private IAgentDescription auctioneer;
	private int currentAuction;
	private int lastBid;
	private int biddingLimit;
	private Random rand;
	private Bid oldBid = new Bid();
	private Bid currentBid = new Bid();
	
	int STRATEGY = 1;
	public int getSTRATEGY() {
		return STRATEGY;
	}

	public void setSTRATEGY(int sTRATEGY) {
		STRATEGY = sTRATEGY;
	}

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
	}

	/**
	 * join group, get actions, attach observer, set reject msg
	 */
	@Override
	public void doStart() {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		this.currentAuction = -1;
		this.rand = new Random();
		
		// attach observers
		this.memory.attach(observerSTARTAUC, STARTAUC);
		this.memory.attach(observerWIN, WIN);
		this.memory.attach(observerBID, BID);
		
		log.info("Starting " + thisAgent.getAgentName() + " with strategy " + STRATEGY);
	}

	@Override
	public void execute() {
		
		// only bid if participating at an auction and still got items to buy
		if (currentAuction != -1 && ITEMS_TO_BUY > 0) {
			// only bid if a new bid arrived since last bid
			if (currentBid.getBid() != oldBid.getBid()) {
				// only bid if i'm not the highest bidder
				if ( ! currentBid.getSenderID().equals(thisAgent.getAgentDescription())) {
					// send higher bid if possible
					switch (STRATEGY) {
					case 1:
						biddingLimit = budget/2 + rand.nextInt(budget/4);
						if (currentBid.getBid() + 1 <= biddingLimit) {
							sendBid(currentBid.getBid() + 1, currentBid.getAuctionID());
						} else {
							log.debug(thisAgent.getAgentName() + " reached bidding limit " + biddingLimit); 
						}
						break;
					case 2:
						biddingLimit = budget / ITEMS_TO_BUY;
						if (currentBid.getBid() + 1 <= biddingLimit) {
							sendBid(currentBid.getBid() + 1, currentBid.getAuctionID());
						} else {
							log.debug(thisAgent.getAgentName() + " reached bidding limit " + biddingLimit); 
						}
						break;
					case 3:
						;
						break;
					default:
						break;
					}
				}
				
				oldBid = currentBid;
			}
		}
		
	}

	/**
	 * leave group in the end
	 */
	@Override
	public void doStop() {
		Action leave = this
				.retrieveAction(ICommunicationBean.ACTION_LEAVE_GROUP);
		ActionResult result = this.invokeAndWaitForResult(leave,
				new Serializable[] { groupAddress });
		if (result.getFailure() != null) {
			this.log.error("could not leave temp-group: " + result.getFailure());
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
				this.log.error("could not send msg " + result.getFailure());
			}
		}
	}
	
	private void sendBid(int amount, int auctionID) {
		Bid bid = new Bid(thisAgent.getAgentDescription(), amount, auctionID);
		lastBid = amount;
		JiacMessage bidMsg = new JiacMessage(bid);
		invoke(send, new Serializable[] { bidMsg, auctioneer.getMessageBoxAddress() });
	}
	
	
	
	final class StartAuctionObserver implements SpaceObserver<IFact> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7644631440961432970L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			
			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					StartOfAuction soa = (StartOfAuction) ((JiacMessage) object).getPayload();
//					log.debug("received start of auction");
					// reply only if budget is not zero and this Agents still wants to buy more items
					if (ITEMS_TO_BUY > 0 && budget > 0) { 
						RegisterForAuction rfa = new RegisterForAuction(thisAgent.getAgentDescription(), soa.getAuctionID());
						JiacMessage rfaMsg = new JiacMessage(rfa);
						// register for auction
						auctioneer = soa.getSenderID();
						currentAuction = rfa.getAuctionID();
						oldBid = new Bid();
						currentBid = new Bid();
						invoke(send, new Serializable[] { rfaMsg, auctioneer.getMessageBoxAddress() });
						log.debug(thisAgent.getAgentName() + " tryed to register, items to buy " + ITEMS_TO_BUY);
						
						// send initial Bid
						sendBid(1, soa.getAuctionID());
					} else {
						log.debug(thisAgent.getAgentName() + " does't join auction " + soa.getAuctionID());
						currentAuction = -1;
						auctioneer = null;
					}
				}
			}
		}
	}
	
	final class InformBidObserver implements SpaceObserver<IFact> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7644631440961432970L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			
			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					log.debug("informBid received ");
					
					@SuppressWarnings("unchecked")
					Inform<Object> inf = (Inform<Object>) ((JiacMessage) object).getPayload();
					if (inf.getValue() instanceof Bid) {
						Bid bid = (Bid) inf.getValue();
						if (bid.getAuctionID() != currentAuction) {
							log.debug("bid ignored, not participating");
							return;
						}
						
						// check if i'm the hightest bidder
						if (! bid.getSenderID().equals(thisAgent.getAgentDescription()) 
								&& bid.getBid() > currentBid.getBid()) {
							oldBid = currentBid;
							currentBid = bid;
							log.debug("new bid at auction " + bid.getBid());
						}
						
						
						
//						if ( ! bid.getSenderID().equals(thisAgent.getAgentDescription())) {
//							// send higher bid if possible
//							switch (STRATEGY) {
//							case 1:
//								biddingLimit = budget/2 + rand.nextInt(budget/4);
//								if (bid.getBid() + 1 <= biddingLimit) {
//									sendBid(bid.getBid() + 1, bid.getAuctionID());
//								} else {
//									log.debug(thisAgent.getAgentName() + " reached bidding limit " + biddingLimit); 
//								}
//								break;
//							case 2:
//								biddingLimit = budget / ITEMS_TO_BUY;
//								if (bid.getBid() + 1 <= biddingLimit) {
//									sendBid(bid.getBid() + 1, bid.getAuctionID());
//								} else {
//									log.debug(thisAgent.getAgentName() + " reached bidding limit " + biddingLimit); 
//								}
//								break;
//							case 3:
//								;
//								break;
//							default:
//								break;
//							}
//							
//						}
					}
				}
			}
		}
	}
	
	
	final class InformWinObserver implements SpaceObserver<IFact> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7644631440961432970L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			
			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					InformWin infWin = (InformWin) ((JiacMessage) object).getPayload();
					// check if I won the auction
					itemsLeft--;
					if (infWin.getItemBought().getCurrentBid().getAuctionID() == currentAuction) {
						currentAuction = -1;
					}
					if (infWin.getItemBought().getCurrentBid().getSenderID().equals(thisAgent.getAgentDescription())) {
						ITEMS_TO_BUY--;
						budget -= infWin.getItemBought().getCurrentBid().getBid();
						log.info(thisAgent.getAgentName() + " bought " + infWin.getItemBought().getName() 
								+ " for " + infWin.getItemBought().getCurrentBid().getBid());
					}
				}
			}
		}
	}
	
}