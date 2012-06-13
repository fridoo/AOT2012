package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.ArrayList;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import de.dailab.aot.sose2012.ontology.AcceptProposal;
import de.dailab.aot.sose2012.ontology.CallForProposal;
import de.dailab.aot.sose2012.ontology.Failure;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Proposal;
import de.dailab.aot.sose2012.ontology.QualityOfService;
import de.dailab.aot.sose2012.ontology.Refuse;
import de.dailab.aot.sose2012.ontology.RejectProposal;
import de.dailab.aot.sose2012.ontology.Task;
import de.dailab.aot.sose2012.ontology.WinService;
import de.dailab.aot.sose2012.sensors.Window;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class FensterAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// used actions
	private IActionDescription send;
	private IActionDescription getWinAction;
	private IActionDescription setWinAction;

	// msg templates
	private static final JiacMessage CFP = new JiacMessage(
			new CallForProposal());
	private static final JiacMessage accept = new JiacMessage(
			new AcceptProposal());
	private static final JiacMessage reject = new JiacMessage(
			new RejectProposal());

	// group variables
	public static final String GROUP_ADDRESS = "room-clima";
	private IGroupAddress groupAddress = null;

	// message observer
	private final SpaceObserver<IFact> cfpObserver = new CFPObserver();
	private final SpaceObserver<IFact> acceptObserver = new AcceptObserver();
	private final SpaceObserver<IFact> rejectObserver = new RejectObserver();

	// states
	QualityOfService qualityService;
	double quality = 1;
	String provider = "FensterAgent";
	int busy = 0;
	private ArrayList<Task<WinService>> tasks = new ArrayList<Task<WinService>>();
	Task<WinService> currentTask = null;
	private Boolean winPos = null;

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
	}

	/**
	 * join group, get actions, attach observer
	 */
	@Override
	public void doStart() {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);
		
		this.getWinAction = this.retrieveAction(Window.ACTION_GET_WINDOW_STATE);
		this.setWinAction = this.retrieveAction(Window.ACTION_UPDATE_WINDOW_STATE);

		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);

		// attach observers
		this.memory.attach(cfpObserver, CFP);
		this.memory.attach(acceptObserver, accept);
		this.memory.attach(rejectObserver, reject);
	}

	@Override
	public void execute() {
		invoke(getWinAction, new Serializable[] {}, this);
		if (busy > 0) {
			--busy;
		} else if (currentTask != null){
			currentTask = null;
		}
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
			}
		}
	}

	final class CFPObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = -5114193061506471383L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					log.debug("CFP empfangen");
					JiacMessage message = (JiacMessage) object;
					if (message.getHeader(IJiacMessage.Header.SEND_TO).equals(
							GROUP_ADDRESS)) {
						CallForProposal cfp = (CallForProposal) message
								.getPayload();
						log.debug("CFP: " + cfp);
						if (cfp.getQuality() != null
								&& cfp.getTask() != null
								&& cfp.getTask().getJob() instanceof WinService
								&& cfp.getQuality().quality <= quality) {
							Task<WinService> task = (Task<WinService>) cfp
									.getTask();
							if (busy == 0) {
								WinService win = (WinService) task
										.getJob();
								// quality.heating is misused for winposition, 2 is open, 1 is closed
								qualityService = new QualityOfService(provider,
										(win.value ? 2 : 1), quality);
								Proposal prop = new Proposal(qualityService,
										thisAgent.getAgentDescription(), task);
								JiacMessage msg = new JiacMessage(prop);
								invoke(send,
										new Serializable[] {
												msg,
												task.getClient()
														.getMessageBoxAddress() });
								tasks.add(task);
								log.debug("Schicke Proposal für: "
										+ task.getId() + " " + prop);
							} else {
								Refuse refuse = new Refuse(
										thisAgent.getAgentDescription(),
										cfp.getTask());
								JiacMessage msg = new JiacMessage(refuse);
								invoke(send,
										new Serializable[] {
												msg,
												task.getClient()
														.getMessageBoxAddress() });
								if(tasks.contains(task)) {
									tasks.remove(task);
								}
							}
						}
					}
				}
			}
		}
	}

	final class AcceptObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = -8398201613021159704L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					AcceptProposal acc = (AcceptProposal) ((JiacMessage) object)
							.getPayload();
					log.debug("Accept empfangen: " + acc);
					if (acc.getTask() != null
							&& acc.getTask().getJob() instanceof WinService) {
						Task<WinService> task = (Task<WinService>) acc
								.getTask();
						if (busy == 0) {
							if(tasks.contains(task)) {
								tasks.remove(task);
								currentTask = task;
								boolean w = task.getJob().value;
								invoke(setWinAction, new Serializable[] { w }, FensterAgentBean.this);
							}
						} else {
							JiacMessage msg = new JiacMessage(new Failure(
									thisAgent.getAgentDescription(), task));
							invoke(send, new Serializable[] { msg,
									task.getClient().getMessageBoxAddress() });
							if(tasks.contains(task)) {
								tasks.remove(task);
							}
						}
					}
				}
			}
		}
	}

	final class RejectObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = 6745660754188485697L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					RejectProposal rej = (RejectProposal) ((JiacMessage) object)
							.getPayload();
					log.debug(rej);
					if(tasks.contains(rej.getTask())) {
						tasks.remove(rej.getTask());
					}
				}
			}
		}
	}
}