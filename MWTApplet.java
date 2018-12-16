import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Polygon;
import java.awt.Graphics2D;

import java.applet.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.ArrayList;
import java.util.List;

/*
<applet code="MWTApplet" width=800 height=800>
</applet>
*/

/*
 * 
 * Author: Anthony Turcios
 *
 * MWTApplet class
 *
 * This class represents an applet that will
 * allow a user to:
 *
 * 		1) Enter the number of vertices
 * 		2) Click on the white region to enter vertices
 *			2.a) vertices must be entered in CLOCK WISE order
 *			2.b) vertices must form convex polygon
 *		3) hit "Clear" or "Calculate MWT"
 * 		4) once calculation is done, hit "Clear" to enter new polygon
 */
public class MWTApplet extends Applet implements ActionListener {

	/* UI Components
	******************/

	// label for vertices text field	
	private JLabel nVertLbl, mwtValLbl;
	// used for holding the number of vertices
	private TextField nVerTxt;
	// set num vertices, clear all, calculate Min Weight Triangulation
	private Button setBtn, clrBtn, mwtBtn;
	// two main panels, the controls and the tabels
	private JPanel controls, tblsPnl;
	
	// custom canvas class that handles animations
	// essentially a JPanel with some more functionality
	private Canvas center;

	// tables that will store the cost and solutions
	// these tables are used for displaing in applet
	private JTable solTbl;
	private JTable costTbl;

	// actual tables used to store 
	// find the solution
	private int nVertices = -1;
	private int[][] sol;
	private double[][] cost;


	/* Graphics Setup
	 *****************/

	public void init() {
		setButtons();
		setPanels();

		//add the panels and buttons to the applet
		this.setLayout(new BorderLayout());
		this.add("North", this.controls);
		this.add("Center", this.center);
		this.add("South", this.tblsPnl);
	}

	/*
	 * Instantiate all buttons and set their action
	 * listener.
	 */
	public void setButtons() {
		this.clrBtn = new Button("Clear");
		this.clrBtn.addActionListener(this);

		this.mwtBtn = new Button("Calculate MWT");
		this.mwtBtn.addActionListener(this);

		this.setBtn = new Button("Set Vertices");
		this.setBtn.addActionListener(this);
	}

