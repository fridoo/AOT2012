package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.Random;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2012.ontology.Agree;
import de.dailab.aot.sose2012.ontology.Failure;
import de.dailab.aot.sose2012.ontology.HeatingService;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.QualityOfService;
import de.dailab.aot.sose2012.ontology.QueryRef;
import de.dailab.aot.sose2012.ontology.Refuse;
import de.dailab.aot.sose2012.ontology.Request;
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
public class HeizungsAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// used actions
	private IActionDescription send;

	// msg templates
	private static final JiacMessage REQUESTmsg = new JiacMessage(new Request<Object>());
	private static final JiacMessage QUERYREFmsg = new JiacMessage(new QueryRef<Object>());

	// group variables
	public static final String GROUP_ADDRESS = "room-clima";
	private IGroupAddress groupAddress = null;

	// message observer
	private final SpaceObserver<IFact> observerRequest = new RequestObserver();
	private final SpaceObserver<IFact> observerQueryRef = new QueryRefObserver();

	// used messages
//	private JiacMessage refuse;
//	private JiacMessage failure;

	//states set in xml
	double quality = 0.0;
	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}

	String provider = "";
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	int range = 5;
	
	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	// states
	int busy = 0;


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

		// attach observers
		this.memory.attach(observerRequest, REQUESTmsg);
		this.memory.attach(observerQueryRef, QUERYREFmsg);
		
		log.debug(provider + " gestartet");
	}

	@Override
	public void execute() {
		if (busy > 0) {
			--busy;
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
	
	private boolean doesTaskSucceed() {
		Random r = new Random();
		return (r.nextDouble() <= this.quality);
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
	
	final class RequestObserver implements SpaceObserver<IFact> {
		
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
					// TODO handle request from broker
					@SuppressWarnings("unchecked")
					Request<Object> request = (Request<Object>) ((JiacMessage) object).getPayload();
					if (request.getValue() instanceof HeatingService) {
						HeatingService hsToExecute = (HeatingService) request.getValue();
						if (hsToExecute.heating > HeizungsAgentBean.this.range || busy > 0) { // busy or out of range
							// send refusal
							JiacMessage refuse = new JiacMessage(
									new Refuse<HeatingService>(thisAgent.getAgentDescription(), hsToExecute));
							invoke(send, new Serializable[] { refuse, request.getAgent().getMessageBoxAddress() });
						} else {
							// send agree and do task
							JiacMessage agree = new JiacMessage(
									new Agree<HeatingService>(thisAgent.getAgentDescription(), hsToExecute));
							invoke(send, new Serializable[] { agree, request.getAgent().getMessageBoxAddress() });
							
							// Heizungsagent is busy during the duration of the task and the double amount afterwards
							HeizungsAgentBean.this.busy = hsToExecute.duration + hsToExecute.duration * 2;
							
							if (doesTaskSucceed()) {
								 // send HeatingService to broker
								HeatingService doHeating = new HeatingService(hsToExecute.heating, hsToExecute.duration);
								JiacMessage inform = new JiacMessage(
										new Inform<HeatingService>(doHeating, thisAgent.getAgentDescription()));
								invoke(send, new Serializable[] {inform , request.getAgent().getMessageBoxAddress() });
							} else {
								// send failure to broker
								JiacMessage failure = new JiacMessage(
										new Failure<HeatingService>(thisAgent.getAgentDescription(), 
												hsToExecute, "Task execution unsuccessful :("));
								invoke(send, new Serializable[] { failure, request.getAgent().getMessageBoxAddress() });
							}
							
						}
					}
				}
			}
		}
	}
	
	final class QueryRefObserver implements SpaceObserver<IFact> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1357620803550398360L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			
			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					@SuppressWarnings("unchecked")
					QueryRef<Object> qr = (QueryRef<Object>) ((JiacMessage) object).getPayload(); 
					if (qr.getInformAbout() instanceof QualityOfService) {
						// send own QualityOfService to the Agent who asked
						if (HeizungsAgentBean.this.busy == 0) { // only answer if not busy
							QualityOfService myQOS = new QualityOfService(HeizungsAgentBean.this.provider, 
									HeizungsAgentBean.this.range, 
									HeizungsAgentBean.this.quality, 
									HeizungsAgentBean.this.busy > 0 ? false : true);
							JiacMessage inform = new JiacMessage(
									new Inform<QualityOfService>(myQOS, 
									thisAgent.getAgentDescription()));
							invoke(send, new Serializable[] { inform, qr.getSenderID().getMessageBoxAddress() });
						}
					}
				}
			}
		}
	}
}
