package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import de.dailab.aot.sose2012.effectors.Heating;
import de.dailab.aot.sose2012.ontology.Hstate;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Request;
import de.dailab.aot.sose2012.ontology.Temperature;
import de.dailab.aot.sose2012.ontology.WinPos;
import de.dailab.aot.sose2012.sensors.Window;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class FensterAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	private IActionDescription getWinAction;
	private IActionDescription setWinAction;
	private IActionDescription send;

	private final static JiacMessage REQ = new JiacMessage(
			new Request<Object>());
	private final JiacMessage reject = new JiacMessage(new Inform<String>(
			"Request rejected. Not authorized"));

	public static final String GROUP_ADDRESS = "temp-group";
	private IGroupAddress groupAddress = null;

	private Boolean winPos = null;
	private int i;

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
	}

	// join group, get actions
	@Override
	public void doStart() {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		this.getWinAction = this.retrieveAction(Window.ACTION_GET_WINDOW_STATE);
		this.setWinAction = this
				.retrieveAction(Window.ACTION_UPDATE_WINDOW_STATE);
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
	}

	// wait for requests to open/close the window
	@Override
	public void execute() {
		Set<JiacMessage> messages = memory.removeAll(REQ);

		invoke(getWinAction, new Serializable[] {}, this);

		for (JiacMessage msg : messages) {
			Request<Object> r = (Request<Object>) msg.getPayload();
			if (r.getValue() instanceof WinPos) {
				if (r.getAgent().getName().equals("UserAgent")) {
					WinPos w = (WinPos) r.getValue();
					Boolean newPos = w.getValue();
					invoke(setWinAction, new Serializable[] { newPos }, this);
					log.debug("FensterAgent set window to: open? " + newPos);
				} else {
					invoke(send, new Serializable[] { reject, r.getAgent().getMessageBoxAddress() });
					log.debug("FensterAgent sent reject msg to: " + r.getAgent());
				}
			}
		}

	}

	// leave group
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
		// send a message to everyone in case someone closed/opened the window
		} else if (Window.ACTION_GET_WINDOW_STATE.equals(result.getAction()
				.getName())) {
			if (result.getFailure() == null) {
				Boolean newPos = (Boolean) result.getResults()[0];
				if (this.winPos != newPos) {
					JiacMessage transport = new JiacMessage(new Inform<WinPos>(
							new WinPos(newPos)));
					this.invoke(send, new Serializable[] { transport,
							this.groupAddress });
					log.debug("FensterAgent sent windowposition msg to everyone: open? "
							+ newPos);
					this.winPos = newPos;
				}
				log.debug("FensterAgent retrieved window-position: open? " + newPos);
			} else {
				this.log.error("could not retrieve window position "
						+ result.getFailure());
			}
		}
	}
}
