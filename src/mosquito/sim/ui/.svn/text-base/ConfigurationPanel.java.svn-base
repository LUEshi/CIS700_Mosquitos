/*
 * 	$Id: ConfigurationPanel.java,v 1.3 2007/11/14 22:00:22 johnc Exp $
 * 
 * 	Programming and Problem Solving
 *  Copyright (c) 2007 The Trustees of Columbia University
 */
package mosquito.sim.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mosquito.sim.GameConfig;
import mosquito.sim.GameEngine;
import mosquito.sim.Player;


public final class ConfigurationPanel extends JPanel implements ChangeListener, ItemListener, ListSelectionListener
{
	private static final long serialVersionUID = 1L;
	private GameConfig config;

	static Font config_font = new Font("Arial", Font.PLAIN, 14);

	private JLabel roundLabel;
	private JSpinner roundSpinner;

	private JLabel numAntsLabel;
	private JSpinner numLightsSpinner;
	private JSpinner numMosquitosSpinner;
	
	private JList playerList;
	private JLabel score;
	private JButton remove;

	private JLabel playerLabel;
	private JComboBox playerBox;

	private JLabel boardLabel;
	private JComboBox boardBox;

	private Class<Player> selectedPlayer;

	private JSlider speedSlider;
	protected JLabel interactiveHelp;
	private JButton generateMosquitosButton;
	private GameEngine engine;
	
