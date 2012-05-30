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
public final class Tournament implements GameListener
{
	private GameEngine engine;
	private static final long serialVersionUID = 1L;
	private boolean longMode = false;
	
	private boolean finishedExecuting = false;
	private JTabbedPane tabPane;
	private boolean result;
	public void setLongMode(boolean longMode) {
		this.longMode = longMode;
	}
	public Tournament(GameEngine engine)
	{
		this.engine = engine;
		engine.addGameListener(this);
		
	}
	public void play()
	{
		finishedExecuting = false;
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				result = engine.setUpGame();
				finishedExecuting = true;
			}
		});
		t.start();
		try {
			t.join(3600000);
			if(finishedExecuting = false)
			{
				t.stop();
				timedOut = true;
			}
			if(result)
				while(engine.step())
				{
					
				}
			else
				System.out.println("Result was false!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
	}
	public int finalTime;
	public int finalN;
	public boolean timedOut = false;
	public void gameUpdated(GameUpdateType type)
	{
		switch (type)
		{
		case GAMEOVER:
			finalTime = engine.getCurrentRound();
			finalN = engine.getBoard().mosquitosCaught;
			break;
		case MOVEPROCESSED:
			break;
		case STARTING:
			break;
		case MOUSEMOVED:
		default:
			// nothing.
		}
	}


}
