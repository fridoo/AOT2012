package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Task<T> implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5771053190445558706L;
	private final String id;
	private T job;
	private IAgentDescription client;
	private volatile int hashCode = 0;

	public Task() {
		this(null, null, null);
	}

	public Task(String id, T job, IAgentDescription client) {
		this.id = id;
		this.job = job;
		this.client = client;
	}

	public String getId() {
		return id;
	}

	public T getJob() {
		return job;
	}

	public void setClient(IAgentDescription client) {
		this.client = client;
	}

	public IAgentDescription getClient() {
		return client;
	}

	@Override
	public String toString() {
		return "Task(id=" + id +") Job:" + job + " client " + client.getName();
	}
	
	public void setJob(T job) {
		this.job = job;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) return true;
		if ( !(obj instanceof Task) ) return false;
		Task<?> t = (Task<?>) obj;
		return t.id.equals(this.id);
	}
	
	@Override
	public int hashCode () {
        final int multiplier = 23;
        if (hashCode == 0) {
            int code = 133;
            code = multiplier * code + id.hashCode();
            hashCode = code;
        }
        return hashCode;
    }

}
