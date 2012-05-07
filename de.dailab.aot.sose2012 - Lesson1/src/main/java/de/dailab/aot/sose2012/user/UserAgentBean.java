package de.dailab.aot.sose2012.user;

import de.dailab.jiactng.agentcore.AbstractAgentBean;

/**
 * This is a stub for 'Arbeitsblatt 2'.
 * 
 * @author mib
 * @version AOT SoSe 2011
 */
public final class UserAgentBean extends AbstractAgentBean {

	public static final String CMD_WINDOW_OPEN = "Open Window";
	public static final String CMD_WINDOW_CLOSE = "Close Window";

	private WindowSwitch frame = null;

	@Override
	public void doStart() throws Exception {
		super.doStart();

		frame = new WindowSwitch(this);
		frame.setVisible(true);
	}

	@Override
	public void doStop() throws Exception {
		if (frame != null) {
			frame.dispose();
			frame = null;
		}
	}

	protected void windowSwitch(String cmd) {
		/*
		 * TODO put your code here ...
		 */
		log.warn("got command: " + cmd);

	}

}
