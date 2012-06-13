package de.dailab.aot.sose2012.user;

import java.io.Serializable;
import java.util.List;

import de.dailab.aot.sose2012.ontology.WinService;
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

/**
 * UserAgentBean is the main class for UserAgent. It receives commands
 * to open/close, which can be triggered by a user.
 * 
 * @author Mitch
 * 
 */
public final class UserAgentBean extends AbstractAgentBean implements ResultReceiver {

	public static final String CMD_WINDOW_OPEN = "Open Window";
	public static final String CMD_WINDOW_CLOSE = "Close Window";

	private WindowSwitch frame = null;
	
	// agent templates for messaging
	private AgentDescription agentTemplate;
	private IAgentDescription agent;
	
	// used actions
	private IActionDescription send;
	
	// group variables
	public static final String GROUP_ADDRESS = "temp-group";
	private IGroupAddress groupAddress = null;
	
	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
		this.agentTemplate = new AgentDescription(null, "FensterAgent", null, null, null, null);
	}
	
	/**
	 *  join group, get actions, get the FensterAgent-Description for messaging
	 */
	@Override
	public void doStart() throws Exception {
		super.doStart();

		frame = new WindowSwitch(this);
		frame.setVisible(true);
		
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);
		
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
//		doesnt work somehow
//		agent = thisAgent.searchAgent(agentTemplate);
		List<IAgentDescription> agents = thisAgent.searchAllAgents(agentTemplate);
		for (IAgentDescription a : agents) {
			if(a.getName().equals("FensterAgent")) {
				agent = a;
			}
		}
		
	}

	/**
	 * leave group in the end
	 */
	@Override
	public void doStop() throws Exception {
		if (frame != null) {
			frame.dispose();
			frame = null;
		}
		Action leave = this
				.retrieveAction(ICommunicationBean.ACTION_LEAVE_GROUP);
		ActionResult result = this.invokeAndWaitForResult(leave,
				new Serializable[] { groupAddress });
		if (result.getFailure() != null) {
			this.log.error("could not leave temp-group: " + result.getFailure());
		}
	}

	/**
	 * send a request to FensterAgent to open/close the window
	 * @param cmd
	 */
	protected void windowSwitch(String cmd) {
		Boolean newPos = null;
		if (cmd.equals(CMD_WINDOW_OPEN)) {
			newPos = true;
		} else if (cmd.equals(CMD_WINDOW_CLOSE)) {
			newPos = false;
		} else {
			log.warn("Could not understand command: " + cmd);
			return;
		}
		
		Request<WinService> req = new Request<WinService>(new WinService(newPos), thisAgent.getAgentDescription());
		JiacMessage transport = new JiacMessage(req);
		
		invoke(send, new Serializable[] { transport, agent.getMessageBoxAddress() } );
		log.debug("UserAgent sent a msg to FensterAgent: " + cmd);

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
}