	/*
	 * Instantiate the panels on the Applet
	 * and add other components to the panels
	 *
	 * Add a mouse click listener to the center
	 * canvas. This will update after each click
	 * 
	 */
	public void setPanels() {

		this.nVertLbl = new JLabel("Number of Vertices");
		this.nVerTxt = new TextField(4);
		this.mwtValLbl = new JLabel("MWT value:");

		// controls will have these
		// components added in this order
		this.controls = new JPanel();
		this.controls.setLayout(new FlowLayout());
		this.controls.add(this.nVertLbl);
		this.controls.add(this.nVerTxt);
		this.controls.add(this.setBtn);
		this.controls.add(this.clrBtn);
		this.controls.add(this.mwtBtn);
		this.controls.add(this.mwtValLbl);
		this.controls.setBackground(Color.LIGHT_GRAY);

		// center canvas requires a mouse listener
		this.center = new Canvas();
		this.center.init();
		this.center.setBackground(Color.WHITE);		
		this.center.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent a) {
				Vertex clicked = new Vertex(a.getX(), a.getY());
				center.vertices.add(clicked);
				repaint();
			}
			@Override
            public void mouseReleased(MouseEvent a) {}
			@Override
			public void mouseEntered(MouseEvent a) {}
			@Override
            public void mousePressed(MouseEvent a) {}
			@Override
            public void mouseExited(MouseEvent a) {}
		});

		// table panel will hold two tables side by side
		this.tblsPnl = new JPanel();
		this.tblsPnl.setLayout(new GridLayout(1, 2));
		this.tblsPnl.setPreferredSize(new Dimension(200, 150));
	}

	/*
	 * required when implenting the ActionListener
	 * 
	 * 3 cases (buttons to be clicked)
	 */
	public void actionPerformed(ActionEvent a) {

		// case 1: set vertices
		if(a.getSource() == setBtn) {
			setTables();
		} 
		// case 2: clear button
		else if(a.getSource() == clrBtn) {
			resetPanels();
		} 
		// case 3: calc min weight triangulation
		else if(a.getSource() == mwtBtn) {
			minWeightTriangulation();
		} 
	}

	/*
	 * Method to set up the tables for both the 
	 * costs and solutions 
	 *
	 * NOTE that the headers in the columns correspond
	 * to the vertices. e.g. i = 1 <--> V1
	 *
	 * For simplicity i left out naming the rows
	 */
	private void setTables() {
		// set the border and title around tables
		this.tblsPnl.setBorder(
			BorderFactory.createTitledBorder(
        		BorderFactory.createEtchedBorder(), 
        		"Cost Table/ Solution Table",
        		TitledBorder.CENTER,
        		TitledBorder.TOP
        	)
        );
		// get the vertices in the text field
		this.nVertices = Integer.parseInt(this.nVerTxt.getText());

		// made for storing headers of the columns (indices)
		String[] colHeaders = generateHeaders();

		// default table model is used to make the columns of the tables
		DefaultTableModel ctblMdl = new DefaultTableModel(colHeaders, this.nVertices);
		DefaultTableModel stblMdl = new DefaultTableModel(colHeaders, this.nVertices);
		
		// instantiate the cost table
		this.costTbl = new JTable(ctblMdl);
		this.costTbl.setShowGrid(true);
		// specify how to select elements from the table
		this.costTbl.setSelectionModel(new DefaultListSelectionModel());
		// specifies how many elements of the table can be selected at a time
		// using the default value
		this.costTbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// scroll pane will hold the table
		JScrollPane scroll1 = new JScrollPane(this.costTbl);

		// the solution table is made exactly the same as the cost table
		this.solTbl = new JTable(stblMdl);
		this.solTbl.setShowGrid(true);
		this.solTbl.setSelectionModel(new DefaultListSelectionModel());
		this.solTbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scroll2 = new JScrollPane(this.solTbl);

		// set the set button was his remove any old table
		this.tblsPnl.removeAll();
		//add both tables cost first then solution table
		this.tblsPnl.add(scroll1);
		this.tblsPnl.add(scroll2);

		// this will ensure that anything previously in this panel
		// is marked as "not okay" to everything is removed
		this.tblsPnl.invalidate();
		// however we want to keep the most recent
		// tables that are added to the panel
		// and validate them (i.e. lines 195-196 should stay)
		this.tblsPnl.getTopLevelAncestor().validate();
		repaint();
	}

	/*
	 * method for clearing ui components
	 */
	private void resetPanels() {

		// clear the text in txtarea
		this.mwtValLbl.setText("");
		
		// clear the table
		this.tblsPnl.removeAll();
		this.tblsPnl.repaint();
		
		// clear the vertices
		this.center.clearVertices();
		this.center.clearEdges();
		
		// reset the vertices, text, and repaint!
		this.nVertices = -1;
		this.nVerTxt.setText("");
		this.center.repaint();
	}

	/*
	 * Using the choices stored in the sol table
	 * actually draw the solution on the polygon
	 */
	private void drawSolution(int i, int j) {
		// ensure that i > j
		// this way we are always considering 
		// upper right hand side of the solution
		// and the diagonal is never hit
		if(Math.abs(i-j) > 1) {
			// from solution choose the edges that minimize the 
			// perimeter from triangle i, sol[i][j], j
			Vertex v1 = new Vertex(i, this.sol[i][j]);
			Vertex v2 = new Vertex(j, this.sol[i][j]);

			// edges to be drawn in by the paint immediately method
			this.center.edges.add(v1);
			this.center.edges.add(v2);
			this.center.paintImmediately(this.center.getBounds());

			//recurse on next edges
			drawSolution(i, this.sol[i][j]);
			drawSolution(this.sol[i][j], j);
		}
		// regardless the canvas needs to be repainted in each call
		// after each recursive call we need to paint the canvas again
		// otherwise edges will go missin
		this.center.paintImmediately(this.center.getBounds());
	}

	/*
	 * Using the value entered into the text field
	 * ensure that there is a value and initialize the
	 * solution and cost tables
	 */
	private void initTables() {
		if(this.nVertices == -1) {return;}
		this.cost = new double[this.nVertices][this.nVertices];
		this.sol = new int[this.nVertices][this.nVertices];
	}

	/* MWT Algorithm & Solution
	***************************/
	
	/*
	 * Calculates the distance from v1 to v2
	 */
	private double distance(Vertex v1, Vertex v2) {
		double distSquare = Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2);
		return Math.sqrt(distSquare);
	}

	/*
	 * Calculates the perimeter of the triangle formed by i, k, j
	 */
	private double perimeter(int i, int k, int j) {
		Vertex v1, v2, v3;
		v1 = this.center.vertices.get(i);
		v2 = this.center.vertices.get(j);
		v3 = this.center.vertices.get(k);

		double weight = distance(v1, v2) + distance(v2, v3) + distance(v3, v1);
		return weight;
	}

	/*
	 * Actual algorithm for Minimum Weight Triangulation
	 *
	 * NOTE: the user of sleep(...) is meant to
	 * make the animation slower, these values can be adjusted
	 */
	private void minWeightTriangulation() {
		if(this.nVertices == -1) {return;}
		if(this.nVertices < 3) {
			System.out.println("You need at least a triangle..");
			return;
		}

		initTables();

		// consider the edges along the polygon
		for(int gap = 0; gap < this.nVertices; ++gap) {

			for(int i = 0, j = gap; j < this.nVertices; ++i, ++j) {
				
				// only consider edges and vertices that 
				// form a triangle  
				if(j < i+2) {
					this.cost[i][j] = 0.0;

					// put into cost table
					this.costTbl.setValueAt(0, i, j);
					this.costTbl.paintImmediately(this.costTbl.getBounds());
					sleep(200);

				} else {
					// set the cost to be impossible value
					this.cost[i][j] = Double.MAX_VALUE;

					// put high value into cost table
					this.costTbl.setValueAt(100000, i, j);
					this.costTbl.paintImmediately(this.costTbl.getBounds());
					sleep(200);

					// now actually use the recurrence relation
					for(int k = i+1; k < j; ++k) {
						double p = this.cost[i][k] + this.cost[k][j] + perimeter(i, k, j);
						if(p < this.cost[i][j]) {
							
							// put min cost in tbl
							this.cost[i][j] = p;
							this.costTbl.setValueAt((int)p, i, j);
							this.costTbl.paintImmediately(this.costTbl.getBounds());
							sleep(300);

							//put the choice into the sol table
							this.sol[i][j] = k;
							this.solTbl.setValueAt(k, i, j);
							this.solTbl.paintImmediately(this.solTbl.getBounds());
							sleep(300);
						}

						// clear any edges that have been previously drawn
						this.center.clearEdges();
						this.center.edges.add(new Vertex(i, k));
						this.center.edges.add(new Vertex(k, j));
						this.center.paintImmediately(this.center.getBounds());
						sleep(400);
                    	this.center.clearEdges();
					}
				}
			}
		}
		// now actually draw the solution
		drawSolution(0, this.nVertices-1);
		this.mwtValLbl.setText("MWT value: " + (int)this.cost[0][this.nVertices-1]);
	}

	/*
	 * Used to generate the headers of the columns of
	 * each JTable
	 */
	private String[] generateHeaders() {
		if(this.nVertices == -1) {
			return null;
		}
		String[] result = new String[this.nVertices];
		for(int i = 0; i < this.nVertices; ++i) {
			result[i] = ""+i;
		}
		return result;
	}

	/*
	 * Used to slow down animation by having the main thread sleep 
	 * for n milliseconds
	 */
	private void sleep(int n) {
		try {
        	Thread.sleep(n);
    	} catch(Exception e) {}
	}
}