	public ConfigurationPanel(final GameConfig config,final GameEngine engine)
	{
		this.config = config;
		this.engine= engine;
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Configuration"));
		this.setPreferredSize(new Dimension(350, 1200));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(layout);

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;

		JPanel panel = new JPanel(new FlowLayout());
		
		numAntsLabel = new JLabel("Number of Lights: ");
		numAntsLabel.setFont(config_font);
		numLightsSpinner = new JSpinner(new SpinnerNumberModel(this.config.getNumLights(), 1, null, 1));
		numLightsSpinner.setPreferredSize(new Dimension(120, 25));
		numLightsSpinner.addChangeListener(this);	

		panel = new JPanel(new FlowLayout());
		panel.add(numAntsLabel);
		panel.add(numLightsSpinner);
		layout.setConstraints(panel, c);
		this.add(panel);
		
		JLabel label = new JLabel("Number of Mosquitos: ");
		label.setFont(config_font);
		numMosquitosSpinner = new JSpinner(new SpinnerNumberModel(this.config.getNumMosquitos(), 1, null, 1));
		numMosquitosSpinner.setPreferredSize(new Dimension(120, 25));
		numMosquitosSpinner.addChangeListener(this);	

		panel = new JPanel(new FlowLayout());
		panel.add(label);
		panel.add(numMosquitosSpinner);
		score = new JLabel("Score: N/A");
		panel.add(score);
		layout.setConstraints(panel, c);
		this.add(panel);
		
		panel = new JPanel(new FlowLayout());
		panel.setMinimumSize(new Dimension(100, 200));
		playerLabel = new JLabel("Player:");
		panel.add(playerLabel);
		// make player combo box
		playerBox = new JComboBox(config.getPlayerListModel());
		playerBox.addItemListener(this);
		playerBox.setRenderer(new ClassRenderer());
		panel.add(playerBox);
		interactiveHelp = new JLabel("<html><b>Interactive mode enabled:</b><br>" +
				"<i>Click</i>: toggle a light<br>" +
				"<i>Ctrl-alt-click</i>: collector<br>" +
				"<i>Ctrl-click</i>: light<br>" +
				"<i>Alt-click</i>: 1 mosquito<br>" +
				"<i>Alt-shift click</i>: 100 mosquitos<br><br>" +
				"Caution: Game-end will be determined by the<br>" +
				" population size specified above, not actual count</html>");
		interactiveHelp.setVisible(false);
		generateMosquitosButton = new JButton("Fill Mosquitos");
		generateMosquitosButton.setVisible(false);
		generateMosquitosButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				engine.getBoard().createMosquitos(config.getNumMosquitos());
				engine.notifyRepaint();
			}
		});
		panel.add(generateMosquitosButton);
		panel.add(interactiveHelp);
		layout.setConstraints(panel, c);
		this.add(panel);
		config.setPlayerClass((Class<Player>) playerBox.getSelectedItem());

		// board combo
		panel = new JPanel(new FlowLayout());
		boardLabel = new JLabel("Board:");
		boardBox = new JComboBox(config.getBoardList());
		boardBox.addItemListener(this);
		panel.add(boardLabel);
		panel.add(boardBox);
		layout.setConstraints(panel, c);
		this.add(panel);

		speedSlider = new JSlider(0, 1000);
		speedSlider.setValue(0);
		panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("Delay (0 - 1000ms):"));
		panel.add(speedSlider);
		layout.setConstraints(panel, c);
		this.add(panel);
		
		mouseCoords = new JLabel("Mouse: N/A");
		add(mouseCoords);
		
		
	}
	public void setMouseCoords(Point2D p)
	{
		if(p == null)
			mouseCoords.setText("Mouse: N/A");
		float v1 = (float) Math.round(p.getX()*100)/100;
		float v2 = (float) Math.round(p.getY()*100)/100;
		mouseCoords.setText(String.format("Mouse: %4.2f, %4.2f", v1,v2));
	}
	protected JLabel mouseCoords;
	
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		boardBox.setEnabled(enabled);
		numMosquitosSpinner.setEnabled(enabled);
		numLightsSpinner.setEnabled(enabled);
		playerBox.setEnabled(enabled);
	}

	public void stateChanged(ChangeEvent arg0)
	{
		if (arg0.getSource().equals(roundSpinner))
			config.setMaxRounds(((Integer) ((JSpinner) arg0.getSource()).getValue()).intValue());
		else if(arg0.getSource().equals(numLightsSpinner))
			config.setNumLights(((Integer) ((JSpinner) arg0.getSource()).getValue()).intValue());
		else if(arg0.getSource().equals(numMosquitosSpinner))
			config.setNumMosquitos(((Integer) ((JSpinner) arg0.getSource()).getValue()).intValue());
		else
			throw new RuntimeException("Unknown State Changed Event!!");
	}

	public void itemStateChanged(ItemEvent arg0)
	{
		if (arg0.getSource().equals(playerBox) && arg0.getStateChange() == ItemEvent.SELECTED)
		{
			// config.setActivePlayer((Class)arg0.getItem());
			selectedPlayer = (Class) arg0.getItem();
			config.setPlayerClass((Class<Player>) playerBox.getSelectedItem());
			if(selectedPlayer.getName().equals("mosquito.g0.InteractivePlayer"))
			{
				this.interactiveHelp.setVisible(true);
				this.generateMosquitosButton.setVisible(true);
			}
			else
			{
				this.interactiveHelp.setVisible(false);
				this.generateMosquitosButton.setVisible(false);
			}
			//System.out.println("SELECTED:" + selectedPlayer.toString());
		}
		if (arg0.getSource().equals(boardBox) && arg0.getStateChange() == ItemEvent.SELECTED)
		{
			config.setSelectedBoard((File) arg0.getItem());
		}
	}

	public void reloadBoards()
	{
		boardBox.removeAllItems();
		File[] files = config.getBoardList();
		for (int i = 0; i < files.length; i++)
			boardBox.addItem(files[i]);
	}

	public JSlider getSpeedSlider()
	{
		return speedSlider;
	}

	public void valueChanged(ListSelectionEvent e)
	{
	}
	
	public void updateScore(int caught)
	{
		score.setText("Caught: " + caught + " ("+Math.round(10000d*(double) (caught)/(double)config.getNumMosquitos())/100d + "%)");
	}
}
