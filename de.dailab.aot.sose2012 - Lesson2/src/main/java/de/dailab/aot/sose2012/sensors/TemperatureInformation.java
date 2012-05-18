package de.dailab.aot.sose2012.sensors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public final class TemperatureInformation extends JFrame {

	private static final long serialVersionUID = 8526496035200921500L;

	private static final Color RED = new Color(192, 0, 0);
	private static final Color GREEN = new Color(0, 192, 32);
	private static final Color GREY = new Color(96, 96, 96);
	private static final Color BLUE = new Color(0, 64, 192);
	private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);

	private JLabel temperature = null;
	private JLabel heating = null;
	private JLabel window = null;

	protected TemperatureInformation() {
		this.setTitle("Sensoren");
		this.setSize(400, 300);
		this.init();
	}

	private void init() {

		JPanel grid = new JPanel(new GridLayout(3, 2, 8, 8));
		grid.setBorder(BORDER);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		temperature = new JLabel("16 °C");
		temperature.setForeground(GREY);
		heating = new JLabel("0");
		window = new JLabel("offen");

		JLabel t = new JLabel("Temperatur");
		t.setBorder(BORDER);
		JLabel h = new JLabel("Heizung");
		h.setBorder(BORDER);
		JLabel f = new JLabel("Fenster");
		f.setBorder(BORDER);

		grid.add(t);
		grid.add(temperature);
		grid.add(h);
		grid.add(heating);
		grid.add(f);
		grid.add(window);

		JPanel parent = new JPanel(new BorderLayout());
		parent.setBorder(BORDER);


		this.add(grid);

		this.pack();
	}

	protected void updateTemperature(Double value) {
		if (value < 16.0D || value > 25.0D) {
			temperature.setForeground(RED);
		}
		else if (value > 20.0D && value < 22.0D) {
			temperature.setForeground(GREEN);
		}
		else {
			temperature.setForeground(GREY);
		}
		this.temperature.setText(String.format("%2.1f °C", value));
	}

	protected void updateHeating(Integer value) {
		if (value >= 4) {
			heating.setForeground(RED);
		}
		else if (value > 1) {
			heating.setForeground(GREEN);
		}
		else {
			heating.setForeground(Color.BLACK);
		}
		this.heating.setText(String.valueOf(value));
	}

	protected void updateWindow(Boolean value) {
		if (value) {
			window.setText("offen");
			window.setForeground(BLUE);
		}
		else {
			window.setText("geschlossen");
			window.setForeground(GREY);
		}
	}

}
