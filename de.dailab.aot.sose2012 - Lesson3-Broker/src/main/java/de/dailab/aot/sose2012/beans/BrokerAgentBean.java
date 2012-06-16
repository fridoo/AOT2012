package de.dailab.aot.sose2012.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.aot.sose2012.ontology.Agree;
import de.dailab.aot.sose2012.ontology.Failure;
import de.dailab.aot.sose2012.ontology.FailureProxy;
import de.dailab.aot.sose2012.ontology.HeatingService;
import de.dailab.aot.sose2012.ontology.Inform;
import de.dailab.aot.sose2012.ontology.InformDoneProxy;
import de.dailab.aot.sose2012.ontology.Proxy;
import de.dailab.aot.sose2012.ontology.QualityOfService;
import de.dailab.aot.sose2012.ontology.QueryRef;
import de.dailab.aot.sose2012.ontology.Refuse;
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
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class BrokerAgentBean extends AbstractAgentBean implements
		ResultReceiver {
	
	// used actions
	private IActionDescription send;
	
	// group variables
	public static final String GROUP_ADDRESS = "room-clima";
	private IGroupAddress groupAddress = null;
	
	// msg template
	private final static JiacMessage INF = new JiacMessage(new Inform<Object>());
	private final static JiacMessage REFUSE = new JiacMessage(new Refuse<Object>());
	private final static JiacMessage AGREE = new JiacMessage(new Agree<Object>());
	private final static JiacMessage FAILURE = new JiacMessage(new Failure<Object>());
	private final static JiacMessage PROXY = new JiacMessage(new Proxy());
	
	// message observer
	private final SpaceObserver<IFact> observerINF = new InformObserver();
	private final SpaceObserver<IFact> observerREFUSE = new RefuseObserver();
	private final SpaceObserver<IFact> observerAGREE = new AgreeObserver();
	private final SpaceObserver<IFact> observerFAILURE = new FailureObserver();
	private final SpaceObserver<IFact> observerPROXY = new ProxyObserver();
	
	AgentTaskManager currentTask = null;

	@Override
	public void doInit() throws Exception {
		this.groupAddress = CommunicationAddressFactory.createGroupAddress(GROUP_ADDRESS);
	}

	@Override
	public void doStart() throws Exception {
		Action join = this.retrieveAction(ICommunicationBean.ACTION_JOIN_GROUP);
		this.invoke(join, new Serializable[] { groupAddress }, this);

		send = this.retrieveAction(ICommunicationBean.ACTION_SEND);
		
		this.memory.attach(observerINF, INF);
		this.memory.attach(observerREFUSE, REFUSE);
		this.memory.attach(observerAGREE, AGREE);
		this.memory.attach(observerFAILURE, FAILURE);
		this.memory.attach(observerPROXY, PROXY);
		
	}

	@Override
	public void doStop() throws Exception {
		Action leave = this.retrieveAction(ICommunicationBean.ACTION_LEAVE_GROUP);
		ActionResult result = this.invokeAndWaitForResult(leave,
				new Serializable[] { groupAddress });
		if (result.getFailure() != null) {
			this.log.error("could not leave temp-group: " + result.getFailure());
		}
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		super.execute();
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
	
	final class ProxyObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8138712861238378764L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					Proxy proxy = (Proxy) ((JiacMessage) object).getPayload(); // extract proxy from message
					if (proxy.getMessage() instanceof Request<?>) {
						Request<Object> req = (Request<Object>) proxy.getMessage(); // extract request from proxy
						if (req.getValue() instanceof HeatingService) {
							// send agree
							Agree<HeatingService> agree = new Agree<HeatingService>(thisAgent.getAgentDescription(), 
									(HeatingService)req.getValue());
							JiacMessage agreeMsg = new JiacMessage(agree);
							invoke(send, new Serializable[] { agreeMsg, req.getSenderID().getMessageBoxAddress() });
							
							// create new AgentTaskManager for this request
							HeatingService hsToDo = (HeatingService) req.getValue(); // extract HeatingService from request
							currentTask = new AgentTaskManager(req.getRequestID(), hsToDo, req.getSenderID());
							
							// send query-ref for QualityOfService over the group channel
							QueryRef<QualityOfService> query = new QueryRef<QualityOfService>(
									thisAgent.getAgentDescription(), new QualityOfService(), req.getRequestID());
							JiacMessage queryMsg = new JiacMessage(query);
							invoke(send, new Serializable[] { queryMsg, BrokerAgentBean.this.groupAddress });
						} else {
							// send refuse
							Refuse<HeatingService> refuse = new Refuse<HeatingService>(
									thisAgent.getAgentDescription(), (HeatingService)req.getValue());
							JiacMessage refuseMsg = new JiacMessage(refuse);
							invoke(send, new Serializable[] { refuseMsg, req.getSenderID().getMessageBoxAddress() });
						}
					}
				}
			}
		}
	}
	
	final class InformObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7429494904157842669L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					Inform<Object> inf = (Inform<Object>) ((JiacMessage) object).getPayload();
					if (inf.getValue() instanceof QualityOfService) {
						// broker received a quality of service notification from Heizungsagent
						if (currentTask != null) {
							if (inf.getInReplyToID() == currentTask.getTaskID()) {
								QualityOfService qos = (QualityOfService) inf.getValue();
								currentTask.addQOS(qos);
								if (currentTask.full()) { // when the 4th Agent
															// replied check for
															// best suitable
															// agent
									IAgentDescription bestAgent = currentTask.getBestAgentForTask();
									log.debug("Broker: hat den besten Heizungsagent gewählt: " + bestAgent.getName());
									// send request for HeatingService to the best agent
									Request<HeatingService> req = new Request<HeatingService>(
											currentTask.getSetHeatingTo(),
											thisAgent.getAgentDescription(),
											currentTask.getTaskID());
									JiacMessage reqMsg = new JiacMessage(req);
									invoke(send, new Serializable[] { reqMsg,
											bestAgent.getMessageBoxAddress() });

									// send inform-done-proxy to Raumklimaagent
									InformDoneProxy<Integer> idp = new InformDoneProxy<Integer>(
											thisAgent.getAgentDescription(),
											currentTask.getTaskID());
									JiacMessage idpMsg = new JiacMessage(idp);
									invoke(send, new Serializable[] {
											idpMsg,
											currentTask.getClient().getMessageBoxAddress() });
								}
							} else {
								log.debug("Broker: Inform QOS hat nicht die ID des currentTask");
							}
						} else {
							log.debug("Broker: Inform QOS currentTask == null");
						}
					} else if (inf.getValue() instanceof HeatingService) {
						// TODO
						if (currentTask != null) {
							if (inf.getInReplyToID() == currentTask.getTaskID()) {
								// send HeatingService to Raumklimaagent
								HeatingService hs = (HeatingService) inf.getValue();
								Inform<HeatingService> informHS = new Inform<HeatingService>(hs, 
										thisAgent.getAgentDescription(), inf.getInReplyToID());
								JiacMessage infMsg = new JiacMessage(informHS);
								invoke(send, new Serializable[] {
										infMsg, currentTask.getClient().getMessageBoxAddress() });
							} else {
								log.debug("Broker: Inform HS hat nicht die ID von currentTask");
							}
						} else {
							log.debug("Broker: Inform HS currentTask == null");
						}
						
					}
				}
			}
		}
	}

	final class AgreeObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4763457929220578956L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle Agree from Heizungsagent
					@SuppressWarnings("unchecked")
					Agree<Object> agree =  (Agree<Object>) ((JiacMessage)object).getPayload();
					log.debug("Broker hat Agree von " + agree.getSenderID().getName() + " erhalten");
				}
			}
		}
	}

	final class RefuseObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3288266214466079670L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle Refuse from Heizungsagent
					//send failure proxy
					@SuppressWarnings("unchecked")
					Refuse<Object> refuse = (Refuse<Object>) ((JiacMessage)object).getPayload();
					if (refuse.getReferencedTask() instanceof HeatingService) {
						FailureProxy<HeatingService> failProxy = new FailureProxy<HeatingService>(thisAgent.getAgentDescription(),
								(HeatingService) refuse.getReferencedTask(), "received a refuse from Heizungsagent");
						JiacMessage failProxyMsg = new JiacMessage(failProxy);
						invoke(send, new Serializable[] { failProxyMsg, currentTask.getClient().getMessageBoxAddress() });
					}
					log.debug("Broker hat Refuse von " + refuse.getProposer().getName() + " erhalten");
				}
			}
		}
	}
	
	final class FailureObserver implements SpaceObserver<IFact> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3288266214466079670L;

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {

			if (event instanceof WriteCallEvent) {
				@SuppressWarnings("rawtypes")
				Object object = ((WriteCallEvent) event).getObject();
				if (object instanceof JiacMessage) {
					// handle Failure from Heizungsagent
					// send failure to Raumklimaagent
					@SuppressWarnings("unchecked")
					Failure<Object> fail =  (Failure<Object>) ((JiacMessage)object).getPayload();
					if (fail.getReferencedTask() instanceof HeatingService) {
						Failure<HeatingService> failOwn = new Failure<HeatingService>(thisAgent.getAgentDescription(),
								(HeatingService) fail.getReferencedTask(), fail.getException());
						JiacMessage failMsg = new JiacMessage(failOwn);
						invoke(send, new Serializable[] { failMsg, currentTask.getClient().getMessageBoxAddress() });
					}
					log.debug("Broker hat Failure von " + fail.getProposer().getName() + " erhalten");
				}
			}
		}
	}
	
	class AgentTaskManager {
		
		private int taskID;
		private IAgentDescription client;
		private HeatingService setHeatingTo;
		private ArrayList<QualityOfService> qosList;
		
		public AgentTaskManager(int taskID, HeatingService setHeatingTo, IAgentDescription client) {
			this.taskID = taskID;
			this.setHeatingTo = setHeatingTo;
			this.client = client;
			this.qosList = new ArrayList<QualityOfService>(4);
		}
		
		public void addQOS(QualityOfService qos) {
			this.qosList.add(qos);
		}
		
		public boolean full() {
			return this.qosList.size() == 4;
		}
		
		public IAgentDescription getBestAgentForTask() {
			Iterator<QualityOfService> iter = qosList.iterator();
			while (iter.hasNext()) {
				QualityOfService qos = iter.next();
				// remove all qos whos Agent aren't ready or can't deliver the desired heating state
				if (!qos.ready || qos.heating < this.setHeatingTo.heating) {
					iter.remove();
				}
			}
			
			if (qosList.isEmpty()) {
				return null;
			} else {
				iter = qosList.iterator(); // get new iterator
				// determine the QualityOfService the the maximum quality
				QualityOfService maxQOS = qosList.get(0);
				while(iter.hasNext()) {
					QualityOfService currQOS = iter.next();
					if (currQOS.quality > maxQOS.quality) {
						maxQOS = currQOS;
					}
				}
				return maxQOS.providerIAD;
			}
			
		}

		public int getTaskID() {
			return taskID;
		}

		public HeatingService getSetHeatingTo() {
			return setHeatingTo;
		}

		public IAgentDescription getClient() {
			return client;
		}
		
		
		
	}

}
