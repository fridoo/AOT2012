package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.ArrayList;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;
import de.dailab.aot.sose2012.ontology.AcceptProposal;
import de.dailab.aot.sose2012.ontology.CallForProposal;
import de.dailab.aot.sose2012.ontology.Failure;
import de.dailab.aot.sose2012.ontology.HeatingService;
import de.dailab.aot.sose2012.ontology.Proposal;
import de.dailab.aot.sose2012.ontology.QualityOfService;
import de.dailab.aot.sose2012.ontology.Refuse;
import de.dailab.aot.sose2012.ontology.RejectProposal;
import de.dailab.aot.sose2012.ontology.Task;
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

/**
 * HeizungsAgentBean is the main class for HeizungsAgent. It receives
 * heating-state changerequests from other agents and fulfills them if they're
 * valid, or rejects them.
 * 
 * @author Mitch
 * 
 */
public class HeizungsAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// used actions
	private IActionDescription send;

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

	//states set in xml
	double quality = 0.0;
	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}

	String provider = "";
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	int range = 5;
	
	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	// states
	QualityOfService qualityService;
	int busy = 0;
	private ArrayList<Task<HeatingService>> tasks = new ArrayList<Task<HeatingService>>();
	Task<HeatingService> currentTask = null;

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
	}

	/**
	 * join group, get actions, attach observer, set reject msg
	 */
	@Override
	public void doStart() {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);

		// attach observers
		this.memory.attach(cfpObserver, CFP);
		this.memory.attach(acceptObserver, accept);
		this.memory.attach(rejectObserver, reject);
	}

	@Override
	public void execute() {
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
		}
	}

	/**
	 * looks for requests to set the heating (between 0 and 5)
	 * 
	 * @author Mitch
	 * 
	 */
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
								&& cfp.getTask().getJob() instanceof HeatingService
								&& cfp.getQuality().quality <= quality) {
							Task<HeatingService> task = (Task<HeatingService>) cfp.getTask();
							if (busy == 0) {
								HeatingService heat = (HeatingService) task.getJob();
								if (heat.heating > range) {
									HeatingService newHeat = new HeatingService(range, heat.duration);
									heat = newHeat;
									task.setJob(heat);
								}
								qualityService = new QualityOfService(provider,
										heat.heating, quality);
								Proposal prop = new Proposal(qualityService,
										thisAgent.getAgentDescription(),
										task);
								JiacMessage msg = new JiacMessage(prop);
								invoke(send, new Serializable[] {
										msg,
										task.getClient()
												.getMessageBoxAddress() });
								tasks.add(task);
								log.debug("Schicke Proposal für: " + task.getId() + " " + prop);
							} else {
								Refuse refuse = new Refuse(
										thisAgent.getAgentDescription(),
										cfp.getTask());
								JiacMessage msg = new JiacMessage(refuse);
								invoke(send, new Serializable[] {
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
					if (acc.getTask() != null && acc.getTask().getJob() instanceof HeatingService) {
						Task<HeatingService> task = (Task<HeatingService>) acc.getTask();
						if(busy == 0) {
							if(tasks.contains(task)) {
								currentTask = task;
								busy = task.getJob().duration*2;
								JiacMessage msg = new JiacMessage(task.getJob());
								invoke(send, new Serializable[] {
									msg,
									task.getClient()
									.getMessageBoxAddress() });
								log.debug("HeatService geschickt: " + task.getJob() + " an " + task.getClient().getName());
								tasks.remove(task);
							}
						} else {
							JiacMessage msg = new JiacMessage(new Failure(thisAgent.getAgentDescription(), task));
							invoke(send, new Serializable[] {
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

