package de.dailab.aot.sose2011.blackboard;

import de.dailab.jiactng.agentcore.AbstractAgentBean;

/**
 * This abstract agent bean grants access to the agent node's blackboard. 
 * 
 * @author mib
 * @version AOT SoSe 2011
 */
public abstract class BlackboardAgentBean extends AbstractAgentBean {

	protected Blackboard blackboard = null;

	@Override
	public void doInit() throws Exception {
		super.doInit();
		blackboard = thisAgent.getAgentNode().findAgentNodeBean(Blackboard.class);
		if (blackboard == null) {
			log.error("this agent has no access to the blackboard");
		}
		else {
			log.info("blackboard = " + blackboard.getBeanName());
		}
	}

	public final void setBlackboard(Blackboard blackboard) {
		this.blackboard = blackboard;
	}

	public final Blackboard getBlackboard() {
		return blackboard;
	}

}
