package de.dailab.aot.sose2012.beans;

import java.io.Serializable;

import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Temperature;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * ThermoAgentBean is the main class for ThermoAgent. It receives temperature-data and
 * propagates them to every other agent in his group.
 * 
 * @author Mitch
 * 
 */
public class ThermoAgentBean extends AbstractAgentBean implements ResultReceiver {

	// used actions
	private IActionDescription send;
	
	// group variables
	public static final String GROUP_ADDRESS = "temp-group";
	private IGroupAddress groupAddress = null;
	
	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory.createGroupAddress(GROUP_ADDRESS);
	}
	
	/**
	 *  join group, get actions
	 */
	@Override
	public void doStart() {
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress, }, this);
	}
	
	/**
	 * read memory for temps, and msg them to everyone
	 */
	@Override
	public void execute() {
		// read Temperature
		Temperature currentTemp = null;
		currentTemp = memory.read(new Temperature());
		
		// wrap the msg and send it to all groupmembers
		JiacMessage transport = new JiacMessage(new Inform<Temperature>(currentTemp, thisAgent.getAgentDescription()));
		this.invoke(send, new Serializable[] { transport, this.groupAddress });
		log.debug("ThermoAgent sent msg to everyone: "+currentTemp.getValue());
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
				this.log.error("could not join chat group address: " + result.getFailure());
			}
		} else if (ICommunicationBean.ACTION_SEND
				.equals(resultActionName)) {
			if (result.getFailure() != null) {
				this.log.error("could not send msg" + result.getFailure());
			}
		}
	}

}
