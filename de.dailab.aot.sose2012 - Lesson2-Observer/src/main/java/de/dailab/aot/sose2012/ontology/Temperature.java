package de.dailab.aot.sose2012.ontology;

import java.util.Date;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public final class Temperature implements IFact, Comparable<Temperature> {

	private static final long serialVersionUID = -6262270993946739793L;
	/**
	 * The temperature value.
	 */
	private final Double value;
	/**
	 * Creation Date in milliseconds.
	 */
	public final Long creationDate;

	public Temperature() {
		value = null;
		creationDate = null;
	}

	public Temperature(final Double arg) {
		this.value = arg;
		creationDate = System.currentTimeMillis();

	}

	public Double getValue() {
		return value;
	}

	@Override
	public int compareTo(Temperature o) {
		return Long.valueOf(o.creationDate - this.creationDate).intValue();
	}

	@Override
	public String toString() {
		return "Temperature[" + String.format("%2.3f", value) + " degrees,date=" + new Date(creationDate).toString() + "]";
	}

}
