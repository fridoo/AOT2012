package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Request;
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
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class FensterAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	private IActionDescription getWinAction;
	private IActionDescription setWinAction;
	private IActionDescription send;

	private final static JiacMessage REQ = new JiacMessage(
			new Request<Object>());
	private JiacMessage reject;

	public static final String GROUP_ADDRESS = "temp-group";
	private IGroupAddress groupAddress = null;

	private Boolean winPos = null;
	
	private final SpaceObserver<IFact> observer = new RequestObserver();

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
		
		this.memory.attach(observer, REQ);
		
		reject = new JiacMessage(new Inform<String>(
				"Request rejected. Value out of bounds", thisAgent.getAgentDescription()));
	}

	// wait for requests to open/close the window
	@Override
	public void execute() {
		invoke(getWinAction, new Serializable[] {}, this);
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
							new WinPos(newPos), thisAgent.getAgentDescription()));
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
	
	final class RequestObserver implements SpaceObserver<IFact> {

		static final long serialVersionUID = -1143799774862165996L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					@SuppressWarnings("unchecked")
					Request<Object> r = (Request<Object>) ((JiacMessage) object)
							.getPayload();
					if (r.getValue() instanceof WinPos) {
						if (r.getAgent().getName().equals("UserAgent")) {
							WinPos w = (WinPos) r.getValue();
							Boolean newPos = w.getValue();
							invoke(setWinAction, new Serializable[] { newPos }, FensterAgentBean.this);
							log.debug("FensterAgent set window to: open? " + newPos);
						} else {
							invoke(send, new Serializable[] { reject, r.getAgent().getMessageBoxAddress() });
							log.debug("FensterAgent sent reject msg to: " + r.getAgent());
						}
					}
				}
			}
		}
	}
}
