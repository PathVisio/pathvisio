package graphics;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import colorSet.*;

import data.*;

public class GmmlLegend extends Canvas implements MouseListener, MouseMoveListener, PaintListener {
	
	GmmlDrawing drawing;
	GmmlGex gmmlGex;
	public int colorSetIndex;
	double[] extremes;
	
	public GmmlLegend(Composite parent, int style)
	{
		super(parent, style);
			
		createContents();
		
		addMouseMoveListener(this);
		addMouseListener(this);
		addPaintListener(this);
	}
	
	public void setDrawing(GmmlDrawing drawing)
	{
		this.drawing = drawing;
	}
	
	public void setGmmlGex(GmmlGex gmmlGex)
	{
		this.gmmlGex = gmmlGex;
	}
	
	GradientCanvas gradients;
	CriteriaCanvas criteria;
	Label title;
	Group gg;
	Group cg;
	
	public void createContents()
	{	
		setLayout(new GridLayout(1, false));
		
		title = new Label(this, SWT.CENTER);
		gg = new Group(this, SWT.SHADOW_IN);
		cg = new Group(this, SWT.SHADOW_IN);
		gradients = new GradientCanvas(gg, SWT.NONE);
		gradients.setLegend(this);
		criteria = new CriteriaCanvas(cg, SWT.NONE);
		criteria.setLegend(this);
		
		GridData gGrid = new GridData(GridData.FILL_BOTH);
		gGrid.heightHint = 150;
		gg.setLayoutData(gGrid);
		cg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END));
		
		title.setText("Legend");
		gg.setText("Gradients");
		cg.setText("Criteria");
		
		gg.setLayout(new FillLayout());
		cg.setLayout(new FillLayout());

		title.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gg.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		cg.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gradients.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		criteria.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		this.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		Control[] controls = getChildren();
		for(int i = 0; i < controls.length; i++)
		{
			controls[i].setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}
		pack();
		
		setSize(110, getSize().y);
		setVisible(false);
	}
	

	Vector<GmmlColorSetObject> colorSetObjects;
	GmmlColorSet colorSet;
	public void paintControl (PaintEvent e)
	{	
		if(drawing != null) {
			colorSetIndex = drawing.colorSetIndex;
			gmmlGex = drawing.gmmlVision.gmmlGex;
		}
		if(colorSetIndex > -1)
		{			
			Rectangle r = getClientArea();
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			e.gc.drawRectangle(r.x, r.y, r.width - 1, r.height -1);
			
			colorSet = (GmmlColorSet)gmmlGex.colorSets.get(colorSetIndex);
			colorSetObjects = colorSet.colorSetObjects;
			
			setExtremeValues();
			
			criteria.resetContents();

			layout();
			
			gradients.redraw();
		}
	}
	
	private class CriteriaCanvas extends Canvas
	{
		GmmlLegend legend;
		
		public CriteriaCanvas(Composite parent, int style)
		{
			super(parent, style);
			setLayout(new GridLayout(2, false));
		}
		
		public void setLegend(GmmlLegend legend)
		{
			this.legend = legend;
		}
		
		final static int CLABEL_SIZE = 10;
		public void resetContents()
		{
			Control[] controls = getChildren();
			for(int i = 0; i < controls.length; i++)
			{
				System.out.println(controls[i]);
				controls[i].dispose();
			}
			if(legend.colorSetObjects != null)
			{
				CLabel noCritMet = new CLabel(this, SWT.SHADOW_IN);
				Label noCritMetLabel = new Label(this, SWT.LEFT);
				noCritMetLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				noCritMetLabel.setText("No criteria met");
				
				CLabel geneNotFound = new CLabel(this, SWT.SHADOW_IN);
				Label geneNotFoundLabel = new Label(this, SWT.LEFT);
				geneNotFoundLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				geneNotFoundLabel.setText("Gene not found");
				
				noCritMet.setBackground(new Color(getDisplay(), colorSet.color_no_criteria_met));
				geneNotFound.setBackground(new Color(getDisplay(), colorSet.color_gene_not_found));
				
				GridData clabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
				clabelGrid.widthHint = CLABEL_SIZE;
				clabelGrid.heightHint = CLABEL_SIZE;
				geneNotFound.setLayoutData(clabelGrid);
				noCritMet.setLayoutData(clabelGrid);
				
				layout();
			}
		}
	}
	private class GradientCanvas extends Canvas implements PaintListener
	{
		GmmlLegend legend;
		
		public GradientCanvas(Composite parent, int style)
		{
			super(parent, style);
			addPaintListener(this);
		}
		
		public void setLegend(GmmlLegend legend)
		{
			this.legend = legend;
		}

		public void paintControl (PaintEvent e)
		{
			if(legend.colorSetObjects != null)
			{
				ListIterator it = legend.colorSetObjects.listIterator(legend.colorSetObjects.size());
				while(it.hasPrevious())
				{
					GmmlColorSetObject cs = (GmmlColorSetObject)it.previous();
					if(cs instanceof GmmlColorGradient) {
						GmmlColorGradient cg = (GmmlColorGradient)cs;
						drawColorGradient(e, cg);
					}
				}
			}
		}
		
		final static int LABEL_HEIGHT = 20;
		final static int BAR_WIDTH = 20;
		final static int MARGIN_VERTICAL = 10;
		final static int MARGIN_HORIZONTAL = 5;
		final static int MARKER_LENGTH = 5;
		public void drawColorGradient(PaintEvent e, GmmlColorGradient cg)
		{
			Color c = getBackground();
			RGB oldBackground = c.getRGB();
			
			double min = extremes[0];
			double max = extremes[1];
			
			if((float)max == (float)min) {
				return;
			}
			
			// Get region to draw
			Point size = getSize();
			int barHeight = size.y - MARGIN_VERTICAL;
			int start = (int)(((cg.valueStart - min) / (max - min)) * barHeight);
			int end = (int)(((cg.valueEnd - min) / (max - min)) * barHeight);

			if(cg.valueStart == (float)min) {
				start = MARGIN_VERTICAL;
			}
			if(cg.valueEnd == (float)max) {
				end = end - MARGIN_VERTICAL;
			}
			
			int n = end - start;
			
			// Fill squares with color cg.getColor()
			for(int i = start; i < end; i++) {
				double colorValue = cg.valueStart + (i-start) * (cg.valueEnd - cg.valueStart) / n;
				RGB rgb = cg.getColor(colorValue);
				if(rgb != null) {
					c = new Color(getShell().getDisplay(), rgb);
					e.gc.setBackground(c);
					e.gc.fillRectangle(MARGIN_HORIZONTAL, i, BAR_WIDTH, 1);
				}
			}
			int markerCenter = BAR_WIDTH + MARGIN_HORIZONTAL;
			e.gc.drawLine(markerCenter - MARKER_LENGTH, start, markerCenter + MARKER_LENGTH, start);
			e.gc.drawLine(markerCenter - MARKER_LENGTH, end, markerCenter + MARKER_LENGTH, end);
			e.gc.setBackground(new Color(getShell().getDisplay(), oldBackground));
			e.gc.drawString(Double.toString(cg.valueStart), markerCenter + MARKER_LENGTH, start);
			e.gc.drawString(Double.toString(cg.valueEnd), markerCenter + MARKER_LENGTH, end);
//			e.gc.drawRectangle(MARGIN_HORIZONTAL, MARGIN_VERTICAL, BAR_WIDTH, end);
			c.dispose();
		}
		
	}
	
	private void setExtremeValues()
	{
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		
		Iterator it = colorSetObjects.iterator();
		while(it.hasNext())
		{
			GmmlColorSetObject cs = (GmmlColorSetObject)it.next();
			if(cs instanceof GmmlColorGradient)
			{
				GmmlColorGradient cg = (GmmlColorGradient)cs;
				max = Math.max(cg.valueEnd, max);
				min = Math.min(cg.valueStart, min);
			}
		}
		extremes =  new double[] { min, max };
	}
	
	public void adjustToZoom(double factor)
	{
		Point p = getLocation();
		p.x *= factor;
		p.y *= factor;
	}

	boolean isDragging;
	public void mouseDoubleClick(MouseEvent arg0) {	}
	
	public void mouseDown(MouseEvent e) {
		isDragging = true;
		prevX = e.x;
		prevY = e.y;
	}
	
	public void mouseUp(MouseEvent arg0) {
		isDragging = false;	
	}

	int prevX;
	int prevY;
	
	static final int RW = 4;
	Cursor c;
	int cursorStyle = SWT.CURSOR_ARROW;
	
	public void mouseMove(MouseEvent e) {
		if(isDragging)
		{
			int addX = 0;
			int addY = 0;
			int locX = getLocation().x;
			int locY = getLocation().y;
			
			switch(cursorStyle)
			{
			case SWT.CURSOR_ARROW:
				Point p = getLocation();
				setLocation(p.x + e.x - prevX, p.y + e.y - prevY);
				return;
			case SWT.CURSOR_SIZEN:
				break;
			case SWT.CURSOR_SIZES:
				addY = e.y - prevY;
				break;
			case SWT.CURSOR_SIZEE:
				addX = e.x - prevX;
				break;
			case SWT.CURSOR_SIZEW:
				break;
			case SWT.CURSOR_SIZESE:
				addX = e.x - prevX;
				addY = e.y - prevY;
			}
			
			Point p = getSize();
			setSize(p.x + addX, p.y + addY);
			setLocation(locX, locY);
			
			prevX = e.x;
			prevY = e.y;
		}
		else
		{
			if(c != null)
			{
				c.dispose();
			}
			
			cursorStyle = SWT.CURSOR_ARROW;
			
			Point s = new Point(getClientArea().width, getClientArea().height);
//			Rectangle north = new Rectangle(0, -RW, s.x, 2*RW);
			Rectangle south = new Rectangle(0, s.y - RW, s.x, 2*RW);
//			Rectangle west = new Rectangle(-RW, 0, 2*RW, s.y);
			Rectangle east = new Rectangle(s.x - RW, 0, 2*RW, s.y);
			Rectangle se = new Rectangle(s.x - RW, s.y - RW, 2*RW, 2*RW);
			
//			if(north.contains(e.x, e.y))
//			{
//				cursorStyle = SWT.CURSOR_SIZEN;
//			}
			if(south.contains(e.x, e.y))
			{
				cursorStyle = SWT.CURSOR_SIZES;
			}
			if(east.contains(e.x, e.y))
			{
				cursorStyle = SWT.CURSOR_SIZEE;
			}
//			if(west.contains(e.x, e.y))
//			{
//				cursorStyle = SWT.CURSOR_SIZEW;
//			}
			if(se.contains(e.x, e.y))
			{
				cursorStyle = SWT.CURSOR_SIZESE;
			}
			
			c = new Cursor(this.getShell().getDisplay(), cursorStyle);
			this.setCursor(c);
		}
	}
}
