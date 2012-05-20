package de.dailab.gridworld.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.dailab.gridworld.ontology.Field;
import de.dailab.gridworld.ontology.Group;
import de.dailab.gridworld.ontology.Position;
import de.dailab.gridworld.ontology.Role;

/**
 * The Gridworld frame.
 * 
 * @author Tobias Küster
 * @author Michael Burkhardt
 * @version AOT SoSe 2011
 */
public class GridworldFrame extends JFrame {

	static final long serialVersionUID = 4263000971693086148L;
	private final int width;
	private final int height;
	private final int side;
	private final GridWorldComponent component;
	private Field field;
	private JPanel counterPanel;

	private final Map<String, Group> teams = new HashMap<String, Group>();
	private final Map<String, Role> roles = new HashMap<String, Role>();

	public GridworldFrame(final int width, final int height, final int side) {
		this.width = width;
		this.height = height;
		this.side = side;
		this.setTitle("Agent's Grid World");
		this.component = new GridWorldComponent();
		this.counterPanel = new JPanel();
		this.updateCounter(new HashMap<Group, Integer>());

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(this.component, BorderLayout.CENTER);
		this.getContentPane().add(this.counterPanel, BorderLayout.NORTH);
		/*
		 * TODO add additional buttons and stuff if necessary
		 */
		this.pack();
	}

	public void update(final Field field) {
		this.field = field;
		if (field != null) {
			assert field.width == this.width;
			assert field.height == this.height;
			this.component.repaint();
		}
	}

	public void updateCounter(final Map<Group, Integer> counter) {

		this.counterPanel.removeAll();
		this.counterPanel.setLayout(new GridLayout(2, counter.size() + 1));
		List<Group> groups = new ArrayList<Group>(counter.keySet());
		this.counterPanel.add(new JLabel("Group:"));
		for (Group group : groups) {
			this.counterPanel.add(new JLabel(group.name()));
		}
		this.counterPanel.add(new JLabel("Score:"));
		for (Group group : groups) {
			this.counterPanel.add(new JLabel(String.valueOf(counter.get(group))));
		}
		this.counterPanel.revalidate();
	}

	public void updateTeam(final String agentid, final Group group) {
		this.teams.put(agentid, group);
	}

	public void updateRole(final String agentid, final Role role) {
		this.roles.put(agentid, role);
	}

	class GridWorldComponent extends JComponent {

		static final long serialVersionUID = -4567754128064495247L;

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(GridworldFrame.this.width * GridworldFrame.this.side, GridworldFrame.this.height * GridworldFrame.this.side);
		}

		@Override
		public void paint(final Graphics g) {
			super.paint(g);

			/*
			 * draw cells
			 */
			if (GridworldFrame.this.field != null) {
				for (int x = 0; x < GridworldFrame.this.width; x++) {
					for (int y = 0; y < GridworldFrame.this.height; y++) {
						Position cell = GridworldFrame.this.field.getCell(x, y);

						/*
						 * add information about gold
						 */
						if (cell.gold) {
							g.setColor(Color.YELLOW);
							g.fillRect(x * GridworldFrame.this.side, y * GridworldFrame.this.side, GridworldFrame.this.side, GridworldFrame.this.side);
						}
						/*
						 * add information about agents
						 */
						if (cell.agent != null) {

							String aid = cell.agent.getAid();
							/*
							 * compute default value
							 */
							float hue = (float) (aid.hashCode() % 255) / 255;
							Color color = new Color(Color.HSBtoRGB(hue, 1, 1));
							/*
							 * overwrite color
							 */
							if (GridworldFrame.this.teams.containsKey(aid)) {
								color = GridworldFrame.this.teams.get(aid).color;
							}
							g.setColor(color);
							g.fillOval(x * GridworldFrame.this.side + GridworldFrame.this.side / 4,
									y * GridworldFrame.this.side + GridworldFrame.this.side / 4, GridworldFrame.this.side / 2, GridworldFrame.this.side / 2);
						}
					}
				}
			}

			/*
			 * draw grid
			 */
			g.setColor(Color.GRAY);
			for (int i = 0; i < GridworldFrame.this.width; i++) {
				g.drawLine(0, i * GridworldFrame.this.side, GridworldFrame.this.width * GridworldFrame.this.side, i * GridworldFrame.this.side);
			}
			for (int i = 0; i < GridworldFrame.this.height; i++) {
				g.drawLine(i * GridworldFrame.this.side, 0, i * GridworldFrame.this.side, GridworldFrame.this.height * GridworldFrame.this.side);
			}

		}
	}

}
