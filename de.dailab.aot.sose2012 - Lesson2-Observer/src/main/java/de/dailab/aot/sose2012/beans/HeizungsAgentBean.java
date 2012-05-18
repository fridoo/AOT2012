package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import de.dailab.aot.sose2012.effectors.Heating;
import de.dailab.aot.sose2012.ontology.Hstate;
import de.dailab.aot.sose2012.ontology.Inform;
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

public class HeizungsAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	private IActionDescription setHeatAction;
	private IActionDescription send;

	private final static JiacMessage REQ = new JiacMessage(
			new Request<Object>());
	private JiacMessage reject;

	public static final String GROUP_ADDRESS = "temp-group";
	private IGroupAddress groupAddress = null;

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

		this.setHeatAction = this.retrieveAction(Heating.ACTION_UPDATE_STATE);
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);

		this.memory.attach(observer, REQ);
		
		reject = new JiacMessage(new Inform<String>(
				"Request rejected. Value out of bounds", thisAgent.getAgentDescription()));
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
		} else if (Heating.ACTION_UPDATE_STATE.equals(resultActionName)) {
			if (result.getFailure() != null) {
				this.log.error("could not set heating " + result.getFailure());
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
					if (r.getValue() instanceof Hstate) {
						Hstate h = (Hstate) r.getValue();
						if (h.getState() >= 0 && h.getState() <= 5) {
							invoke(setHeatAction,
									new Serializable[] { h.getState() }, HeizungsAgentBean.this);
							log.debug("HeizungAgent sets heating to "
									+ h.getState());
						} else {
							invoke(send, new Serializable[] { reject,
									r.getAgent().getMessageBoxAddress() });
							log.debug("Heizungsagent sent reject msg to one Agent: "
									+ r.getAgent());
						}
					}
				}
			}
		}
	}
}
