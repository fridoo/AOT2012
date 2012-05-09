package de.dailab.aot.sose2012.bean;

import java.io.Serializable;

import de.dailab.aot.sose2012.effectors.Heating;
import de.dailab.aot.sose2012.ontology.Temperature;
import de.dailab.aot.sose2012.sensors.Window;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class SimpleReactiveBean extends AbstractAgentBean implements
		ResultReceiver {

	private IActionDescription getHeatAction;
	private IActionDescription setHeatAction;
	private IActionDescription getWinAction;
	
	private final int TEMP_TO_ACHIVE = 21;
	private int windowPos;
	private int heating;
	private int counter = 0;
	
	@Override
	public void doInit() throws Exception {
		this.windowPos = 1;
		this.heating = 0;
	}

	@Override
	public void doStart() throws Exception {
		this.getHeatAction = thisAgent.searchAction(new Action(
				Heating.ACTION_GET_HEATING_STATE));
		this.setHeatAction = thisAgent.searchAction(new Action(
				Heating.ACTION_UPDATE_STATE));
		this.getWinAction = thisAgent.searchAction(new Action(
				Window.ACTION_GET_WINDOW_STATE));
	}

	@Override
	public void execute() {
		System.out.println("vvv iteration "+ (counter++) + " vvv");
		Temperature currentTemp = null;

		this.invoke(getWinAction, new Serializable[] {}, this);
		this.invoke(getHeatAction, new Serializable[] {}, this);
		currentTemp = memory.read(new Temperature());
		
		double nextTemp = calcNextTemperature(this.heating, this.windowPos, currentTemp.getValue());
		double newHeating = (TEMP_TO_ACHIVE + 0.07 * this.windowPos * nextTemp - nextTemp)
				/ (0.11 * (30 - nextTemp));
		log.debug("current " + currentTemp.getValue());
		log.debug("Next temp without adjustment " + nextTemp);
		log.debug("Heating is now set to: " + this.heating);
		log.debug("Heating would be " + newHeating);

		double tempAfterAdjustment;
		int setHeatingTo;
		if (newHeating > 5) {
			setHeatingTo = 5;
		} else if (newHeating < 0) {
			setHeatingTo = 0;
		} else {
			if (newHeating % 1 < 0.5) {
				setHeatingTo = (int) newHeating;
			} else {
				setHeatingTo = (int) (newHeating + 1);
			}
		}
		tempAfterAdjustment = calcNextTemperature(setHeatingTo, this.windowPos, currentTemp.getValue());
		log.debug("Heating is next set to " + setHeatingTo);
		log.debug("Nextnext temp should be " + tempAfterAdjustment);
		this.invoke(setHeatAction, new Serializable[] { setHeatingTo }, this);
		
		System.out.println("^^^^^^^^^^^^^^^^^^^^");
	}

	@Override
	public void receiveResult(ActionResult result) {
		// Action-Namen ins Debug-Log schreiben
		log.debug("Action result received: " + result.getAction().getName() + " iter: " + counter);

		// welche Aktion wurde aufgerufen?
		if (Window.ACTION_GET_WINDOW_STATE.equals(result.getAction().getName())) {
			if (result.getFailure() == null) {	
				Boolean w = (Boolean) result.getResults()[0];
				log.debug("Window open = " + w);
				this.setWindowPos(w);
			}
		}

		if (Heating.ACTION_GET_HEATING_STATE.equals(result.getAction()
				.getName())) {
			if (result.getFailure() == null) {				
				Integer h = (Integer) result.getResults()[0];
				log.debug("Heating state = " + h);
				this.heating = h;
			}
		}
	}
	
	private void setWindowPos(boolean w) {
		this.windowPos = w ? 2 : 1;
	}
	
	private double calcNextTemperature(int heating, int windowPos, double currentTemp) {
		return ( 0.11 * heating * (30 - currentTemp)
				- 0.07 * windowPos * currentTemp
				+ currentTemp );
	}

}
