/* 
 * 	$Id: Board.java,v 1.6 2007/11/28 16:30:18 johnc Exp $
 * 
 * 	Programming and Problem Solving
 *  Copyright (c) 2007 The Trustees of Columbia University
 */
package mosquito.sim;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Stack;


import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.xerces.parsers.DOMParser;
import dom.Writer;

/**
 * @author Jon Bell
 * 
 */
public final class Board {
	public static final int pixels_per_meter = 150;
	public static int pixels_per_pixel = 25;
	private int width;
	private int height;
	public boolean impersonated = false;
	HashSet<Mosquito> mosquitos = new HashSet<Mosquito>();
	public GameEngine engine;
	private HashMap<Double, HashMap<Double, Double>> cache; // this should be
															// indexed by
															// the hash, so
															// that we can
															// reuse the
															// hash :)
	private int lightsHash;
	public int mosquitosCaught;
	private int cacheDate = -1;
	private Collector collector;

	private double round(double n) {
		return Math.round(100 * n) / 100;
	}

	public double lightStrengthAt(Point2D p, Point2D p2) {
		if (p.getX() < 0 || p.getY() < 0 || p.getX() > 99 || p.getY() > 99)
			return 0;
		Line2D between1 = new Line2D.Double(p, p2);
		for (Line2D w : walls) {
			if (between1.intersectsLine(w))
					return -1;
		}
		
		double strongest = 21;
		// First check for cache hit
		double i = round(p.getX());
		double j = round(p.getY());
		if (cache.containsKey(i) && cache.get(i).containsKey(j)) {
			cacheHits++;
			return cache.get(i).get(j);
		}
		cacheMisses++;
		try {
			for (Light l : lights) {
				boolean obscured = false;
				if (l.isOn(engine.getCurrentRound())
						&& l.getLocation().distance(p) <= 20) {
					Line2D between = new Line2D.Double(p.getX()
							, p.getY(), l
							.getLocation().getX() , l
							.getLocation().getY());					
					for (Line2D w : walls) {
						if (between.intersectsLine(w)
//								|| between2.intersectsLine(w)
								) {
							obscured = true;
						}
					}
					if (!obscured && l.getLocation().distance(p) < strongest) {
						BoardPanel.debugLine = between;
						strongest = l.getLocation().distance(p);
					}
				}
			}
		} catch (ConcurrentModificationException e) {

		}
		if (!cache.containsKey(i))
			cache.put(i, new HashMap<Double, Double>());
		if (!cache.get(i).containsKey(j))
			cache.get(i).put(j, 20 - strongest);
		return 20 - strongest;
	}

	public double lightStrengthAt(double i, double j, Point2D p2) {
		return lightStrengthAt(new Point2D.Double(i, j),p2);
	}

	public void setCollector(Collector collector) {
		this.collector = collector;
	}

	public Collector getCollector() {
		return collector;
	}

	public boolean lightsChanged() {
		HashSet<String> states = new HashSet<String>();
		if (cacheDate != engine.getCurrentRound())
			return true;
		try {
			for (Light l : lights) {
				states.add(l.getX() + "," + l.getY()
						+ (l.isOn(engine.getCurrentRound()) ? " ON" : " OFF"));
			}
		} catch (ConcurrentModificationException e) {
			return true;
		}
		int hash = states.hashCode();
		if (hash != lightsHash) {
			lightsHash = hash;
			return true;
		}
		return false;
	}

	public void clearCache() {
		cache = new HashMap<Double, HashMap<Double, Double>>();
		cacheDate = engine.getCurrentRound();
	}

	public static Point2D toScreenSpace(Point2D point2d) {
		Point2D clone = new Point2D.Double();
		clone.setLocation(toScreenSpace(point2d.getX()),
				toScreenSpace(point2d.getY()));
		return clone;
	}

	public HashSet<Mosquito> getMosquitos() {
		return mosquitos;
	}

	public int cacheHits = 0;
	public int cacheMisses = 0;

