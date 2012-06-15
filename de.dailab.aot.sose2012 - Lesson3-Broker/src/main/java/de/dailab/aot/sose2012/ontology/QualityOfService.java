package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class QualityOfService implements IFact {

	private static final long serialVersionUID = 7354794080449651668L;
	/**
	 * A provider description
	 */
	public final String provider;
	
	public final IAgentDescription providerIAD;
	/**
	 * A heating state, if the provider delivers its service.
	 */
	public final Integer heating;
	/**
	 * The quality of the service. A value of 0.0 is very low. The service
	 * quality gains proportional with this value.
	 */
	public final Double quality;
	/**
	 * true if the provider is ready to deliver its service
	 */
	public final boolean ready;

	public QualityOfService() {
		this(null, null, null, null, false);
	}

	public QualityOfService(String provider, IAgentDescription providerIAD, Integer state, Double level, boolean readyness) {
		this.provider = provider;
		this.providerIAD = providerIAD;
		this.heating = state;
		this.quality = level;
		this.ready = readyness;
	}

	@Override
	public String toString() {
		return "QualityOfService[provider=" + provider + ", heating=" + heating + ", quality="
				+ String.format("%1.2f", quality) + "]";
	}

}
