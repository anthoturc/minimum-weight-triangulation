import java.awt.*;
import java.awt.geom.*;
import java.awt.Graphics2D;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

/*
 *
 * Author: Anthony Turcios
 *
 * Canvas class
 * 
 * Class that will handle drawing the 
 * polygon and updating the animation
 * as the vertices/edges are added to the
 * panel
 *
 * NOTE: This class assumes that the points are being 
 * added in CLOCK WISE order
 */
public class Canvas extends JPanel {

	public List<Vertex> edges;
	public List<Vertex> vertices;

	public void init() {
		this.edges = new ArrayList<>();
		this.vertices = new ArrayList<>();
	}

	/*
	 * Each time the canvas is repainted 
	 * this is the method that is called
	 * it handles drawing the edges of the polygon
	 */
	@Override
	public void paintComponent(Graphics graphic) {
		super.paintComponent(graphic);
		Graphics2D g2D = (Graphics2D)graphic;

		drawPolygon(g2D);
		drawEdges(g2D);
	}

	/*
	 * Given vertices, will draw the convex
	 * polygon formed by them
	 */
	private void drawPolygon(Graphics2D g2D) {

		g2D.setColor(Color.BLUE);
		g2D.setStroke(new BasicStroke(1));

		/* 
		 * iterate throguht list of vertices
		 * and draw lines from one vertex to another
		 * this is easy because we assume that 
		 * edges have been added in a counter clockwise 
		 * manner 
		 */
		int vLen = this.vertices.size();
		for(int i = 0; i < vLen; ++i) {
			Vertex vert = this.vertices.get(i);
			Vertex nextVert = new Vertex(0,0);

			//make the next vertice the following
			//one in the vertice list for drawing
			if(vLen - 1 > i) {
				nextVert = this.vertices.get(i+1);
			} 
			//reconnect the last vertice to the first
			else {
				nextVert = this.vertices.get(0);
			}
			g2D.drawLine(vert.x, vert.y, nextVert.x, nextVert.y);
			//make a cirlce for each vertex
			Shape c = new Ellipse2D.Double(vert.x - 1, vert.y - 1, 9, 9);
			g2D.fill(c);
			//label each vertex
			g2D.setColor(Color.BLACK);
			g2D.setFont(new Font("Dialog", Font.BOLD, 14)); 
			g2D.drawString("V" + i + " (" + vert.x + ", " + vert.y + ")", vert.x-10, vert.y-10);
			g2D.setFont(new Font("Dialog", Font.PLAIN, 12)); 
			g2D.setColor(Color.BLUE);
		}
	}

	/*
	 * Given the set of edges that were passed
	 * from the Applet, draw them over the polygon in
	 * the Canvas
	 */
	private void drawEdges(Graphics2D g2D) {
		/* 	
		 * iterate through list of edges
		 * recall that each edge is stored from its position
		 * in the solution table
		 */
		int eLen = edges.size();
		Vertex edge, vert1, vert2;
		for(int i = 0; i < eLen; ++i) {
			edge = this.edges.get(i);
			vert1 = this.vertices.get(edge.x);
			vert2 = this.vertices.get(edge.y);
			g2D.setColor(Color.RED);
			g2D.setStroke(new BasicStroke(2));
			g2D.drawLine(vert1.x, vert1.y, vert2.x, vert2.y);
			g2D.setStroke(new BasicStroke(1));
			g2D.setColor(Color.BLUE);
		}
	}

	public void clearEdges() {
		this.edges = new ArrayList<>();
	}

	public void clearVertices() {
		this.vertices = new ArrayList<>();
	}	
}