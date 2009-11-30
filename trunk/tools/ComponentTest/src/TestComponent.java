// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.SwingUtilities;


public class TestComponent extends Component {
	Color color;

	public TestComponent(Dimension size, Color c) {
		setSize(size);
		color = c;
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	}

	public void setColor(Color c) {
		if(c != null && !c.equals(color)) {
			color = c;
			repaint();
		}
	}

	public Color getColor() { return color; }

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setColor(getColor());
		g2d.fillOval(0, 0, getSize().width, getSize().height);
	}

	public Dimension getPreferredSize() {
		return getSize();
	}

	public boolean contains(int x, int y) {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Checking " + getParent().getComponentZOrder(this));
		return new Ellipse2D.Double(0, 0, getSize().width, getSize().height).contains(x, y);
	}

	protected void processMouseMotionEvent(MouseEvent e) {
		System.out.println("Mouse Motion Event: " + e.getID() + " at " + e.getX() + ", " + e.getY());
		System.out.println("\tsource: " + e.getSource());
		if(e.getID() == MouseEvent.MOUSE_DRAGGED) {
			//Test getComponentAt performance

			Component c = getParent().getComponentAt(e.getPoint());
			System.out.println("Another component " + c + " at " + e.getPoint());
			System.out.println("\tDRAGGED");
			MouseEvent ne = SwingUtilities.convertMouseEvent(this, e, getParent());
			ne.translatePoint(-downLocation.x, -downLocation.y);
			Point p = ne.getPoint();
			setLocation(p);
			repaint();
		}
		super.processMouseMotionEvent(e);
	}

	Point downLocation;
	int oldZOrder;
	Color oldColor;
	protected void processMouseEvent(MouseEvent e) {
		System.out.println("Mouse event " + MouseEvent.MOUSE_MOVED + ", " + e.getID());
		switch(e.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			//Move to top
			oldZOrder = getParent().getComponentZOrder(this);
			getParent().setComponentZOrder(this, 0);
			//Set clicked location within component
			downLocation = e.getPoint();
			break;
		case MouseEvent.MOUSE_RELEASED:
			//getParent().setComponentZOrder(this, oldZOrder - 1);
			break;
		case MouseEvent.MOUSE_ENTERED:
			oldColor = getColor();
			setColor(Color.YELLOW);
			break;
		case MouseEvent.MOUSE_EXITED:
			setColor(oldColor);
			break;
		}
		super.processMouseEvent(e);
	}
}
