package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

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
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class RaumKlimaAgentBean extends AbstractAgentBean implements ResultReceiver {

	public static final String GROUP_ADDRESS = "temp-group";
	private IGroupAddress groupAddress = null;
	
	private IActionDescription send;
	
	private final static JiacMessage INF = new JiacMessage(
			new Inform<Object>());
	
	private final int TEMP_TO_ACHIVE = 21;
	private int windowPos;
	private int heating;
	private Temperature currentTemp;
	private AgentDescription agentTemplate;
	private IAgentDescription agent;
	
	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory.createGroupAddress(GROUP_ADDRESS);
		this.agentTemplate = new AgentDescription(null, "HeizungsAgent", null, null, null, null);
		
		this.windowPos = 1;
		this.heating = 0;
		this.currentTemp = new Temperature(16.0);
	}
	
	@Override
	public void doStart() {
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress, }, this);
		
//		doesnt work somehow
//		agent = thisAgent.searchAgent(agentTemplate);
		List<IAgentDescription> agents = thisAgent.searchAllAgents(agentTemplate);
		for (IAgentDescription a : agents) {
			if(a.getName().equals("HeizungsAgent")) {
				agent = a;
			}
		}
	}
	
	@Override
	public void execute() {
		Set<JiacMessage> messages = memory.removeAll(INF);
		
		for (JiacMessage msg : messages) {
			@SuppressWarnings("unchecked")
			Inform<Object> i = (Inform<Object>) msg.getPayload();
			if (i.getValue() instanceof Temperature) {
				currentTemp = (Temperature) i.getValue();
			} else if (i.getValue() instanceof WinPos) {
				WinPos w = (WinPos) i.getValue();
				setWindowPos(w.getValue());
			}
		}
		
		double nextTemp = calcNextTemperature(this.heating, this.windowPos, currentTemp.getValue());
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
		tempAfterAdjustment = calcNextTemperature(heating, this.windowPos, currentTemp.getValue());
		log.debug("Heating is next set to " + heating);
		log.debug("Nextnext temp should be " + tempAfterAdjustment);
		
		Hstate h = new Hstate(heating);
		JiacMessage transport = new JiacMessage(new Request<Hstate>(h, thisAgent.getAgentDescription()));
		this.invoke(send, new Serializable[] { transport, agent.getMessageBoxAddress() });
		log.debug("RaumklimaAgent wants HeizungsAgent to set heating to : "+h.getState());
	}
	
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
				this.log.error("could not join chat group address: " + result.getFailure());
			}
		} else if (ICommunicationBean.ACTION_SEND
				.equals(resultActionName)) {
			if (result.getFailure() != null) {
				this.log.error("could not send msg" + result.getFailure());
			}
		}
	}
	
	private void setWindowPos(boolean w) {
		this.windowPos = w ? 2 : 1;
	}
	
	private double calcNextTemperature(int heating, int windowPos, double currentTemp) {
		return ( 0.11 * heating * (30 - currentTemp)
				- 0.07 * windowPos * currentTemp
				+ currentTemp );
	}

}
