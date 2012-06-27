package de.dailab.aot.sose2011;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class FeedDisplay extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5338275423495938665L;
	
	private JTextArea textArea = new JTextArea();

	
	public FeedDisplay() {
		this.setSize(600, 800);
		this.add(this.textArea);
		this.setLayout(new GridBagLayout());
		this.setVisible(true);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
