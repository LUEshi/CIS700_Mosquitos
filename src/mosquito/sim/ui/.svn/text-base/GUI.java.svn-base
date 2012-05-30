/* 
 * 	$Id: GUI.java,v 1.4 2007/11/14 22:02:59 johnc Exp $
 * 
 * 	Programming and Problem Solving
 *  Copyright (c) 2007 The Trustees of Columbia University
 */
package mosquito.sim.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mosquito.sim.BoardPanel;
import mosquito.sim.GameEngine;
import mosquito.sim.GameListener;

//import colony.Tournament;
//import colony.Tournament.TournamentResult;
/**
 * 
 * @author Cieslewicz
 * 
 */
public final class GUI extends JFrame implements ActionListener, GameListener, ChangeListener
{
	private GameEngine engine;
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabPane;

	private ConfigurationPanel configPanel;
	private ControlPanel controlPanel;
	private BoardPanel boardPanel;

	private BoardEditor boardEditor;

	private BoardFrame boardFrame;
	public boolean is_recursive = false;
	
	private volatile boolean fast;
	private GameEngine real_engine;
	public void setEngine(GameEngine engine)
	{
		real_engine = this.engine;
		this.engine = engine;
		boardFrame.setBoard(engine.getBoard(), false);
	}
	public void restoreEngine()
	{
		this.engine = this.real_engine;
		boardFrame.setBoard(engine.getBoard(), false);
	}
	public GUI(GameEngine engine)
	{
		this.engine = engine;
		engine.gui = this;
		engine.addGameListener(this);
		JPanel topPanel = new JPanel(new BorderLayout());
		tabPane = new JTabbedPane();

		JPanel eastPanel = new JPanel(new GridLayout(1, 1));
		configPanel = new ConfigurationPanel(engine.getConfig(),engine);
		eastPanel.add(configPanel);

		controlPanel = new ControlPanel();

		boardPanel = new BoardPanel();
//		JScrollPane boardScroller = new JScrollPane();
//		boardScroller.getViewport().add(boardPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("COMS 4444 - Mosquito");
		setName("GUI");
		setPreferredSize(new Dimension(1000, 850));
		setMinimumSize(new Dimension(500, 500));
		getContentPane().add(topPanel);
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				boardPanel.recalculateDimensions();
				repaint();
			}
		});
		JPanel gamePanel = new JPanel(new BorderLayout());
		gamePanel.setName("Game Play");
		gamePanel.add(eastPanel, BorderLayout.EAST);
		gamePanel.add(controlPanel, BorderLayout.NORTH);
		gamePanel.add(boardPanel, BorderLayout.CENTER);
		boardPanel.recalculateDimensions();
		boardEditor = new BoardEditor(engine);

		tabPane.addTab("Game Play", gamePanel);
		tabPane.addTab("Board Editor", boardEditor);
		tabPane.validate();
		topPanel.add(tabPane, BorderLayout.CENTER);

		tabPane.addChangeListener(this);

		this.pack();
		this.setVisible(true);

		setupListeners();

		boardFrame = new BoardFrame(engine.getBoard());
	}

	private void setupListeners()
	{
		controlPanel.addListener(this);
		// controlPanel.play.addActionListener(this);
		// controlPanel.begin.addActionListener(this);
		// controlPanel.step.addActionListener(this);
		// controlPanel.stop.addActionListener(this);
		// controlPanel.pause.addActionListener(this);
		// controlPanel.tournament.addActionListener(this);
	}

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();
		if (command.compareToIgnoreCase("Begin") == 0)
		{
			

			// disable the begin button and the configPanel (freeze the config)
			controlPanel.begin.setEnabled(false);
			configPanel.setEnabled(false);
			controlPanel.tournament.setEnabled(false);
			fast = false;
//			Thread runner = new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
					if (engine.setUpGame())
					{
						boardPanel.setEngine(engine);
						boardFrame.setEngine(engine);
						boardPanel.setBoard(engine.getBoard(), false);
						boardFrame.setBoard(engine.getBoard(), false);
						// boardFrame.playerLabel.setText(engine.getPlayerName());
						// boardFrame.playerLabel.setForeground(engine.getPlayerColor());
						boardFrame.round.setText("Round: 0");
						controlPanel.stop.setEnabled(true);
						controlPanel.play.setEnabled(true);
						controlPanel.pause.setEnabled(false);
						controlPanel.step.setEnabled(true);
					} else
					{
						// game set up failed. Turn the right buttons on/off.
						controlPanel.begin.setEnabled(true);
						configPanel.setEnabled(true);
						controlPanel.tournament.setEnabled(true);
					}
					repaint();					
//				}
//			});
//			runner.run();
			
		} else if (command.compareToIgnoreCase("Step") == 0)
		{
			engine.step();
		} else if (command.compareToIgnoreCase("Play") == 0)
		{
			controlPanel.step.setEnabled(false);
			controlPanel.play.setEnabled(false);
			controlPanel.pause.setEnabled(true);
			controlPanel.begin.setEnabled(false);
			controlPanel.tournament.setEnabled(false);
			controlPanel.stop.setEnabled(true);
			fast = true;
			GameRunner runner = new GameRunner(configPanel.getSpeedSlider());
			runner.setName("Game Runner");
			runner.start();
		} else if (command.equalsIgnoreCase("Pause"))
		{
			fast = false;
			controlPanel.step.setEnabled(true);
			controlPanel.play.setEnabled(true);
			controlPanel.pause.setEnabled(false);
			controlPanel.begin.setEnabled(false);
			controlPanel.stop.setEnabled(true);
			controlPanel.tournament.setEnabled(false);
		} else if (command.compareToIgnoreCase("Stop") == 0)
		{
			fast = false;
			controlPanel.stop.setEnabled(false);
			controlPanel.play.setEnabled(false);
			controlPanel.pause.setEnabled(false);
			controlPanel.step.setEnabled(false);
			controlPanel.begin.setEnabled(true);
			controlPanel.tournament.setEnabled(true);
			configPanel.setEnabled(true);
		} else if (command.compareToIgnoreCase("Tournament") == 0)
		{
//			 controlPanel.begin.setEnabled(false);
//			 controlPanel.play.setEnabled(false);
//			 controlPanel.step.setEnabled(false);
//			 controlPanel.pause.setEnabled(false);
//			 controlPanel.stop.setEnabled(false);
//			 controlPanel.tournament.setEnabled(false);
//			 configPanel.setEnabled(false);
//			
//			 Tournament t = new Tournament(this.engine, ); //avoid dialog messages.
//			 engine.removeGameListener(this);
//			 t.run();
//						
//			 ArrayList<TournamentResult> results = t.getResults();
//			 for(TournamentResult r : results)
//			 System.out.println(r);
//			 File file = new
//			 File("tournament_"+engine.getConfig().getSelectedBoard().getName()
//					 +"_"+engine.getConfig().getActivePlayerNum()
//					 +"_"+engine.getConfig().getRange()
//			 +"_"+ engine.getConfig().getMaxRounds()
//			 +"_"+engine.getConfig().getTournamentGames()+".txt");
//			 try{
//			 FileWriter f = new FileWriter(file, false);
//							
//			 for(TournamentResult r : results)
//			 f.write(r.toString()+"\n");
//			 //f.write("\n");
//			 f.close();
//			 }catch(IOException e){
//			 System.err.println("Failed printing to output file:" + file );
//			 }
//						
//			 //TODO turn into thread, allow us to kill it...
//			 engine.addGameListener(this);
//			 //TODO need to record outcome somewhere...
//			
//			 controlPanel.begin.setEnabled(true);
//			 controlPanel.play.setEnabled(false);
//			 controlPanel.step.setEnabled(false);
//			 controlPanel.stop.setEnabled(false);
//			 controlPanel.tournament.setEnabled(true);
//			 configPanel.setEnabled(true);

		} else if (command.compareToIgnoreCase("BoardFrame") == 0)
		{
			boardFrame.setVisible(true);
		} else
		{
			throw new RuntimeException("Unknow Action Command: " + command);
		}
	}

	private class GameRunner extends Thread implements ChangeListener
	{
		private JSlider slider;
		private int delay;

		public GameRunner(JSlider slider)
		{
			this.slider = slider;
		}

		public void run()
		{
			delay = slider.getValue();
			slider.addChangeListener(this);
			while (fast && engine.step())
			{
				try
				{
					Thread.sleep(delay);
				} catch (InterruptedException e)
				{
					// this should not happen!
					e.printStackTrace();
				}
				
			}
			slider.removeChangeListener(this);
		}

		public void stateChanged(ChangeEvent arg0)
		{
			if (arg0.getSource().equals(slider))
			{
				delay = ((JSlider) arg0.getSource()).getValue();
			}
		}
	}

	public void gameUpdated(GameUpdateType type)
	{
		// find our super parent frame -- needed for dialogs
		Component c = this;
		while (null != c.getParent())
			c = c.getParent();

		switch (type)
		{
		case GAMEOVER:
			if(!is_recursive)
			{
				fast = false;
				controlPanel.play.setEnabled(false);
				controlPanel.step.setEnabled(false);
				controlPanel.pause.setEnabled(false);
				controlPanel.stop.setEnabled(false);
				controlPanel.begin.setEnabled(true);
				controlPanel.tournament.setEnabled(true);
				configPanel.setEnabled(true);
				String s = "Achieved 50% capture at t=" + (engine.getCurrentRound()-1);
				JOptionPane.showMessageDialog((Frame) c, s, "Game Over", JOptionPane.INFORMATION_MESSAGE);
			}
			break;
		case MOVEPROCESSED:
			controlPanel.roundText.setText("" + engine.getCurrentRound());
			boardFrame.round.setText("Round: " + engine.getCurrentRound());
			boardFrame.bp.repaint();
			boardPanel.repaint();
			configPanel.updateScore(engine.getBoard().mosquitosCaught);
//			configPanel.updateScores(engine.getScores());
			break;
		case STARTING:
			controlPanel.roundText.setText("0");
			break;
		case MOUSEMOVED:
			configPanel.setMouseCoords(BoardPanel.MouseCoords);
		case REPAINT:
			boardFrame.bp.repaint();
			boardPanel.repaint();
		default:
			// nothing.
		}
	}

	public void stateChanged(ChangeEvent arg0)
	{
		if (arg0.getSource().equals(tabPane))
		{
			engine.getConfig().readBoards();
			configPanel.reloadBoards();
			
		}
	}

}
