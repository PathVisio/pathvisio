package graphics;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import colorSet.*;

import data.*;
import data.GmmlGex.Sample;

public class GmmlLegend extends Canvas implements MouseListener, MouseMoveListener, PaintListener {
	
	GmmlDrawing drawing;
	GmmlGex gmmlGex;
	public int colorSetIndex;
	ArrayList<Integer> diffSamples;
	HashMap extremes;
	boolean isMovable;
	
	public GmmlLegend(Composite parent, int style, boolean movable)
	{
		super(parent, style);
			
		createContents();
		
		addPaintListener(this);
		
		isMovable = movable;
		if(isMovable)
		{
			addMouseMoveListener(this);
			addMouseListener(this);
		}
	}
	
	public GmmlLegend(Composite parent, int style)
	{
		this(parent, style, true);
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
	CriteriaComposite criteria;
	SampleComposite samples;
	
	Label title;
	Group gg;
	Group cg;
	Group sg;
	public void createContents()
	{	
		setLayout(new GridLayout(1, false));
		
		title = new Label(this, SWT.CENTER);
		sg = new Group(this, SWT.SHADOW_IN);
		gg = new Group(this, SWT.SHADOW_IN);
		cg = new Group(this, SWT.SHADOW_IN);
		gradients = new GradientCanvas(gg, SWT.NONE);
		gradients.setLegend(this);
		criteria = new CriteriaComposite(cg, SWT.NONE);
		criteria.setLegend(this);
		samples = new SampleComposite(sg, SWT.NONE);
		samples.setLegend(this);
		
		GridData gGrid = new GridData(GridData.FILL_BOTH);
		gGrid.heightHint = 200;
//		gGrid.widthHint = 100;
		gg.setLayoutData(gGrid);
		cg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END));
		sg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		title.setText("Legend");
		gg.setText("Gradients");
		cg.setText("Criteria");
		sg.setText("Samples");
		
		gg.setLayout(new FillLayout());
		cg.setLayout(new FillLayout());
		sg.setLayout(new FillLayout());

		title.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gg.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		cg.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		sg.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		this.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		Control[] controls = getChildren();
		for(int i = 0; i < controls.length; i++)
		{
			controls[i].setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}
		
		pack();
		
//		setSize(110, getSize().y);
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
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			e.gc.drawRectangle(r.x, r.y, r.width - 1, r.height -1);
			
			colorSet = (GmmlColorSet)gmmlGex.colorSets.get(colorSetIndex);
			colorSetObjects = colorSet.colorSetObjects;
			
			setDiffGradients();
			setExtremeValues();
			
			samples.resetContents();
			criteria.resetContents();
			
			layout();
			
