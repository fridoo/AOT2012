package de.dailab.aot.sose2012.user;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * This is a simple GUI element with two buttons, sending commands to the parent agent bean.
 * 
 * @author mib
 * @version AOT SoSe 2011
 */
public final class WindowSwitch extends JFrame implements ActionListener {

	private static final long serialVersionUID = 7966392006435636473L;
	private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);

	private final JButton buttonOpen = new JButton(UserAgentBean.CMD_WINDOW_OPEN);
	private final JButton buttonClose = new JButton(UserAgentBean.CMD_WINDOW_CLOSE);
	private final UserAgentBean parent;

	protected WindowSwitch(UserAgentBean arg) {
		if (arg == null) {
			throw new IllegalArgumentException("parameter UserAgentBean is null");
		}
		this.parent = arg;
		this.setTitle("Window Switcher");
		// this.setSize(400, 300);
		this.init();
	}

	private void init() {

		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setBorder(BORDER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel.add(buttonOpen, BorderLayout.WEST);
		panel.add(buttonClose, BorderLayout.EAST);

		buttonOpen.addActionListener(this);
		buttonClose.addActionListener(this);

		this.add(panel);
		this.pack();
	}

	/**
	 * Delegates the action event from the two buttons to the parent agent bean.
	 * 
	 * @param event
	 *           the event from a button, if it was pushed
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		this.parent.windowSwitch(event.getActionCommand());
	}

}
