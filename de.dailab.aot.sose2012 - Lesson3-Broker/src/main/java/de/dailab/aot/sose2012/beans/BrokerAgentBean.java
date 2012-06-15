package de.dailab.aot.sose2012.beans;

import java.io.Serializable;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2012.ontology.Agree;
import de.dailab.aot.sose2012.ontology.Failure;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Proxy;
import de.dailab.aot.sose2012.ontology.Refuse;
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

public class BrokerAgentBean extends AbstractAgentBean implements
		ResultReceiver {
	
	// used actions
	private IActionDescription send;
	
	// group variables
	public static final String GROUP_ADDRESS = "room-clima";
	private IGroupAddress groupAddress = null;
	
	// msg template
	private final static JiacMessage INF = new JiacMessage(new Inform<Object>());
	private final static JiacMessage REFUSE = new JiacMessage(new Refuse<Object>());
	private final static JiacMessage AGREE = new JiacMessage(new Agree<Object>());
	private final static JiacMessage FAILURE = new JiacMessage(new Failure<Object>());
	private final static JiacMessage PROXY = new JiacMessage(new Proxy());
	
	// message observer
	private final SpaceObserver<IFact> observerINF = new InformObserver();
	private final SpaceObserver<IFact> observerREFUSE = new RefuseObserver();
	private final SpaceObserver<IFact> observerAGREE = new AgreeObserver();
	private final SpaceObserver<IFact> observerFAILURE = new FailureObserver();
	private final SpaceObserver<IFact> observerPROXY = new ProxyObserver();

	@Override
	public void doInit() throws Exception {
		this.groupAddress = CommunicationAddressFactory.createGroupAddress(GROUP_ADDRESS);
	}

	@Override
	public void doStart() throws Exception {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		this.memory.attach(observerINF, INF);
		this.memory.attach(observerREFUSE, REFUSE);
		this.memory.attach(observerAGREE, AGREE);
		this.memory.attach(observerFAILURE, FAILURE);
		this.memory.attach(observerPROXY, PROXY);
		
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
		// TODO Auto-generated method stub
		super.execute();
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
				this.log.error("could not send msg" + result.getFailure());
			}
		}
	}
	
	final class ProxyObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8138712861238378764L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// TODO
				}
			}
		}
	}
	
	final class InformObserver implements SpaceObserver<IFact> {

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
					//  TODO
				}
			}
		}
	}

	final class AgreeObserver implements SpaceObserver<IFact> {

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
					// handle Agree from Heizungsagent
					Agree<Object> agree =  (Agree<Object>) ((JiacMessage)object).getPayload();
					log.debug("Broker hat Agree von " + agree.getSenderID().getName() + " erhalten");
				}
			}
		}
	}

	final class RefuseObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3288266214466079670L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle Refuse from Heizungsagent
					Refuse<Object> refuse =  (Refuse<Object>) ((JiacMessage)object).getPayload();
					log.debug("Broker hat Refuse von " + refuse.getProposer().getName() + " erhalten");
				}
			}
		}
	}
	
	final class FailureObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3288266214466079670L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle Failure from Heizungsagent
					Failure<Object> refuse =  (Failure<Object>) ((JiacMessage)object).getPayload();
					log.debug("Broker hat Failure von " + refuse.getProposer().getName() + " erhalten");
				}
			}
		}
	}

}
