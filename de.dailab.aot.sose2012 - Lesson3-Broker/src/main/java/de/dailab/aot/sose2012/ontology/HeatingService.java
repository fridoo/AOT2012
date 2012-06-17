package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class HeatingService implements IFact {

	static final long serialVersionUID = -7676234354360120443L;

	/**
	 * A heating state delivered to the client.
	 */
	public final Integer heating;
	/**
	 * Duration of this heating service in cycles.
	 */
	public final Integer duration;
	/**
	 * creation date in milliseconds.
	 */
	public final Long creationDate;

	public HeatingService() {
		this.heating = null;
		this.duration = null;
		this.creationDate = null;
	}

	public HeatingService(Integer state, Integer duration) {
		this.heating = state;
		this.duration = duration;
		this.creationDate = System.currentTimeMillis();
	}

}
