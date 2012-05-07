package de.dailab.aot.sose2012.sensors;

import java.util.Random;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;

/**
 * This is a software window component. A window of a room could be opened or it could be closed.
 * 
 * @author mib
 * @version AOT SoSe 2011
 */
public final class Window extends AbstractMethodExposingBean {

	/**
	 * Returns the current state of the 'window'. This action returns a boolean value. If the window is opened, the value
	 * is <code>true</code>, otherwise <code>false</code>.
	 * 
	 * @see #getWindowState()
	 */
	public static final String ACTION_GET_WINDOW_STATE = "Window.GetState";

	/**
	 * The initial state of the window is <code>false</code> = <i>closed</i>.
	 */
	public static final Boolean INITIAL = Boolean.FALSE;

	/**
	 * The internal state of the window. If the value equals <code>true</code>, this symbolises an opened window,
	 * otherwise the window is closed.
	 */
	private Boolean isOpen = INITIAL;

	private final Random r = new Random();


	/**
	 * This method returns the state of a window as a JIACv action.
	 * 
	 * @return <code>true</code> symbolises an opened window, <code>false</code> the window is closed.
	 * @see #ACTION_GET_WINDOW_STATE
	 */
	@Expose(name = ACTION_GET_WINDOW_STATE, scope = ActionScope.AGENT)
	public Boolean getWindowState() {
		return this.isOpen;
	}


	@Override
	public void execute() {
		/*
		 * diffuse window switches at random
		 */
		if (r.nextDouble() < 0.25D) {
			this.isOpen = !this.isOpen;
			if (isOpen) {
				log.info("human interaction: someone opens the window for fresh air");
			}
			else {
				log.info("human interaction: someone closes the window because of traffic noise");
			}
		}
	}

}
