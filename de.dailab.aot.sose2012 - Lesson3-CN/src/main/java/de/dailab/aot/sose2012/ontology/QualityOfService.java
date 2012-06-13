package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class QualityOfService implements IFact {

	private static final long serialVersionUID = 7354794080449651668L;
	/**
	 * A provider description
	 */
	public final String provider;
	/**
	 * A heating state, if the provider delivers its service.
	 */
	public final Integer heating;
	/**
	 * The quality of the service. A value of 0.0 is very low. The service
	 * quality gains proportional with this value.
	 */
	public final Double quality;

	public QualityOfService() {
		this(null, null, null);
	}

	public QualityOfService(String provider, Integer state, Double level) {
		this.provider = provider;
		this.heating = state;
		this.quality = level;
	}

	@Override
	public String toString() {
		return "QualityOfService[provider=" + provider + ", heating=" + heating + ", quality="
				+ String.format("%1.2f", quality) + "]";
	}

}
