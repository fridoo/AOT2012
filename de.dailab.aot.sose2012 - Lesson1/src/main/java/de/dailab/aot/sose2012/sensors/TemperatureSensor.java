package de.dailab.aot.sose2012.sensors;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import de.dailab.aot.sose2012.effectors.Heating;
import de.dailab.aot.sose2012.ontology.Temperature;
import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * This is a agent bean that is used to simulate temperature changes between two steps. The current {@link Temperature}
 * will be written into agents memory.
 * 
 * @author mib
 * @version AOT SoSe 2011
 */
public final class TemperatureSensor extends AbstractAgentBean implements ResultReceiver {

	private static final String LOGGINGFILE = "temperatures.csv";
	private static final Temperature TPL = new Temperature();
	private static final Double INITIAL = Double.valueOf(16);
	private static final Double MAXIMAL = Double.valueOf(30);
	private static final Double MINIMAL = Double.valueOf(0.0);
	private Boolean stateWindow = Window.INITIAL;
	private Integer stateHeating = Heating.INITIAL;
	private TemperatureInformation frame = null;


	private final Comparator<Temperature> comparator = new Comparator<Temperature>() {
		@Override
		public int compare(final Temperature o1, final Temperature o2) {
			return Long.valueOf(o2.creationDate - o1.creationDate).intValue();
		}
	};

	@Override
	public void doInit() throws Exception {

		final File f = new File(TemperatureSensor.LOGGINGFILE);
		if (f.exists()) {
			f.delete();
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			fos.write("temperature;heating;window;growth\n".getBytes());
		}
		catch (final IOException ioe) {
			this.log.error("could not write into file '" + TemperatureSensor.LOGGINGFILE + "' (" + ioe.getClass().getSimpleName() + "): " + ioe.getMessage());
		}
		finally {
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}

	};


	@Override
	public void doStart() {
		this.frame = new TemperatureInformation();
		this.frame.updateHeating(this.stateHeating);
		this.frame.updateWindow(this.stateWindow);
		this.frame.updateTemperature(TemperatureSensor.INITIAL);
		this.frame.setVisible(true);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				// log.info("tick " + e.getID());
				if (TemperatureSensor.this.thisAgent.getAgentNode() instanceof SimpleAgentNode) {
					try {
						((SimpleAgentNode) TemperatureSensor.this.thisAgent.getAgentNode()).shutdown();
						System.exit(0);
					}
					catch (final LifecycleException lex) {
						TemperatureSensor.this.log.error("could not shutdown this agent node: " + lex.getMessage());
						System.exit(1);
					}
				}
			}
		});

	};

	@Override
	public void doStop() throws Exception {
		if (this.frame != null) {
			this.frame.dispose();
			this.frame = null;
		}
	}

	@Override
	public void execute() {

		final Action heating = this.retrieveAction(Heating.ACTION_GET_HEATING_STATE);
		if (heating != null) {
			this.invoke(heating, new Serializable[] {}, this);
		}
		final Action window = this.retrieveAction(Window.ACTION_GET_WINDOW_STATE);
		if (window != null) {
			this.invoke(window, new Serializable[] {}, this);
		}

		/*
		 * read all old Temperature values
		 */
		final Set<Temperature> set = this.memory.removeAll(TemperatureSensor.TPL);

		if (set.size() > 0) {
			/*
			 * create array
			 */
			Temperature[] array = new Temperature[set.size()];
			/*
			 * copy content
			 */
			array = set.toArray(array);
			/*
			 * sort content
			 */
			Arrays.sort(array, this.comparator);
			/*
			 * last element
			 */
			final Temperature last = array[array.length - 1];
			final Double base = Double.valueOf(last.getValue());
			/*
			 * computes new temperature
			 */
			final Integer w = this.stateWindow ? 2 : 1;
			final Double loss = -0.07 * w * (last.getValue() - TemperatureSensor.MINIMAL);
			final Double gain = 0.11 * this.stateHeating * (TemperatureSensor.MAXIMAL - last.getValue());

			final Double delta = Double.valueOf(loss + gain);
			final Double value = Double.valueOf(base + delta);

			this.frame.updateTemperature(value);
			final Temperature t = new Temperature(value);

			this.memory.write(t);

			try {
				this.logTemperature(value, this.stateHeating, this.stateWindow, delta);
			}
			catch (final IOException e) {
				if (this.log.isDebugEnabled()) {
					this.log.debug("could not write CVS file for logging temperatures (" + e.getClass().getSimpleName() + "): " + e.getMessage());
				}
			}

		}
		else {
			final Temperature t = new Temperature(TemperatureSensor.INITIAL);
			this.memory.write(t);
		}
	}

	@Override
	public void receiveResult(final ActionResult result) {

		final String name = result.getAction().getName();
		if (Heating.ACTION_GET_HEATING_STATE.equals(name)) {
			try {
				this.stateHeating = (Integer) result.getResults()[0];
				this.frame.updateHeating(this.stateHeating);
			}
			catch (final Exception e) {
				this.log.error("could not update heating state: " + e.getMessage());
			}
		}
		else if (Window.ACTION_GET_WINDOW_STATE.equals(name)) {
			try {
				this.stateWindow = (Boolean) result.getResults()[0];
				this.frame.updateWindow(this.stateWindow);
			}
			catch (final Exception e) {
				this.log.error("could not update window state: " + e.getMessage());
			}
		}
	}

	private void logTemperature(final Double temperature, final Integer heating, final Boolean window, final Double growth) throws IOException {

		final String line = String.format("%2.1f;%d;" + (window ? "1" : "0") + ";%1.3f%n", temperature, heating, growth);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(TemperatureSensor.LOGGINGFILE, true);
			fos.write(line.getBytes());
		}
		finally {
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}
	}


}
