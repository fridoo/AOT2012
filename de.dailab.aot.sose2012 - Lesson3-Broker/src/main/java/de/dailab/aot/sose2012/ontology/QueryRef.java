package de.dailab.aot.sose2012.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class QueryRef<T> implements IFact {

	/**
	 * 
	 */
	private static final long serialVersionUID = 150452826659047769L;

	private final IAgentDescription senderID;
	private final T informAbout;
	private final int queryID;
	
	public QueryRef() {
		this.senderID = null;
		this.informAbout = null;
		this.queryID = -1;
	}
	
	public QueryRef(IAgentDescription senderID, T informAbout, int queryID ) {
		this.senderID = senderID;
		this.informAbout = informAbout;
		this.queryID = queryID;
	}

	public IAgentDescription getSenderID() {
		return senderID;
	}

	public T getInformAbout() {
		return informAbout;
	}

	public int getQueryID() {
		return queryID;
	}
	
	
}
