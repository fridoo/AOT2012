package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.List;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import de.dailab.aot.sose2012.ontology.Hstate;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Request;
import de.dailab.aot.sose2012.ontology.Temperature;
import de.dailab.aot.sose2012.ontology.WinPos;
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
	private IAgentDescription agent;

	// used actions
	private IActionDescription send;

	// group variables
	public static final String GROUP_ADDRESS = "temp-group";
	private IGroupAddress groupAddress = null;

	// inform-msg template
	private final static JiacMessage INF = new JiacMessage(new Inform<Object>());

	// states
	private final int TEMP_TO_ACHIVE = 21;
	private int windowPos;
	private int heating;
	private Temperature currentTemp;

	// message observer
	private final SpaceObserver<IFact> observer = new InformObserver();

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
		this.agentTemplate = new AgentDescription(null, "HeizungsAgent", null,
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
		List<IAgentDescription> agents = thisAgent
				.searchAllAgents(agentTemplate);
		for (IAgentDescription a : agents) {
			if (a.getName().equals("HeizungsAgent")) {
				agent = a;
			}
		}

		this.memory.attach(observer, INF);
	}

	/**
	 * compute heating-state and send a request to Heizungsagent
	 */
	@Override
	public void execute() {

		double nextTemp = calcNextTemperature(this.heating, this.windowPos,
				currentTemp.getValue());
		double newHeating = (TEMP_TO_ACHIVE + 0.07 * this.windowPos * nextTemp - nextTemp)
				/ (0.11 * (30 - nextTemp));
		log.debug("current " + currentTemp.getValue());
		log.debug("Next temp without adjustment " + nextTemp);
		log.debug("Heating is now set to: " + this.heating);
		log.debug("Heating would be " + newHeating);

		double tempAfterAdjustment;
		if (newHeating > 5) {
			heating = 5;
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

		Hstate h = new Hstate(heating);
		JiacMessage transport = new JiacMessage(new Request<Hstate>(h,
				thisAgent.getAgentDescription()));
		this.invoke(send,
				new Serializable[] { transport, agent.getMessageBoxAddress() });
		log.debug("RaumklimaAgent wants HeizungsAgent to set heating to : "
				+ h.getState());
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
					Inform<Object> i = (Inform<Object>) ((JiacMessage) object)
							.getPayload();
					if (i.getValue() instanceof Temperature) {
						currentTemp = (Temperature) i.getValue();
					} else if (i.getValue() instanceof WinPos) {
						WinPos w = (WinPos) i.getValue();
						setWindowPos(w.getValue());
					} else if (i.getValue() instanceof String) {
						String s = (String) i.getValue();
						log.debug("RaumklimaAgent received msg: " + s
								+ "from agent: " + i.getAgent());
					}
				}
			}
		}
	}
}
