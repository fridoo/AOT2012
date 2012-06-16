package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.List;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2012.ontology.Agree;
import de.dailab.aot.sose2012.ontology.FailureNoMatch;
import de.dailab.aot.sose2012.ontology.FailureProxy;
import de.dailab.aot.sose2012.ontology.HeatingService;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.InformDoneProxy;
import de.dailab.aot.sose2012.ontology.Proxy;
import de.dailab.aot.sose2012.ontology.Refuse;
import de.dailab.aot.sose2012.ontology.Request;
import de.dailab.aot.sose2012.ontology.Temperature;
import de.dailab.aot.sose2012.ontology.WinService;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * RaumKlimaAgentBean is the main class for RaumKlimaAgent. It computes the best
 * heating-state in order to maintain a temperature near 21°C and sends its
 * request to HeizungsAgent.
 * 
 * @author Mitch
 * 
 */
public class RaumKlimaAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// agent templates for messaging
	private AgentDescription agentTemplate;
	private IAgentDescription broker;
	private final String BROKER_NAME = "BrokerAgent";

	// used actions
	private IActionDescription send;

	// group variables
	public static final String GROUP_ADDRESS = "room-clima";
	private IGroupAddress groupAddress = null;

	// msg template
	private final static JiacMessage INF = new JiacMessage(new Inform<Object>());
	private final static JiacMessage INFDoneProxy = new JiacMessage(new InformDoneProxy<Object>());
	private final static JiacMessage REFUSE = new JiacMessage(new Refuse<Object>());
	private final static JiacMessage AGREE = new JiacMessage(new Agree<Object>());
	private final static JiacMessage FAILUREnoMatch = new JiacMessage(new FailureNoMatch<Object>());
	private final static JiacMessage FAILUREProxy = new JiacMessage(new FailureProxy<Object>());

	// states
	private final int TEMP_TO_ACHIVE = 21;
	private int windowPos;
	private int heating;
	private Temperature currentTemp;
	private int taskID = 0;

	// message observer
	private final SpaceObserver<IFact> observerINF = new InformObserver();
	private final SpaceObserver<IFact> observerINFDoneProxy = new InformDoneProxyObserver();
	private final SpaceObserver<IFact> observerREFUSE = new RefuseObserver();
	private final SpaceObserver<IFact> observerAGREE = new AgreeObserver();
	private final SpaceObserver<IFact> observerFAILUREnoMatch = new FailureNoMatchObserver();
	private final SpaceObserver<IFact> observerFAILUREProxy = new FailureProxyObserver();

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
		this.agentTemplate = new AgentDescription(null, BROKER_NAME, null,
				null, null, null);

		this.windowPos = 1;
		this.heating = 0;
		this.currentTemp = new Temperature(16.0);
	}

	/**
	 *  join group, get actions, attach observer, get the HeizungsAgent-Description for messaging
	 */
	@Override
	public void doStart() {
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);

		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress, }, this);

		// doesnt work somehow
		// agent = thisAgent.searchAgent(agentTemplate);
		List<IAgentDescription> agents = thisAgent.searchAllAgents(agentTemplate);
		log.debug("befor broker search");
		for (IAgentDescription a : agents) {
			log.debug("search for broker");
			if (a.getName().equals(BROKER_NAME)) {
				log.debug("found broker");
				broker = a;
			}
		}

		this.memory.attach(observerINF, INF);
		this.memory.attach(observerINFDoneProxy, INFDoneProxy);
		this.memory.attach(observerREFUSE, REFUSE);
		this.memory.attach(observerAGREE, AGREE);
		this.memory.attach(observerFAILUREnoMatch, FAILUREnoMatch);
		this.memory.attach(observerFAILUREProxy, FAILUREProxy);
	}

	/**
	 * compute heating-state and send a request to Broker
	 */
	@Override
	public void execute() {
		
		currentTemp = memory.read(new Temperature());
		if (currentTemp == null) {
			currentTemp = new Temperature(20.0);
		}

		double nextTemp = calcNextTemperature(this.heating, this.windowPos,
				currentTemp.getValue());
		double newHeating = (TEMP_TO_ACHIVE + 0.07 * this.windowPos * nextTemp - nextTemp)
				/ (0.11 * (30 - nextTemp));
		log.debug("current " + currentTemp.getValue());
		log.debug("Next temp without adjustment " + nextTemp);
		log.debug("Heating is now set to: " + this.heating);
		log.debug("Heating would be " + newHeating);

		double tempAfterAdjustment;
		if (newHeating > 6) {
			heating = 6;
		} else if (newHeating < 0) {
			heating = 0;
		} else {
			if (newHeating % 1 < 0.5) {
				heating = (int) newHeating;
			} else {
				heating = (int) (newHeating + 1);
			}
		}
		tempAfterAdjustment = calcNextTemperature(heating, this.windowPos,
				currentTemp.getValue());
		log.debug("Heating is next set to " + heating);
		log.debug("Nextnext temp should be " + tempAfterAdjustment);

		HeatingService hservice = new HeatingService(heating, 1);
		JiacMessage proxyMsg = new JiacMessage(
				new Proxy(thisAgent.getAgentDescription(), 
				new Request<HeatingService>(hservice, thisAgent.getAgentDescription(), ++this.taskID))
				);
		this.invoke(send, new Serializable[] { proxyMsg, broker.getMessageBoxAddress() }); // send to Broker
		log.debug("RaumklimaAgent wants Broker to set heating to : "
				+ hservice.heating);
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
				this.log.error("could not send msg" + result.getFailure());
			}
		}
	}

	/**
	 * helper for setting windowPos-state
	 * @param w
	 */
	private void setWindowPos(boolean w) {
		this.windowPos = w ? 2 : 1;
	}

	/**
	 * helper for computing the next temperature on given data
	 * 
	 * @param heating
	 * @param windowPos
	 * @param currentTemp
	 * @return
	 */
	private double calcNextTemperature(int heating, int windowPos,
			double currentTemp) {
		return (0.11 * heating * (30 - currentTemp) - 0.07 * windowPos
				* currentTemp + currentTemp);
	}

	/**
	 *  looks for information about temperature, window-position (and updates 
	 *  the respective states) and receives rejects.
	 * 
	 * @author Mitch
	 *
	 */
	final class InformObserver implements SpaceObserver<IFact> {

		static final long serialVersionUID = -1143799774862165996L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					@SuppressWarnings("unchecked")
					Inform<Object> inf = (Inform<Object>) ((JiacMessage) object).getPayload();
					if (inf.getValue() instanceof WinService) {
						WinService w = (WinService) inf.getValue();
						setWindowPos(w.getValue());
					} else if (inf.getValue() instanceof String) {
						String s = (String) inf.getValue();
						log.debug("RaumklimaAgent received msg: " + s+ "from agent: " + inf.getAgent());
					} else if (inf.getValue() instanceof HeatingService) {
						HeatingService heat = (HeatingService) inf.getValue();
						// create a new Service with a fresh creationdate so
						// former dates will not matter anymore
						HeatingService newHeat = new HeatingService(
								heat.heating, heat.duration);
						memory.write(newHeat);
						log.debug("Heatservice empfangen und ins Memory geschrieben: "
								+ newHeat);
					}
				}
			}
		}
	}
	
	final class InformDoneProxyObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5792784214903627611L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			
			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle InformDoneProxy from Broker
					log.debug("Raumklimaagent hat Inform_Done_Proxy vom Broker erhalten");
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
					// handle Refuse from Broker
					log.debug("Raumklimaagent hat Refuse vom Broker erhalten");
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
					// handle Agree from Broker
					log.debug("Raumklimaagent hat Agree vom Broker erhalten");
				}
			}
		}
	}
	
	final class FailureNoMatchObserver implements SpaceObserver<IFact> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -4864856113617633340L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			
			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle FailureNoMatch from Broker
					log.debug("Raumklimaagent hat Failure_No_Match vom Broker erhalten");
				}
			}
		}
	}
	
	final class FailureProxyObserver implements SpaceObserver<IFact> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1038041090243746429L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			
			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle FailureProxy from Broker
					log.debug("Raumklimaagent hat Failure_Proxy vom Broker erhalten");
				}
			}
		}
	}
}