	public double getDirectionOfLight(Point2D p) {
//		if (lightsChanged())
//			clearCache();
//		int angle = -1;
//		double brightest = 0;
//		// It's possible that there's a route to the light, we should check!
//		for (double a = 0; a < Math.PI / 2; a += Math.PI / 120) {
//			double r = 1;
//			double cos = Math.cos(a);
//			double sin = Math.sin(a);
//			// cutoff for rounding issues
//			if (cos < 0.00000000001)
//				cos = 0;
//			if (sin < 0.00000000001)
//				sin = 0;
//			if (cos != 0 && sin != 0) {
//
//				double i = (double) (p.getX() + (r * cos));
//				double j = (double) (p.getY() - (r * sin));
//				double sample = lightStrengthAt(i, j,p);
//				if (sample > brightest) {
//					angle = (int) (a * 180 / Math.PI);
//					brightest = sample;
//				}
//
//				i = (p.getX() - (r * cos));
//				j = (p.getY() - (r * sin));
//				sample = lightStrengthAt(i, j,p);
//				if (sample > brightest) {
//					angle = 180 - (int) (a * 180 / Math.PI);
//					brightest = sample;
//				}
//
//				// 180+theta
//				i = (p.getX() - (r * cos));
//				j = (p.getY() + (r * sin));
//				sample = lightStrengthAt(i, j,p);
//				if (sample > brightest) {
//					angle = 180 + (int) (a * 180 / Math.PI);
//					brightest = sample;
//				}
//
//				// 360-theta
//				i = (p.getX() + (r * cos));
//				j = (p.getY() + (r * sin));
//				sample = lightStrengthAt(i, j,p);
//				if (sample > brightest) {
//					angle = 360 - (int) (a * 180 / Math.PI);
//					brightest = sample;
//				}
//			}
//		}
		double angle = -1;
		double max_dist = 21;
		for(Light l : lights)
		{
			if(l.getLocation().distance(p) < max_dist && l.isOn(engine.getCurrentRound()))
			{
				//See if we have a straight path to the light
				boolean obscured = false;
					Line2D between = new Line2D.Double(p.getX()
							, p.getY(), l
							.getLocation().getX() , l
							.getLocation().getY());					
					for (Line2D w : walls) {
						if (between.intersectsLine(w)
//								|| between2.intersectsLine(w)
								) {
							obscured = true;
						}
					}
					if (!obscured) {
//						BoardPanel.debugLine = between;
						max_dist = l.getLocation().distance(p);
						angle = Math.atan(Math.abs((l.getLocation().getY() - p.getY())/(l.getLocation().getX() - p.getX()))) * 180/Math.PI;
						
						
						if(l.getLocation().getX() > p.getX() && l.getLocation().getY() < p.getY())
						{
							angle = angle;
						}
						else if(l.getLocation().getX() < p.getX() && l.getLocation().getY() > p.getY())
						{
							angle = 180 + angle;
						}
						else if(l.getLocation().getX() < p.getX() && l.getLocation().getY() < p.getY())
						{
							angle = 180 - angle;
						}
						else if(l.getLocation().getX() > p.getX() && l.getLocation().getY() > p.getY())
						{
							angle = 360 - angle;
						}
				
					}
			}
		}
		if (angle > 0)
			return angle;
		return GameConfig.random.nextInt(360);
	}

	public static double MOSQUITO_EPSILON = 0.5;

	public boolean collidesWithWall(Point2D p) {
		Rectangle2D tester = new Rectangle2D.Double(
				p.getX() - MOSQUITO_EPSILON, p.getY() - MOSQUITO_EPSILON,
				MOSQUITO_EPSILON * 2, MOSQUITO_EPSILON * 2);
		return collidesWithWall(tester);
	}
	public boolean collidesWithWall(Rectangle2D p) {
	
		for (Line2D l : walls) {
			if (l.intersects(p)) {
				return true;
			}
		}
		return false;
	}
	public void createMosquitos(int n) {
		mosquitos = new HashSet<Mosquito>();
		for (int i = 0; i < n; i++) {
			Point2D p = new Point2D.Double(
					1 + GameConfig.random.nextDouble() * 98,
					1 + GameConfig.random.nextDouble() * 98);
			while (collidesWithWall(p))
				p = new Point2D.Double(1 + GameConfig.random.nextDouble() * 98,
						1 + GameConfig.random.nextDouble() * 98);
			mosquitos.add(new Mosquito(p));
		}
	}
	
	
	Set<Line2D> walls;

