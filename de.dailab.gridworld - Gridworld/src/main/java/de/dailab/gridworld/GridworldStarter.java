package de.dailab.gridworld;

import de.dailab.jiactng.agentcore.SimpleAgentNode;

public final class GridworldStarter {
	
	private GridworldStarter() {
		/*
		 * hide me
		 */
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleAgentNode.main(new String[]{"classpath:MatchPlatform.xml"});
	}

}
