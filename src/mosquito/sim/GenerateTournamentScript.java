package mosquito.sim;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;


public class GenerateTournamentScript {
	static class Pair
	{
		String p1;
		String p2;
		public Pair(String p1, String p2)
		{
			this.p1 = p1;
			this.p2 = p2;
		}
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return p1.hashCode()+p2.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return (p1.equals(((Pair) obj).p1) && p2.equals(((Pair) obj).p2) ) || (p1.equals(((Pair) obj).p2) && p2.equals(((Pair) obj).p1) );
		}
	}
	static void makeSeeds()
	{
		Random r = new Random();
					for(int i = 0;i<20;i++)
					{
						Statement s;
						try {
							s = conn.createStatement();	
							s.execute("INSERT INTO seeds (seed) VALUES (\""+r.nextLong()+"\")");
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			
		}
	}
	static String tpl = "PLAYER;NUMLIGHTS;BOARD;results.db";
	static void generate()
	{
		String[] players = {"mosquito.g1.WalkTowardsTheLight",
				"mosquito.g2.G2Dragonfly",
				"mosquito.g3.G3Player",
				"mosquito.g4.Exterminator",
				"mosquito.g5.G5Player",
				"mosquito.g6.MosquitoBuster",
				"mosquito.g7.ZapperPlayer"};
		String[] boards = {
				"Blank",
				"BoxesAndLines",
				"Cage",
				"Caged",
				"G5.H",
				"Steps",
				"W",
				"spiralMaze",
				"Sunrise",
				"Satan++"
		};
		String[] lights = {
				"3",
				"5",
				"8",
				"13",
				"100"
		};
		Random r =new Random();
//		String[] seeds = new String 
		for(String b : boards)
		{
			long[] seeds = new long[20];
			for(String p : players)
			{
			
					for(String l : lights)
					{
						Statement s;
						try {
							s = conn.createStatement();	
							s.execute("INSERT INTO jobs (job) VALUES (\""+(tpl.replace("PLAYER", p).
									replace("NUMLIGHTS", l).replace("BOARD", b))+"\")");
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
				}
			}
		}
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
	public static void main(String[] args) {
		initDB("results.db");
//		generate();
		makeSeeds();
//			generate2v2s();
//			enerate5v5s();
//			generate8p();
//			generate9p();
//			generate2v2v2v2();
//			generateSingles();
		
	}
}