			gradients.redraw();
		}
	}
	
	private class SampleComposite extends Composite 
	{
		GmmlLegend legend;
		Image sampleImage;
		
		public SampleComposite(Composite parent, int style)
		{
			super(parent, style);
			setLayout(new GridLayout(1, false));
			setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}
		
		public void setLegend(GmmlLegend legend)
		{
			this.legend = legend;
		}
				
		public void resetContents()
		{			
			Control[] controls = getChildren();
			for(int i = 0; i < controls.length; i++)
			{
				controls[i].dispose();
			}
			if(legend.colorSetObjects != null)
			{
				setSampleImage();
				Label sampleLabel = new Label(this, SWT.FLAT);
				sampleLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				sampleLabel.setImage(sampleImage);
				
				int i = 0;
				for(Sample s : colorSet.useSamples)
				{
					i++;
					Label l = new Label(this, SWT.FLAT);
					l.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
					l.setText(i + ": " + s.name);
					l.pack();
				}
				layout();
			}
		}
		
		final static int MARGIN = 5;
		public void setSampleImage()
		{
			Font f = new Font(getDisplay(), "Arial", 6, SWT.NONE);
			
			int marginY = MARGIN / 5;
			Point imageSize = new Point(getClientArea().width - 2*MARGIN, 
					Math.max(15, (int)(getClientArea().width * 0.15)) - 2*marginY);
			if(sampleImage != null)
			{
				sampleImage.dispose();
			}
			sampleImage = new Image(getDisplay(), imageSize.x, imageSize.y);
			int nr = colorSet.useSamples.size();
			GC imageGc = new GC(sampleImage);
			imageGc.setFont(f);
			imageSize.x -= 1;
			imageSize.y -= 1;
			String exampleId = "Gene ID";
			Point stringSize = imageGc.textExtent(exampleId);
			Rectangle drawArea = new Rectangle(imageSize.x / 2, 0, imageSize.x /2, imageSize.y);
			imageGc.drawString(exampleId, drawArea.x / 2 - stringSize.x / 2, imageSize.y / 2 - stringSize.y / 2 );
			imageGc.drawRectangle(0, 0, imageSize.x / 2, imageSize.y);
			for(int i = 0; i < nr; i++)
			{
				Rectangle r = new Rectangle(drawArea.x + drawArea.width * i / nr,
						drawArea.y, drawArea.width / nr, drawArea.height);
				imageGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
				imageGc.drawRectangle(r.x, r.y, r.width, r.height);
				Point numberSize = imageGc.textExtent(Integer.toString(i + 1));
				imageGc.drawString(Integer.toString(i + 1), r.x + r.width / 2 - numberSize.x / 2,
						r.height / 2 - numberSize.y / 2, true);
			}
			imageGc.dispose();
			f.dispose();
		}		
	}
	
	private class CriteriaComposite extends Composite
	{
		GmmlLegend legend;
		
		public CriteriaComposite(Composite parent, int style)
		{
			super(parent, style);
			setLayout(new GridLayout(2, false));
			setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
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
			setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}
		
		public void setLegend(GmmlLegend legend)
		{
			this.legend = legend;
		}

		public void paintControl (PaintEvent e)
		{
			if(legend.colorSetObjects != null)
			{
				//Divide canvas in diffSamples.size() columns
				Point size = getSize();
				HashMap<Integer, Rectangle> rectangles = new HashMap<Integer, Rectangle>();
				int n = 0;
				for(int i : diffSamples)
				{
					rectangles.put(i, new Rectangle(
							n * size.x / diffSamples.size(),
							0,
							size.x / diffSamples.size(),
							size.y));
					n++;
				}
				ListIterator it = legend.colorSetObjects.listIterator(legend.colorSetObjects.size());
				while(it.hasPrevious())
				{
					GmmlColorSetObject cs = (GmmlColorSetObject)it.previous();
					if(cs instanceof GmmlColorGradient) {
						GmmlColorGradient cg = (GmmlColorGradient)cs;
						drawColorGradient(e, cg, rectangles.get(cg.getDataColumn()));
					}
				}
			}
		}
		
		final static int LABEL_HEIGHT = 20;
		final static int BAR_WIDTH = 10;
		final static int MARGIN_VERTICAL = 10;
		final static int MARGIN_HORIZONTAL = 15;
		final static int MARKER_LENGTH = 4;
		final static int LABEL_FONT_SIZE = 8;
		public void drawColorGradient(PaintEvent e, GmmlColorGradient cg, Rectangle r)
		{
			Color c = getBackground();
			RGB oldBackground = c.getRGB();
			
			double[] minmax = (double[])extremes.get(cg.getDataColumn());
			double min = minmax[0];
			double max = minmax[1];
			
			if((float)max == (float)min) {
				return;
			}
			
			// Get region to draw
			int barHeight = r.height - MARGIN_VERTICAL;
			int start = (int)(((cg.valueStart - min) / (max - min)) * barHeight);
			int end = (int)(((cg.valueEnd - min) / (max - min)) * barHeight);
			
			int xPos = MARGIN_HORIZONTAL + r.x;

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
					e.gc.fillRectangle(xPos, i, BAR_WIDTH, 1);
				}
			}
			
			Font f = new Font(e.display, "Arial", LABEL_FONT_SIZE, SWT.NONE);
			e.gc.setFont(f);
			
			int markerCenter = BAR_WIDTH + xPos;
			e.gc.drawLine(markerCenter - MARKER_LENGTH, start, markerCenter + MARKER_LENGTH, start);
			e.gc.drawLine(markerCenter - MARKER_LENGTH, end, markerCenter + MARKER_LENGTH, end);
			e.gc.setBackground(new Color(getShell().getDisplay(), oldBackground));
			e.gc.drawString(Double.toString(cg.valueStart), markerCenter + MARKER_LENGTH, start);
			e.gc.drawString(Double.toString(cg.valueEnd), markerCenter + MARKER_LENGTH, end);
			
			//Draw labels
			int dataColumn = cg.getDataColumn();
			String label = "";
			if(dataColumn == -1)
			{
				label = "All samples";
			}
			else
			{
				label = gmmlGex.samples.get(dataColumn).name;
			}
			
			Point labelRegion = new Point(r.height, MARGIN_HORIZONTAL);
			Point labelSize = e.gc.textExtent(label);
			
			Transform t = new Transform(e.display);
			t.rotate(-90);
			e.gc.setTransform(t);
			e.gc.drawString(label, -(labelRegion.x / 2) - labelSize.x / 2,
								   r.x + (labelRegion.y / 2) - (labelSize.y / 2));
			t.rotate(90);
			e.gc.setTransform(t);
			
			t.dispose();
			c.dispose();
			f.dispose();
		}
		
	}
	
	private void setExtremeValues()
	{		
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		
		extremes = new HashMap<Integer, double[]>();
		
		Iterator it = colorSetObjects.iterator();
		while(it.hasNext())
		{
			GmmlColorSetObject cs = (GmmlColorSetObject)it.next();
			if(cs instanceof GmmlColorGradient)
			{
				GmmlColorGradient cg = (GmmlColorGradient)cs;
				if(!extremes.containsKey(cg.getDataColumn()))
				{
					extremes.put(cg.getDataColumn(), new double[] {min, max});
				}
				double[] d = (double[])extremes.get(cg.getDataColumn());
				d[1] =  Math.max(cg.valueEnd, d[1]);
				d[0] = Math.min(cg.valueStart,d[0]);
				extremes.put(cg.getDataColumn(), d);
			}
		}
	}
	
	private void setDiffGradients()
	{
		diffSamples = new ArrayList<Integer>();
		for(GmmlColorSetObject o : colorSetObjects)
		{
			if(o instanceof GmmlColorGradient)
			{
				int sampleId = ((GmmlColorGradient)o).getDataColumn();
				if(!diffSamples.contains(new Integer(sampleId)))
				{
					diffSamples.add(new Integer(sampleId));
				}
			}
		}
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
