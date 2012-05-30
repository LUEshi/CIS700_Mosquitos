/* 
 * 	$Id: BoardPanel.java,v 1.1 2007/09/06 14:51:49 johnc Exp $
 * 
 * 	Programming and Problem Solving
 *  Copyright (c) 2007 The Trustees of Columbia University
 */
package mosquito.sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public final class BoardPanel extends JPanel implements MouseListener,
		MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Point2D MouseCoords;

	private Board board;

	private GameEngine engine;
	public void recalculateDimensions() {
		int my_w = this.getWidth();
		int my_h = this.getHeight();
		int d = Math.min(my_w, my_h);
		d -= 10;
		if (d > 0)
			Board.pixels_per_pixel = (100
					* Board.pixels_per_meter )/ d;
		repaint();
	}
	Cursor curCursor;

	public BoardPanel() {
		this.setPreferredSize(new Dimension(600, 600));
		this.setBackground(Color.white);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	Rectangle2D boardBox = null;
	public static Line2D debugLine = null;

	/**
	 * Makes sure that there are no enclosed/unreachable
	 * 
	 * @return
	 */
	public boolean validateReachable() {
		int[][] blobs = new int[(int) Board.toScreenSpace(101)][(int) Board
				.toScreenSpace(101)];
		BufferedImage im = new BufferedImage((int) Board.toScreenSpace(101),
				(int) Board.toScreenSpace(101), BufferedImage.TYPE_INT_RGB);
		this.paint(im.getGraphics());
		Raster ra = im.getRaster();
		int nblob = 1;

		for (int i = 0; i < (int) Board.toScreenSpace(101); i++) {
			for (int j = 0; j < (int) Board.toScreenSpace(101); j++) {
				double[] px = null;
				blobs[i][j] = -1;
				px = ra.getPixel(i, j, px);
				if (px[0] == 0 && px[1] == 0 && px[2] == 0) {
					if (i > 0 && blobs[i - 1][j] > 0) {
						blobs[i][j] = blobs[i - 1][j];

						// Check for merge corner-case
						if (j > 0 && blobs[i][j - 1] > 0
								&& blobs[i][j - 1] != blobs[i][j]) {
							// These need to be merged
							blobs = replaceVals(blobs, blobs[i][j],
									blobs[i][j - 1]);
						}
					} else if (j > 0 && blobs[i][j - 1] > 0) {
						blobs[i][j] = blobs[i][j - 1];
						// Add to the mass

						// Check for merge corner-case
						if (i > 0 && blobs[i - 1][j] > 0
								&& blobs[i - 1][j] != blobs[i][j]) {
							// These two need to be merged
							blobs = replaceVals(blobs, blobs[i][j],
									blobs[i - 1][j]);
						}
					} else {
						// This pixel isn't neighboring other skin-like pixels.
						// Make it a "new blob"
						blobs[i][j] = nblob;
						nblob++;
					}
				}
			}
		}
//		debugBlobs(blobs, im);
		int blobNum = blobs[0][0];
		int i = 0;
		while(blobNum < 0)
		{
			i++;
			blobNum = blobs[i][i];
		}
		for(i = 0;i<blobs.length;i++)
		{
			for(int j = 0; j<blobs[0].length;j++)
			{
				if(blobs[i][j] != blobNum && blobs[i][j] != -1)
				{
					return false;
				}
			}
		}
		return true;
	}
	private void debugBlobs(int[][] blobs,BufferedImage im)
	{
		WritableRaster rw = im.getRaster();
		for(int i = 0; i<blobs.length;i++)
		{
			for(int j = 0; j<blobs[0].length;j++)
			{
					double[] px = new double[] {blobs[i][j]*4,blobs[i][j]*4,blobs[i][j]*4};
					rw.setPixel(i, j, px);
			}
		}
		im.setData(rw);
		ImageIcon i = new ImageIcon(im);
		JFrame f = new JFrame();
		f.setSize(blobs.length, blobs[0].length);
		f.add(new JLabel(i));
		f.setVisible(true);
	}
	/**
	 * Replaces all values of needle with r in blobs
	 * 
	 * @param blobs
	 *            Int array to search and replace in
	 * @param needle
	 *            What to search for
	 * @param r
	 *            What to replace it with
	 * @return Modified blobs array with needle replaced with r
	 */
	private int[][] replaceVals(int[][] blobs, int needle, int r) {
		for (int i = 0; i < blobs.length; i++) {
			for (int j = 0; j < blobs[0].length; j++) {
				if (blobs[i][j] == needle)
					blobs[i][j] = r;
			}
		}
		return blobs;
	}

	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(Color.black);
		if (board != null)
		{
			boardBox = new Rectangle2D.Double(0, 0, Board.toScreenSpace(board
					.getWidth()), Board.toScreenSpace(board.getHeight()));
			g2D.fillRect((int) boardBox.getX(), (int) boardBox.getY(),
					(int) boardBox.getWidth(), (int) boardBox.getHeight());
		}
		g2D.setColor(Color.red);
		if (engine != null && board.getLights() != null) {
			for (Light l : board.getLights()) {
				g2D.setColor(Color.YELLOW);
				if (l.isOn(engine.getCurrentRound())) {
					g2D.drawOval((int) Board.toScreenSpace(l.getX() - 20),
							(int) Board.toScreenSpace(l.getY() - 20),
							(int) Board.toScreenSpace(40),
							(int) Board.toScreenSpace(40));
				}
			}
		}
		if (lastLine != null) {
			Line2D trans = new Line2D.Double(Board.toScreenSpace(lastLine
					.getP1()), Board.toScreenSpace(lastLine.getP2()));
			g2D.draw(trans);
		}
		if (engine != null && board.getLights() != null) {

			for (Light l : board.getLights()) {
				g2D.setColor(Color.YELLOW);
				if (l.isOn(engine.getCurrentRound())) {
					g2D.fillOval((int) Board.toScreenSpace(l.getX() - .5),
							(int) Board.toScreenSpace(l.getY() - .5),
							(int) Board.toScreenSpace(1),
							(int) Board.toScreenSpace(1));
				} else
					g2D.drawOval((int) Board.toScreenSpace(l.getX() - .5),
							(int) Board.toScreenSpace(l.getY() - .5),
							(int) Board.toScreenSpace(1),
							(int) Board.toScreenSpace(1));
			}

		}
		if (engine != null && board.getCollector() != null) {
			g2D.setColor(Color.GREEN);
			g2D.fillOval(
					(int) Board.toScreenSpace(board.getCollector().getX()
							- Collector.RADIUS / 2),
					(int) Board.toScreenSpace(board.getCollector().getY()
							- Collector.RADIUS / 2),
					(int) Board.toScreenSpace(Collector.RADIUS),
					(int) Board.toScreenSpace(Collector.RADIUS));
		}
		
		if (board != null)
			for (Line2D line : board.walls) {
				Line2D trans = new Line2D.Double(Board.toScreenSpace(line
						.getP1()), Board.toScreenSpace(line.getP2()));

				if (line == selectedLine) {
					g2D.setStroke(new BasicStroke(3));
					g2D.setColor(Color.cyan);
					g2D.draw(trans);
					
					
				}
				g2D.setStroke(new BasicStroke(2));
				g2D.setColor(new Color(229,52,70));
				g2D.draw(trans);
			}
		g2D.setStroke(new BasicStroke(1));
		if (board != null && board.getMosquitos() != null)
			for (Mosquito m : board.getMosquitos()) {
				if (!m.caught) {
					g2D.setColor(Color.cyan);
					g2D.drawLine(
							(int) Board.toScreenSpace(m.location.getX() - .5),
							(int) Board.toScreenSpace(m.location.getY() - .5),
							(int) Board.toScreenSpace(m.location.getX() + .5),
							(int) Board.toScreenSpace(m.location.getY() + .5));
					g2D.drawLine(
							(int) Board.toScreenSpace(m.location.getX() - .5),
							(int) Board.toScreenSpace(m.location.getY() + .5),
							(int) Board.toScreenSpace(m.location.getX() + .5),
							(int) Board.toScreenSpace(m.location.getY() - .5));
				}

			}
		if (debugLine != null) {
			g2D.setColor(Color.orange);
			g2D.drawLine((int) Board.toScreenSpace(debugLine.getX1()),
					(int) Board.toScreenSpace(debugLine.getY1()),
					(int) Board.toScreenSpace(debugLine.getX2()),
					(int) Board.toScreenSpace(debugLine.getY2()));

		}
		if (curCursor != null)
			setCursor(curCursor);
	}

	public BoardPanel(GameEngine eng, boolean editable) {
		setEngine(eng);
		setBoard(engine.getBoard(), editable);
		addMouseListener(this);
		addMouseMotionListener(this);
		this.editable = editable;
	}

	public void setEngine(GameEngine eng) {
		engine = eng;
	}

	private boolean editable;

	public void setBoard(Board b, boolean editable) {
		board = b;
		this.setPreferredSize(new Dimension((int) Board.toScreenSpace(b
				.getWidth()), (int) Board.toScreenSpace(b.getHeight())));

		this.editable = editable;

		boardBox = new Rectangle2D.Double(0, 0, Board.toScreenSpace(board
				.getWidth()), Board.toScreenSpace(board.getHeight()));
		repaint();
		revalidate();
	}

	Line2D selectedLine = null;

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		if (!editable)
			return;
		if (lastLine != null)
		{
			board.walls.add(lastLine);
			if(!validateReachable())
			{
				board.walls.remove(lastLine);
				String s = "Error: The line you just drew creates a closed space; it has been removed";
				JOptionPane.showMessageDialog(this, s, "Illegal Line drawn", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		lastLine = null;
		lastLineStart = null;
	}

	public void mouseClicked(MouseEvent e) {
		if (!boardBox.contains(e.getPoint()))
			return;

		if (editable) {
			Rectangle2D testPt = new Rectangle2D.Double(Board.fromScreenSpace(e
					.getX() - 5), Board.fromScreenSpace(e.getY() - 5),
					Board.fromScreenSpace(8), Board.fromScreenSpace(8));
			boolean hit = false;
			for (Line2D line : board.walls) {
				if (line.intersects(testPt)) {
					selectedLine = line;
					hit = true;
				}
			}
			if (!hit) {
				selectedLine = null;
			}
		} else if (board.isInteractive()) {
			Rectangle2D testPt = new Rectangle2D.Double(Board.fromScreenSpace(e
					.getX() - 5), Board.fromScreenSpace(e.getY() - 5),
					Board.fromScreenSpace(8), Board.fromScreenSpace(8));
			// Look for modifiers
			if (e.isControlDown() && e.isAltDown()) {
				Collector c = new Collector(Board.fromScreenSpace(e.getX()),
						Board.fromScreenSpace(e.getY()));
				board.setCollector(c);
			} else if (e.isAltDown()) {
				if (e.isShiftDown()) {
					for (int i = 0; i < 100; i++) {
						board.mosquitos.add(new Mosquito(Board
								.fromScreenSpace(e.getPoint())));
					}
				}
				Mosquito m = new Mosquito(Board.fromScreenSpace(e.getPoint()));
				board.mosquitos.add(m);
				System.out
						.println("Mosquitos count: " + board.mosquitos.size());
			} else if (e.isControlDown()) {
				Light l = new Light(Board.fromScreenSpace(e.getX()),
						Board.fromScreenSpace(e.getY()), 0, 0, 0);
				l.forced_on = true;
				board.lights.add(l);
			} else {
				// Look to see if there is a light here to toggle
				for (Light l : board.lights) {
					if (testPt.contains(l.getLocation()))
						l.forced_on = !l.forced_on;
				}
			}
		}
		repaint();
	}

	Line2D lastLine = null;
	Point lastLineStart = null;

	public void mouseDragged(MouseEvent e) {
		if (!editable)
			return;
		if (!boardBox.contains(e.getPoint()))
			return;
		if (lastLineStart == null)
			lastLineStart = e.getPoint();
		else if (lastLineStart != null
				&& e.getPoint().distance(lastLineStart) > 3) {
			lastLine = new Line2D.Double(Board.fromScreenSpace(lastLineStart),
					Board.fromScreenSpace(e.getPoint()));
		}
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		MouseCoords = Board.fromScreenSpace(e.getPoint());
		if (engine != null)
			engine.mouseChanged();
		if (!editable)
			return;
		if (!boardBox.contains(e.getPoint()))
			return;
		Rectangle2D testPt = new Rectangle2D.Double(e.getX() - 5, e.getY() - 5,
				8, 8);
		boolean hit = false;
		for (Line2D line : board.walls) {
			if (line.intersects(testPt)) {
				hit = true;
				curCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			}
		}
		if (!hit) {
			curCursor = Cursor.getDefaultCursor();
		}
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void removeSelectedLine() {
		if (selectedLine != null && board != null) {
			board.walls.remove(selectedLine);
			selectedLine = null;
			repaint();
		}
	}
}