	public Set<Light> getLights() {
		return lights;
	}

	Set<Light> lights;

	public void setLights(Set<Light> lights) {
		this.lights = lights;
	}

	public static double fromScreenSpace(double v) {
		return v * pixels_per_pixel / pixels_per_meter;
	}

	public static double toScreenSpace(double v) {
		return v * pixels_per_meter / pixels_per_pixel;
	}

	public static Point2D fromScreenSpace(Point2D p) {
		Point2D r = new Point2D.Double();
		r.setLocation(fromScreenSpace(p.getX()), fromScreenSpace(p.getY()));
		return r;
	}

	public HashSet<Line2D> getWalls() {
		HashSet<Line2D> ret = new HashSet<Line2D>();
		for (Line2D t : walls)
			ret.add(new Line2D.Double((t.getX1()), (t.getY1()), (t.getX2()), (t
					.getY2())));
		return ret;
	}

	public Board() {
		this.width = 100;
		this.height = 100;
		walls = new HashSet<Line2D>();
		init();
	}

	public Board(int width, int height) {
		this.width = 100;
		this.height = 100;
		walls = new HashSet<Line2D>();
		init();
	}

	public Board(String file) throws IOException {
		load(new File(file));

	}

	public void load(File f) throws IOException {
		DOMParser parser = new DOMParser();
		if (f == null) {
			System.out.println("File is null!");
		}
		try {
			FileInputStream fis = new FileInputStream(f);
			InputSource s = new InputSource(fis);
			parser.parse(s);
		} catch (SAXException e) {
			throw new IOException("XML Parsing exception: " + e);
		}
		try {
			Document doc = parser.getDocument();
			Node dim = doc.getElementsByTagName("DIMENSIONS").item(0);
			walls.clear();
			NamedNodeMap dim_attrs = dim.getAttributes();
			width = Integer.parseInt(dim_attrs.getNamedItem("width")
					.getNodeValue());
			height = Integer.parseInt(dim_attrs.getNamedItem("height")
					.getNodeValue());

			init();

			NodeList nodes = doc.getElementsByTagName("WALL");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node e = nodes.item(i);
				NamedNodeMap attrs = e.getAttributes();
				double x1 = Double.valueOf(attrs.getNamedItem("x_1")
						.getNodeValue());
				double x2 = Double.valueOf(attrs.getNamedItem("x_2")
						.getNodeValue());
				double y1 = Double.valueOf(attrs.getNamedItem("y_1")
						.getNodeValue());
				double y2 = Double.valueOf(attrs.getNamedItem("y_2")
						.getNodeValue());
				walls.add(new Line2D.Double(x1, y1, x2, y2));
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Problem loading board xml file: " + e);
		}

		// sanityCheck();

	}

	public void save(File f) throws IOException {

		// sanityCheck();

		Document xmlDoc = new DocumentImpl();
		Element root = xmlDoc.createElement("BOARD");

		Element dim = xmlDoc.createElement("DIMENSIONS");
		dim.setAttribute("width", Integer.toString(width));
		dim.setAttribute("height", Integer.toString(height));

		root.appendChild(dim);
		Element cells = xmlDoc.createElement("WALLS");
		for (Line2D wall : walls) {
			Element e = xmlDoc.createElement("WALL");
			e.setAttribute("x_1", "" + (wall.getX1()));
			e.setAttribute("y_1", "" + ((wall.getY1())));
			e.setAttribute("x_2", "" + ((wall.getX2())));
			e.setAttribute("y_2", "" + ((wall.getY2())));
			cells.appendChild(e);
		}
		root.appendChild(cells);
		xmlDoc.appendChild(root);

		Writer writer = new Writer();

		FileOutputStream os = new FileOutputStream(f);
		writer.setOutput(os, null);
		writer.write(xmlDoc);
		os.close();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private void init() {

	}

	public boolean inBounds(int x, int y) {
		return (x >= 0 && x < width) && (y >= 0 && y < height);
	}

	boolean interactive = false;

	public boolean isInteractive() {
		return interactive;
	}

	public void setInteractive(boolean b) {
		interactive = b;
	}

}
