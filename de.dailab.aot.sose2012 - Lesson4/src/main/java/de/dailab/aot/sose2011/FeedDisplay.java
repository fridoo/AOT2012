package de.dailab.aot.sose2011;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class FeedDisplay extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5338275423495938665L;
	
	private JEditorPane textArea = new JEditorPane();

	
	public FeedDisplay() {
		this.setSize(600, 800);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		this.setLayout(new GridBagLayout());
		
//		this.textArea.se
		
		JScrollPane scrollpane = new JScrollPane(textArea);
		this.add(scrollpane, gbc);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	public void addMessage(String msg) {
		Document doc = this.textArea.getDocument();
		try {
			doc.insertString(doc.getLength(), msg + "\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
