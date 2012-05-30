/* 
 * 	$Id: GameEngine.java,v 1.6 2007/11/28 16:30:47 johnc Exp $
 * 
 * 	Programming and Problem Solving
 *  Copyright (c) 2007 The Trustees of Columbia University
 */
package mosquito.sim;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import mosquito.sim.GameListener.GameUpdateType;
import mosquito.sim.ui.GUI;
import mosquito.sim.ui.Text;
import mosquito.sim.ui.Tournament;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public final class GameEngine 
{

	private GameConfig config;
	private Board board;
	// private PlayerWrapper player;
	private int round;
	public GUI gui;
	private ArrayList<GameListener> gameListeners;
	private Logger log;
	boolean initDone = false;
	
	public boolean isSimulated = false;
	static {
		PropertyConfigurator.configure("logger.properties");
	}
	public GameEngine(GameConfig config)
	{
		this.config = (GameConfig) config.clone();
		gameListeners = new ArrayList<GameListener>();
		board = new Board(10, 10);
		board.engine=this;
		this.isSimulated = true;
			try {
				board.load(config.getSelectedBoard());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    log = Logger.getLogger(GameController.class);
	}
	public GameEngine(String configFile)
	{
		config = new GameConfig(configFile);
		gameListeners = new ArrayList<GameListener>();
		board = new Board(10, 10);
		board.engine=this;
		if(config.getSelectedBoard() != null)
			try {
				board.load(config.getSelectedBoard());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    log = Logger.getLogger(GameController.class);
	}

	public void addGameListener(GameListener l)
	{
		gameListeners.add(l);
	}

	public int getCurrentRound()
	{
		return round;
	}

	public GameConfig getConfig()
	{
		return config;
	}
	private Player curPlayer;
	public Board getBoard()
	{
		return board;
	}
	public int getNumCaught()
	{
		return board.mosquitosCaught;
	}
	public boolean step()
	{
		if(board.mosquitosCaught >= config.getNumMosquitos() || (config.getMaxRounds() > 0 && getCurrentRound() > config.getMaxRounds()))
		{
			//GAME OVER!
			notifyListeners(GameUpdateType.GAMEOVER);
			return false;
		}
		try
		{
			// remember previous positions of the lights to detect illegal moves
			int size = board.getLights().size();
			double xCoords[] = new double[size];
			double yCoords[] = new double[size];
			Object[] lightArray = board.getLights().toArray();
			for (int i = 0; i < lightArray.length; i++) {
				Light l = (Light)lightArray[i];
				xCoords[i] = l.getX();
				yCoords[i] = l.getY();
			}
			
			// update the array of where the mosquitoes are
			int count[][] = new int[100][100];
			for (int i = 0; i < 100; i++)
				for (int j = 0; j < 100; j++)
					count[i][j] = 0;
			for (Mosquito m : board.getMosquitos()) {
				if (!m.caught) {
					Point2D location = m.location;
					count[(int)location.getX()][(int)location.getY()]++;
				}
			}
			
			/* DEBUG */
			/*
			for (int j = 0; j < 100; j++) {
				for (int i = 0; i < 100; i++) {
					System.err.print(count[i][j] + " ");
				}
				System.err.println("||");
			}
			*/
			
			
			// ask the Player for the new position of the lights
			Set<Light> lights = curPlayer.updateLights(count);
			// make sure there's no monkey business
			if (lights.size() != size) {
				System.err.println("ERROR! wrong number of lights!");
				System.exit(-1);
			}
			else {
				lightArray = lights.toArray();
				for (int i = 0; i < lights.size(); i++) {
					Light a = (Light)lightArray[i];
					if ((Math.abs(a.getX()-xCoords[i]) > 1) || (Math.abs(a.getY() - yCoords[i]) > 1)) {
						System.err.println("ERROR! light moved by more than one!");
						System.exit(-1);
					}
					
				}
			}
			board.setLights(lights);

			for(Mosquito m : board.getMosquitos())
			{
				if(!m.caught)
				{
					double d = board.getDirectionOfLight(m.location);
					m.moveInDirection(d,board.getWalls());
					// iterate over all the Collectors
					for (Collector c : board.getCollectors()) {
						if(c.contains(m))
						{
							m.caught = true;
							board.mosquitosCaught++;
							break; // so that a Mosquito can't be caught by two Collectors
						}
					}
				}
			}
			
		}
		catch(ConcurrentModificationException e)
		{
			
		}
		notifyListeners(GameUpdateType.MOVEPROCESSED);
		round++;
		return true;
	}

	

	private final static void printUsage()
	{
		System.err.println("Usage: GameEngine <config file> gui");
		System.err.println("Usage: GameEngine <config file> text <board> <playerclass> <num mosquitos> <num lights> <long|short> {max rounds}");
	}

	public void removeGameListener(GameListener l)
	{
		gameListeners.remove(l);
	}
	public void notifyRepaint()
	{
		Iterator<GameListener> it = gameListeners.iterator();
		while (it.hasNext())
		{
			it.next().gameUpdated(GameUpdateType.REPAINT);
		}
	}
	private void notifyListeners(GameUpdateType type)
	{
		Iterator<GameListener> it = gameListeners.iterator();
		while (it.hasNext())
		{
			it.next().gameUpdated(type);
		}
	}

	public static final void main(String[] args)
	{
		System.setOut(
    		    new PrintStream(new OutputStream() {
					@Override
					public void write(int b) throws IOException {						
					}
				}));
		if (args.length < 2 || args.length>8)
		{
			printUsage();
			System.exit(-1);
		}
		GameEngine engine = new GameEngine(args[0]);
		if (args[1].equalsIgnoreCase("text"))
		{
			// TextInterface ti = new TextInterface();
			// ti.register(engine);
			// ti.playGame(); 
			if(args.length < 7)
			{
				printUsage();
				System.exit(-1);
			}
			Text t = new Text(engine);
			engine.getConfig().setSelectedBoard(new File(args[2]));
			try {
				engine.getConfig().setPlayerClass((Class<Player>) Class.forName(args[3]));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			engine.getConfig().setNumMosquitos(Integer.valueOf(args[4]));
			engine.getConfig().setNumLights(Integer.valueOf(args[5]));
			if(args[6].equals("long"))
				t.setLongMode(true);
			if(args.length == 8)
				engine.getConfig().setMaxRounds(Integer.valueOf(args[7]));
			t.play();
//			throw new RuntimeException("Text interface not implemented. Sorry.");
		} else if (args[1].equalsIgnoreCase("gui"))
		{
			
			new GUI(engine);
		}
		else if (args[1].equalsIgnoreCase("tournament"))
		{
	    	initDB("results.db");
	    	long[] seeds = {2910170899112512421L,
	    			-8977088174481070443L,
	    			-8651071089805884396L,
	    			-4166219567344743899L,
	    			5385329145778595518L,
	    			4553327735362579128L,
	    			836993384686631740L,
	    			2390539080315511172L,
	    			1071911339596990231L,
	    			4537303394863086842L,
	    			6133352412041713255L,
	    			-6409026853841832954L,
	    			5450972113990920893L,
	    			1046523556615473556L,
	    			-5850185610449406910L,
	    			4442126462366377046L,
	    			-2732436307574212320L,
	    			-5358868350014628156L,
	    			2941790413888409376L,
	    			8419378013595046756L};
	    	try {
				Statement s = GameEngine.conn.createStatement();
				ResultSet rs1 = s.executeQuery("SELECT COUNT(*) FROM jobs where started is null");
				rs1.next();
				int n = rs1.getInt(1);
				while(n > 0)
				{
					ResultSet rs = s.executeQuery("SELECT job,id from jobs where started is null limit 1");
					if(rs.next())
					{
						int nz = rs.getInt(2);
						String job = (rs.getString(1));
						Statement s2 = GameEngine.conn.createStatement();
						int zz = s2.executeUpdate("UPDATE jobs set started=NOW() where id="+rs.getInt(2) + " and started is null");
						rs.close();
						if(zz == 0)
							continue;
						String[] args2 = job.split(";");
//						System.err.println("Runnign text");
						GameEngine.lights = null;
						GameEngine.collectors = null;
						for(int i = 0; i<20;i++)
						{
							GameEngine eng = new GameEngine("mosquito.xml");
							GameConfig.random = new Random(seeds[i]);
							runTourn(args2,eng,seeds[i],i);
						}
						s2.executeUpdate("UPDATE jobs set ended=NOW() where id="+nz);
					}
					rs1 = s.executeQuery("SELECT COUNT(*) FROM jobs where started is null");
					rs1.next();
					n = rs1.getInt(1);
//						n--;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			runTournament(args, engine);
		}
		else
		{
			printUsage();
			System.exit(-1);
		}
	}
	private static void runTourn(String args[],GameEngine engine,long seed, int n)
	{
		Statement s;
		java.net.InetAddress localMachine = null;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	String hostname =  localMachine.getHostName();
    	int tournament_id =-1;
		try {
			s = conn.createStatement();	
			s.execute("INSERT INTO tournament (start,source,player,board,num_lights,seed,run_num)" +
					" VALUES (NOW(),\""+hostname+"\",\""+args[0]+"\",\""+args[2]+"\",\""+args[1]+"\",\""+seed+"\",\""+n+"\")",Statement.RETURN_GENERATED_KEYS);
			ResultSet rszz= s.getGeneratedKeys();
			rszz.next();
			tournament_id = rszz.getInt(1);
//			System.out.println("ID " + tournament_id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tournament t = new Tournament(engine);
		engine.getConfig().setSelectedBoard(new File("boards/"+args[2]+".xml"));
		try {
			engine.getConfig().setPlayerClass((Class<Player>) Class.forName(args[0]));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		engine.getConfig().setNumMosquitos(1000);
		engine.getConfig().setNumLights(Integer.valueOf(args[1]));
		engine.getConfig().setMaxRounds(15000);
		try
		{
			t.play();
		}
		catch(Exception e)
		{
			System.err.println("Exception in Tournament " + tournament_id + ", player " + args[0]);
			e.printStackTrace();
		}
		if(t.timedOut)
		{
			System.err.println("Timeout in Tournament " + tournament_id + ", player " + args[0]);
		}
		if(n == 0)
		{
			BoardPanel pan = new BoardPanel(engine, false);
			BufferedImage im = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
			pan.paint(im.getGraphics());
		    Iterator writers = ImageIO.getImageWritersByFormatName("png");
		    ImageWriter writer = (ImageWriter)writers.next();
		    File f = new File("output/"+args[0]+"_"+args[2]+"_"+args[1]+".png");
		    ImageOutputStream ios;
			try {
				ios = ImageIO.createImageOutputStream(f);
				 writer.setOutput(ios);
				 writer.write(im);
				 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
		    
		}
		
		try {
			s = conn.createStatement();
			s.execute("UPDATE tournament SET end=NOW(), num_caught="+t.finalN+",time="+t.finalTime + " where id="+tournament_id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void runText(String args[])
	{
		GameEngine engine = new GameEngine("mosquito.xml");
		// TextInterface ti = new TextInterface();
		// ti.register(engine);
		// ti.playGame(); 
		if(args.length < 7)
		{
			printUsage();
			System.exit(-1);
		}
		Text t = new Text(engine);
		engine.getConfig().setSelectedBoard(new File(args[2]));
		try {
			engine.getConfig().setPlayerClass((Class<Player>) Class.forName(args[3]));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		engine.getConfig().setNumMosquitos(Integer.valueOf(args[4]));
		engine.getConfig().setNumLights(Integer.valueOf(args[5]));
		if(args[6].equals("long"))
			t.setLongMode(true);
		if(args.length == 8)
			engine.getConfig().setMaxRounds(Integer.valueOf(args[7]));
		t.play();
	}
	static Set<Light> lights;
	static Set<Collector> collectors; 

	
	public boolean setUpGame()
	{
		try
		{
			round = 0;
			board.load(config.getSelectedBoard());
			board.mosquitosCaught = 0;
			board.setLights(new HashSet<Light>()); // TODO: do we need this?
			
			initDone = false;
			curPlayer = config.getPlayerClass().newInstance();
			curPlayer.setMyConfig((GameConfig) config.clone());
			curPlayer.setGUI(gui);
			curPlayer.Register();
			
			if(this.isSimulated)
				curPlayer.startSimulatedGame(board.getWalls(), config.getNumLights()); // TODO
			else
				curPlayer.startNewGame(board.getWalls(), config.getNumLights(), config.getNumCollectors());
			
			board.createMosquitos(config.getNumMosquitos());
			board.setInteractive(false);

			// update the array of where the mosquitoes are
			int count[][] = new int[100][100];
			for (int i = 0; i < 100; i++)
				for (int j = 0; j < 100; j++)
					count[i][j] = 0;
			for (Mosquito m : board.getMosquitos()) {
				Point2D location = m.location;
				count[(int)location.getX()][(int)location.getY()]++;
			}
			
			/* DEBUG */
			/*
			for (int j = 0; j < 100; j++) {
				for (int i = 0; i < 100; i++) {
					System.err.print(count[i][j] + " ");
				}
				System.err.println("||");
			}
			*/
			Set<Light> lights= curPlayer.getLights(count); 
			if(lights == null)
			{
				System.err.println("Error: Player returned null for lights");
				return false;
			}
			if(lights.size() > config.getNumLights()) 
			{
				System.err.println("Error: You needed to give "  +config.getNumLights() +", but you gave " + lights.size() + " lights instead!");
				return false;
			}
			for(Light l : lights)
			{
				if(l.getX() < 0 || l.getX() > 100 || l.getY() < 0 || l.getY() > 100)
				{
					System.err.println("Error: Lights are OOB");
					System.err.println(l.getX() + ", " + l.getY());
					return false;
				}
			}
			board.setLights(lights);

			Set<Collector> collectors = curPlayer.getCollectors(); 
			if(collectors == null)
			{
				System.err.println("Error: Player returned null for collectors");
				return false;
			}
			if(collectors.size() > config.getNumCollectors()) 
			{
				System.err.println("Error: You needed to give "  +config.getNumCollectors() +", but you gave " + collectors.size() + " collectors instead!");
				return false;
			}
			for (Collector col : collectors) {
				for(Line2D w : board.getWalls())
				{
					if (col.intersects(w)) {
						System.err.println("Error: Collector intersects walls");
						return false;
					}
				}
				for(Light l : lights)
				{
					if(col.contains(l.getLocation()))
					{
						System.err.println("Error: Collector intersects light");
						return false;
					}
				}
				if(col.getX() < 0 || col.getX() > 100 || col.getY() < 0 || col.getY() > 100)
				{
					System.err.println("Error: Collector is OOB");
					return false;
				}
			}
			board.setCollectors(collectors); 

			if(!this.isSimulated)
			{ // TODO: do we still need this?
				GameEngine.lights = lights;
				GameEngine.collectors = collectors;
			}
			initDone = true;
		} catch (IOException e)
		{
			log.error("Exception: " + e);
			return false;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		round = 0;
		notifyListeners(GameUpdateType.STARTING);
		return true;
	}
	
	
	

	static Connection conn = null;
	static void initDB(String filename)
	{
        try
        {
//            String url = "jdbc:sqlite:"+filename;
//            Class.forName ("org.sqlite.JDBC").newInstance ();
//            conn = DriverManager.getConnection (url);
        	String userName = "ppsf09";
            String password = "ppsf09";
            String url = "jdbc:mysql://projects.seas.columbia.edu/4444p1";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
        }
        catch (Exception e)
        {
            System.err.println ("Cannot connect to database server");
        }

	}
	
	public void mouseChanged() {
		notifyListeners(GameUpdateType.MOUSEMOVED);
	}
}
