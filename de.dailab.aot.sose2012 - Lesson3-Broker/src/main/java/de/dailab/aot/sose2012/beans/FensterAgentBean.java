package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Request;
import de.dailab.aot.sose2012.ontology.WinService;
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

/**
 * FensterAgentBean is the main class for FensterAgent. It retrieves window-position
 * data and informs every other agent on the platform if it changes from its former 
 * state. Furthermore it receives window-position changerequests from other agents
 * and fulfills them if they're coming from an UserAgent or rejects them otherwise.
 * 
 * @author Mitch
 *
 */
public class FensterAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// used actions
	private IActionDescription getWinAction;
	private IActionDescription setWinAction;
	private IActionDescription send;

	//reject msg
	private JiacMessage reject;
	// request-msg template
	private final static JiacMessage REQ = new JiacMessage(
			new Request<Object>());

	// group variables
	public static final String GROUP_ADDRESS = "room-clima";
	private IGroupAddress groupAddress = null;

	// state
	private Boolean winPos = null;
	
	// message observer
	private final SpaceObserver<IFact> observer = new WinRequestObserver();

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
	}

	/**
	 *  join group, get actions, attach observer, set reject msg
	 */
	@Override
	public void doStart() {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		this.getWinAction = this.retrieveAction(Window.ACTION_GET_WINDOW_STATE);
		this.setWinAction = this.retrieveAction(Window.ACTION_UPDATE_WINDOW_STATE);
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		// only look for requests
		this.memory.attach(observer, REQ);
		
		reject = new JiacMessage(new Inform<String>(
				"Request rejected. Authorization needed", thisAgent.getAgentDescription()));
	}

	/**
	 *  refresh window informations
	 */
	@Override
	public void execute() {
		invoke(getWinAction, new Serializable[] {}, this);
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
		// send a message to everyone in case someone closed/opened the window
		} else if (Window.ACTION_GET_WINDOW_STATE.equals(result.getAction()
				.getName())) {
			if (result.getFailure() == null) {
				Boolean newPos = (Boolean) result.getResults()[0];
				if (this.winPos != newPos) {
					JiacMessage transport = new JiacMessage(new Inform<WinService>(
							new WinService(newPos), thisAgent.getAgentDescription()));
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
	
	/**
	 *  looks for requests to open/close the window (but only from UserAgent)
	 *  
	 * @author Mitch
	 *
	 */
	final class WinRequestObserver implements SpaceObserver<IFact> {

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
					if (r.getValue() instanceof WinService) {
						if (r.getSenderID().getName().equals("UserAgent")) {
							WinService w = (WinService) r.getValue();
							Boolean newPos = w.getValue();
							invoke(setWinAction, new Serializable[] { newPos }, FensterAgentBean.this);
							log.debug("FensterAgent set window to: open? " + newPos);
						} else {
							invoke(send, new Serializable[] { reject, r.getSenderID().getMessageBoxAddress() });
							log.debug("FensterAgent sent reject msg to: " + r.getSenderID());
						}
					}
				}
			}
		}
	}
}
