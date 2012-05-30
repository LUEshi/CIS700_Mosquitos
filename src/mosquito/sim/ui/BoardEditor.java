/* 
 * 	$Id: BoardEditor.java,v 1.3 2007/11/15 19:12:11 johnc Exp $
 * 
 * 	Programming and Problem Solving
 *  Copyright (c) 2007 The Trustees of Columbia University
 */
package mosquito.sim.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import mosquito.sim.Board;
import mosquito.sim.BoardPanel;
import mosquito.sim.GameEngine;




public final class BoardEditor extends JPanel implements ItemListener, ChangeListener, ActionListener{
	private static final long serialVersionUID = 1L;
	JSpinner heightSpinner;
	JSpinner widthSpinner;
	JButton resetButton;
	JButton loadButton;
	JButton removeButton;
	JButton saveButton;
	JComboBox editBox;
	JSpinner nestsSpinner;
	JSpinner blockedSpinner;
	JButton randomButton;
	
	boolean loading = false;
	
	BoardPanel boardPanel;
	Board board;
	GameEngine engine;
	
	JFileChooser chooser;
	
	public BoardEditor(GameEngine eng){
		engine = eng;
		init();
	}
	private void init(){
		chooser = new JFileChooser();
		chooser.setFileFilter(new XMLFilter());
		setLayout(new BorderLayout());
		JPanel controlPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		controlPanel.setLayout(layout);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		controlPanel.setPreferredSize(new Dimension(200, 300));
		
		controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Configuration"));
		
		JPanel panel = new JPanel(new FlowLayout());
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		resetButton.setActionCommand("RESET");
		panel = new JPanel();
		panel.add(resetButton);
		layout.setConstraints(panel,c);
		controlPanel.add(panel);
		
		loadButton = new JButton("Load");
		loadButton.addActionListener(this);
		loadButton.setActionCommand("LOAD");
		panel = new JPanel();
		panel.add(loadButton);
		layout.setConstraints(panel, c);
		controlPanel.add(panel);
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setActionCommand("SAVE");
		panel = new JPanel();
		panel.add(saveButton);
		layout.setConstraints(panel, c);
		controlPanel.add(panel);
		
		removeButton = new JButton("Remove Selected");
		removeButton.addActionListener(this);
		removeButton.setActionCommand("REMOVE");
		panel = new JPanel();
		panel.add(removeButton);
		layout.setConstraints(panel, c);
		controlPanel.add(panel);
		
		loading = false;
		
		board = new Board();
		boardPanel = new BoardPanel(engine, true);
		boardPanel.setBoard(board, true);
		
		scroller = new JScrollPane();
		scroller.getViewport().add(boardPanel);
		
		this.add(controlPanel, BorderLayout.EAST);
		this.add(scroller, BorderLayout.CENTER);
	}
	JScrollPane scroller;
	public void setBoard(Board b){
		board = b;
		boardPanel.setBoard(b, true);
		
		scroller.revalidate();
		scroller.repaint();
	}

//	public final static void main(String [] args){
//		JFrame f = new JFrame();
//		f.setPreferredSize(new Dimension(600, 600));
//		f.getContentPane().add(new BoardEditor(engine));
//		f.pack();
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.setVisible(true);
//	}
	
	public void itemStateChanged(ItemEvent arg0) {

	}
	public void stateChanged(ChangeEvent arg0) {
		JSpinner spinner = (JSpinner)arg0.getSource();
		if(!loading && (spinner.equals(heightSpinner) || spinner.equals(widthSpinner))) {
			board = new Board( ((Integer)widthSpinner.getValue()).intValue(), ((Integer)heightSpinner.getValue()).intValue());
			boardPanel.setBoard(board, true);
			System.out.println("Changed size");
			this.getParent().repaint();
			this.repaint();
		}
	}
	
	private class XMLFilter extends FileFilter{

		@Override
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".xml");
		}

		@Override
		public String getDescription() {
			return "XML Files";
		}
		
	}
	
	public void actionPerformed(ActionEvent arg0) {
		//find our super parent frame -- needed for dialogs
		Component c = this;
		while (null != c.getParent())
			c = c.getParent();
				
		if(arg0.getActionCommand().equalsIgnoreCase("SAVE")){
			chooser.setCurrentDirectory(new File("boards"));
			int returnVal = chooser.showSaveDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File file = chooser.getSelectedFile();
				try{
					board.save(file);
					JOptionPane.showMessageDialog((Frame)c, "File saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
				}catch(Exception e){
					JOptionPane.showMessageDialog( (Frame)c, e, "Save Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}else if(arg0.getActionCommand().equalsIgnoreCase("LOAD")){
			chooser.setCurrentDirectory(new File("boards"));
			int returnVal = chooser.showOpenDialog(this);
			
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File file = chooser.getSelectedFile();
				try{
					loading = true;
					board.load(file);
					boardPanel.setBoard(board, true);
					JOptionPane.showMessageDialog((Frame)c, "File loaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
					loading = false;
				}catch(IOException e){
//					e.printStackTrace();
					JOptionPane.showMessageDialog( (Frame)c, e, "Load Error", JOptionPane.ERROR_MESSAGE);
					loading = false;
				}
			}
		}else if(arg0.getActionCommand().equalsIgnoreCase("RESET")){
			board = new Board(20, 20);
			boardPanel.setBoard(board, true);
			this.repaint();
			boardPanel.repaint();
		}else if(arg0.getActionCommand().equalsIgnoreCase("REMOVE")){
			boardPanel.removeSelectedLine();
		}
	}
}
