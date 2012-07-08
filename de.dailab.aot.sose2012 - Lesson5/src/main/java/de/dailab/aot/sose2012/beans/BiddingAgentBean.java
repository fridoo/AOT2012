package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.Random;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

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

/**
 * HeizungsAgentBean is the main class for HeizungsAgent. It receives
 * heating-state changerequests from other agents and fulfills them if they're
 * valid, or rejects them.
 * 
 * @author Mitch
 * 
 */
public class BiddingAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// used actions
	private IActionDescription send;

	// group variables
	public static final String GROUP_ADDRESS = "auction";
	private IGroupAddress groupAddress = null;

	// msg templates
	private static final JiacMessage STARTAUC = new JiacMessage(new StartOfAuction());
	private static final JiacMessage WIN = new JiacMessage(new InformWin());
	
	// message observer
	private final SpaceObserver<IFact> observerSTARTAUC = new StartAuctionObserver();
	private final SpaceObserver<IFact> observerWIN = new InformWinObserver();

	private final int ITEMS_TO_BUY = 3;
	private int budget;
	private int itemsBought;


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

		this.budget = 500;
		this.itemsBought = 0;
		
		// attach observers
		this.memory.attach(observerSTARTAUC, STARTAUC);
		this.memory.attach(observerWIN, WIN);
	}

	@Override
	public void execute() {
		// TODO implement bidding strategy
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
					// reply only if budget is not zero and this Agents still wants to buy more items
					if (itemsBought < ITEMS_TO_BUY && budget !=  0) { 
						StartOfAuction soa = (StartOfAuction) ((JiacMessage) object).getPayload();
						RegisterForAuction rfa = new RegisterForAuction(thisAgent.getAgentDescription(), soa.getAuctionID());
						JiacMessage rfaMsg = new JiacMessage(rfa);
						// register for auction
						invoke(send, new Serializable[] { rfaMsg, soa.getSenderID().getMessageBoxAddress() });
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
					if (infWin.getItemBought().getCurrentBid().getSenderID().equals(thisAgent.getAgentDescription())) {
						log.info(thisAgent.getAgentName() + " bought " + infWin.getItemBought().getName() 
								+ " for" + infWin.getItemBought().getCurrentBid().getBid());
						itemsBought++;
					}
				}
			}
		}
	}
	
}