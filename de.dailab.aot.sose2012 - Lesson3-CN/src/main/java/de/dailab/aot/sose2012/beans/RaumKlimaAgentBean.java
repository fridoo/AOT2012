package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2012.ontology.AcceptProposal;
import de.dailab.aot.sose2012.ontology.Failure;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.Proposal;
import de.dailab.aot.sose2012.ontology.CallForProposal;
import de.dailab.aot.sose2012.ontology.HeatingService;
import de.dailab.aot.sose2012.ontology.QualityOfService;
import de.dailab.aot.sose2012.ontology.Refuse;
import de.dailab.aot.sose2012.ontology.Task;
import de.dailab.aot.sose2012.ontology.Temperature;
import de.dailab.aot.sose2012.ontology.WinService;
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
 * RaumKlimaAgentBean is the main class for RaumKlimaAgent. It computes the best
 * heating-state in order to maintain a temperature near 21°C and sends its
 * request to HeizungsAgent.
 * 
 * @author Mitch
 * 
 */
public class RaumKlimaAgentBean extends AbstractAgentBean implements
		ResultReceiver {

	// agent templates
	private static final Temperature tempTpl = new Temperature();
	private static final JiacMessage infTpl = new JiacMessage(new Inform());
	private static final JiacMessage proposalTpl = new JiacMessage(
			new Proposal());
	private static final JiacMessage refuseTpl = new JiacMessage(new Refuse());
	private static final JiacMessage failureTpl = new JiacMessage(new Failure());

	// used actions
	private IActionDescription send;

	// group variables
	public static final String GROUP_ADDRESS = "room-clima";
	private IGroupAddress groupAddress = null;

	// states
	private final int TEMP_TO_ACHIVE = 21;
	private int windowPos;
	private int heating;
	private Temperature currentTemp = null;
	private PriorityQueue<Temperature> temperatures = new PriorityQueue<Temperature>();
	private HeatingService heat = new HeatingService();
	private CallForProposal cfp = new CallForProposal();
	private int taskCount = 0;
	private PriorityQueue<Proposal> proposals = new PriorityQueue<Proposal>(10,
			new ProposalComparator());
	private int deadlineForCFP = 500;
	private long nextDeadLine = System.currentTimeMillis();
	private Task<HeatingService> currentTask = null;
	boolean failed = false;

	// message observer
	private final SpaceObserver<IFact> tempObserver = new TemperatureObserver();
	private final SpaceObserver<IFact> infObserver = new InformObserver();
	private final SpaceObserver<IFact> propObserver = new ProposalObserver();
	private final SpaceObserver<IFact> refObserver = new RefuseObserver();
	private final SpaceObserver<IFact> failObserver = new FailureObserver();

	@Override
	public void doInit() {
		this.groupAddress = CommunicationAddressFactory
				.createGroupAddress(GROUP_ADDRESS);
		this.windowPos = 1;
		this.heating = 0;
	}

	/**
	 * join group, get actions, attach observer
	 */
	@Override
	public void doStart() {
		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);

		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress, }, this);

		this.memory.attach(tempObserver, tempTpl);
		this.memory.attach(infObserver, infTpl);
		this.memory.attach(propObserver, proposalTpl);
		this.memory.attach(refObserver, refuseTpl);
		this.memory.attach(failObserver, failureTpl);
	}

	/**
	 * compute heating-state and send a request to Heizungsagent
	 */
	@Override
	public void execute() {
		if (System.currentTimeMillis() > nextDeadLine) {
			if (!proposals.isEmpty()) {
				log.debug(proposals.size() + " proposals empfangen");
				Proposal prop = proposals.poll();
				AcceptProposal a = new AcceptProposal(prop.getReferencedTask());
				JiacMessage accept = new JiacMessage(a);
				invoke(send, new Serializable[] { accept,
						prop.getProposer().getMessageBoxAddress() });
				proposals.clear();
				currentTask = null;
				this.heating = prop.quality.heating;
				log.debug("Accept geschickt: " + a + " an: " + prop.getProposer());
			} else {
				currentTask = null;
				this.heating = 0;
				log.debug("Keine Proposals empfangen");
			}
		}
		if (!temperatures.isEmpty()) {
			currentTemp = temperatures.peek();
		}
		if (currentTemp == null ) {
			return;
		}
		temperatures.clear();
		if (failed) {
			failed = false;
			this.heating = 0;
		}
		double nextTemp = calcNextTemperature(this.heating, this.windowPos,
				currentTemp.getValue());
		double newHeating = (TEMP_TO_ACHIVE + 0.07 * this.windowPos
				* currentTemp.getValue() - currentTemp.getValue())
				/ (0.11 * (30 - currentTemp.getValue()));
		if (newHeating > 6) {
			heating = 6;
		} else if (newHeating < 0) {
			heating = 0;
		} else {
			if (newHeating % 1 < 0.5) {
				heating = (int) newHeating;
			} else {
				heating = (int) (newHeating + 1);
			}
		}
		double nextnextTemp = calcNextTemperature(this.heating, this.windowPos,
				nextTemp);
		log.debug("current " + currentTemp.getValue());
		log.debug("Heating is next set to " + heating);
		log.debug("Next temp should be " + nextTemp);
		log.debug("NextNext temp should be " + nextnextTemp);

		heat = new HeatingService(heating, 1);
		currentTask = new Task<HeatingService>("t-" + taskCount, heat,
				thisAgent.getAgentDescription());
		callForProposals(new Task<HeatingService>("t-" + taskCount, heat,
				thisAgent.getAgentDescription()), (long) deadlineForCFP);
		if (taskCount == Integer.MAX_VALUE) {
			taskCount = 0;
		} else {
			taskCount++;
		}
		log.debug("Schicke neues CFP: " + currentTask);
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
			this.log.error("could not leave room-clima: " + result.getFailure());
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
	 * 
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
	 * looks for information about temperature, window-position (and updates the
	 * respective states) and receives rejects.
	 * 
	 * @author Mitch
	 * 
	 */
	final class TemperatureObserver implements SpaceObserver<IFact> {
		private static final long serialVersionUID = 1029198682347250319L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof Temperature) {
					Temperature t = (Temperature) object;
					temperatures.add(t);
					log.debug("Temperature added: " + t);
				}
			}
		}
	}

	final class ProposalObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = -4712056017333426842L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					Proposal prop = (Proposal) ((JiacMessage) object)
							.getPayload();
					proposals.add(prop);
				}
			}
		}
	}

	final class RefuseObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = 3288266214466079670L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					Refuse ref = (Refuse) ((JiacMessage) object).getPayload();
					log.debug(ref.getProposer().getName()
							+ " hat abgelehnt die Task "
							+ ref.getReferencedTask().toString()
							+ " zu übernehmen.");
				}
			}
		}
	}

	final class FailureObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = 2952306745915919961L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					failed = true;
					Failure fail = (Failure) ((JiacMessage) object)
							.getPayload();
					log.debug(fail.getProposer().getName()
							+ " failte bei der Ausführung von "
							+ fail.getReferencedTask().toString() + " weil: " + fail.getException());
				}
			}
		}
	}

	final class InformObserver implements SpaceObserver<IFact> {

		private static final long serialVersionUID = 2952306745915919961L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					Inform inf = (Inform) ((JiacMessage) object).getPayload();
					if (inf.getValue() instanceof WinService) {
						WinService w = (WinService) inf.getValue();
						setWindowPos(w.getValue());
						log.debug("Winservice empfangen: " + w);
					} else if (inf.getValue() instanceof HeatingService) {
						HeatingService heat = (HeatingService) inf.getValue();
						// create a new Service with a fresh creationdate so
						// former dates will not matter anymore
						HeatingService newHeat = new HeatingService(
								heat.heating, heat.duration);
						memory.write(newHeat);
						log.debug("Heatservice empfangen und ins Memory geschrieben: "
								+ newHeat);
					}
				}
			}
		}
	}

	private void callForProposals(Task<HeatingService> task, long deadline) {
		log.info("Calling for Proposals for: " + task.toString());
		cfp = new CallForProposal(task,
				new QualityOfService(null, heating, 0.0));
		JiacMessage message = new JiacMessage(cfp);
		invoke(send, new Serializable[] { message, groupAddress });

		this.nextDeadLine = System.currentTimeMillis() + deadline;
	}
	
	class ProposalComparator implements Comparator<Proposal> {

		@Override
		public int compare(Proposal o1, Proposal o2) {
//			log.debug("compare 1: " + o1 + " mit 2: " + o2 + " Ergebnis: " + (int) Math.signum((((HeatingService) o1.getReferencedTask().getJob()).heating -
//					o1.quality.heating*o1.quality.quality)
//					- (((HeatingService) o2.getReferencedTask().getJob()).heating -
//							o2.quality.heating*o2.quality.quality)) + " Rechnung " + "(" + ((HeatingService) o1.getReferencedTask().getJob()).heating + "-"
//							+o1.quality.heating+"*"+o1.quality.quality+")-("+((HeatingService) o2.getReferencedTask().getJob()).heating+"-"+o2.quality.heating+"*"+o2.quality.quality+")" );
			return (int) Math.signum((((HeatingService) o1.getReferencedTask().getJob()).heating -
					o1.quality.heating*o1.quality.quality)
					- (((HeatingService) o2.getReferencedTask().getJob()).heating -
							o2.quality.heating*o2.quality.quality));
		}
		
	}
}
