package de.dailab.aot.sose2012.effectors;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;

/**
 * This is a software heating component. This heating is discrete. Its internal state has a value range from 0 .. 5,
 * heating is off to heating maximum effect.
 * 
 * @author mib
 * @version SoSe 2011
 */
public final class Heating extends AbstractMethodExposingBean {

	public static final Integer INITIAL = 0;
	public static final Integer MIN_STATE = 0;
	public static final Integer MAX_STATE = 5;

	/**
	 * Returns the current state of the 'heating'. This action returns an integer value between 0 and 5.
	 */
	public static final String ACTION_GET_HEATING_STATE = "Heating.GetState";

	/**
	 * This action takes effect to the representing heating component. This action has one parameter, it is an integer
	 * value between 0 and 5. The integer symbolises the effectiveness of the heating, 0 equals &quot;heating is
	 * off&quot; and a value of 5 switches the heating to maximum power.
	 */
	public static final String ACTION_UPDATE_STATE = "Heating.UpdateState";

	private Integer state = INITIAL;

	@Expose(name = ACTION_GET_HEATING_STATE, scope = ActionScope.AGENT)
	public Integer getHeatingState() {
		return this.state;
	}

	@Expose(name = ACTION_UPDATE_STATE, scope = ActionScope.AGENT)
	public void updateState(Integer newState) throws Exception {
		
		if (newState == null) {
			throw new IllegalArgumentException("Could not update internal state to 'null'");
		}
		else if (newState < MIN_STATE || newState > MAX_STATE) {
			throw new IllegalArgumentException("new value is out of range");
		}
		this.state = newState;
	}

}
