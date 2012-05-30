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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mosquito.sim.BoardPanel;
import mosquito.sim.GameEngine;
import mosquito.sim.GameListener;


/**
 * 
 * @author Bell
 * 
 */
public final class Text implements GameListener
{
	private GameEngine engine;
	private static final long serialVersionUID = 1L;
	private boolean longMode = false;
	private JTabbedPane tabPane;

	public void setLongMode(boolean longMode) {
		this.longMode = longMode;
	}
	public Text(GameEngine engine)
	{
		this.engine = engine;
		engine.addGameListener(this);
		
	}
	public void play()
	{
		if(engine.setUpGame())
			while(engine.step())
			{
				
			}
	}
	public void gameUpdated(GameUpdateType type)
	{
		switch (type)
		{
		case GAMEOVER:
			System.out.println("" + engine.getCurrentRound() +","+engine.getBoard().mosquitosCaught+","
					+ engine.getConfig().getNumLights() + ","+engine.getConfig().getNumMosquitos() + "," +engine.getConfig().getSelectedBoard().getName()
					+","+engine.getConfig().getPlayerClass().getName()
					);			
			break;
		case MOVEPROCESSED:
			if(longMode)
			System.out.println("" + engine.getCurrentRound() +","+engine.getBoard().mosquitosCaught+","
				+ engine.getConfig().getNumLights() + ","+engine.getConfig().getNumMosquitos() + "," +engine.getConfig().getSelectedBoard().getName()
				+","+engine.getConfig().getPlayerClass().getName()
				);
//			configPanel.updateScore(engine.getBoard().mosquitosCaught);
			break;
		case STARTING:
			System.out.println("Time,Num_Caught,Num_Lights,Num_Mosquitos,Board_Name,Player_Name");
			break;
		case MOUSEMOVED:
		default:
			// nothing.
		}
	}


}
