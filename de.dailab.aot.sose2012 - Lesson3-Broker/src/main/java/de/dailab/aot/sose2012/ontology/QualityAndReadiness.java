package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * not used
 * @author frido
 *
 */
public class QualityAndReadiness implements IFact {

	private final boolean ready;
	private final QualityOfService qos;
	
	
	public QualityAndReadiness(boolean ready, QualityOfService qos) {
		this.ready = ready;
		this.qos = qos;
	}


	public boolean isReady() {
		return ready;
	}


	public QualityOfService getQos() {
		return qos;
	}
	
}
