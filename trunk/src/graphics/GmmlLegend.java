package graphics;


import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import util.SwtUtils;
import colorSet.GmmlColorCriterion;
import colorSet.GmmlColorGradient;
import colorSet.GmmlColorSet;
import colorSet.GmmlColorSetObject;
import colorSet.GmmlColorGradient.ColorValuePair;
import data.GmmlGex;
import data.GmmlGex.Sample;

public class GmmlLegend extends Composite implements MouseListener, 
													 MouseMoveListener, 
													 PaintListener 
													 {
	
	GmmlDrawing drawing;
	GmmlGex gmmlGex;
	public int colorSetIndex;
	ArrayList<Integer> diffSamples;
	HashMap extremes;
	boolean isMovable;
	
	Point lastFitSize;
	
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
	GridData gGrid;
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
		
		gGrid = new GridData(GridData.FILL_HORIZONTAL);
		gGrid.heightHint = 40;
		gg.setLayoutData(gGrid);
		cg.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		layout();
		setVisible(false);
	}

	Vector<GmmlColorSetObject> colorSetObjects;
	GmmlColorSet colorSet;
	public void paintControl (PaintEvent e)
	{	
		if(colorSetIndex > -1)
		{			
			Rectangle r = getClientArea();
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			e.gc.drawRectangle(r.x, r.y, r.width - 1, r.height -1);
		}
		else if(isCustomSize)
		{
			samples.resetContents();
		}
	}

	static final String FONT = "arial narrow";
	static final int FONTSIZE = 8;
	public void resetContents()
	{
		if(drawing != null) {
			colorSetIndex = drawing.colorSetIndex;
			gmmlGex = drawing.gmmlVision.gmmlGex;
		}
		if(colorSetIndex > -1 && gmmlGex.colorSets.size() > 0)
		{
			colorSet = (GmmlColorSet)gmmlGex.colorSets.get(colorSetIndex);
		} else {
			return;
		}
		colorSetObjects = colorSet.colorSetObjects;
		
		setDiffGradients();
		
		samples.resetContents();
		criteria.resetContents();
		gradients.redraw();
		
		if(!isCustomSize && !isDragging)
		{
			gGrid.heightHint = (MAX_BAR_HEIGHT + 20) * diffSamples.size();
			layout(true);
			pack(true);
			lastFitSize = getSize();
		}
		samples.resetContents();
		redraw();
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
				Label sampleLabel = new Label(this, SWT.FLAT);
				
				int i = 0;
				for(Sample s : colorSet.useSamples)
				{
					i++;
					Label l = new Label(this, SWT.FLAT);
					l.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
					l.setText(i + ": " + s.getName());
				}
				
				if(!isCustomSize)
				{
					layout(true);
					pack(true);
				}
				setSampleImage();
				sampleLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				sampleLabel.setImage(sampleImage);
				pack();
				layout();
			}
			
		}
		
		final static int MARGIN = 5;
		final static int SAMPLE_IMAGE_HEIGHT = GmmlGeneProduct.INITIAL_HEIGHT;
		public void setSampleImage()
		{
			Font f = new Font(getDisplay(), FONT, FONTSIZE, SWT.NONE);
			
			int nr = colorSet.useSamples.size();
			Point imageSize = new Point(sg.getClientArea().width - MARGIN, 
					SAMPLE_IMAGE_HEIGHT);
			if(sampleImage != null)
			{
				sampleImage.dispose();
			}
			sampleImage = new Image(getDisplay(), imageSize.x, imageSize.y);
			
			GC imageGc = new GC(sampleImage);
			imageGc.setFont(f);
			String exampleId = "Gene ID";
			Point stringSize = imageGc.textExtent(exampleId);
			
			Rectangle drawArea = sampleImage.getBounds();
			drawArea.height -= 1;
			drawArea.width = (int)Math.ceil(GmmlGpColor.COLOR_AREA_RATIO * drawArea.width);
			if(nr > 0) drawArea.width -= drawArea.width % nr;

			int w = sampleImage.getBounds().width - drawArea.width;
			imageGc.drawString(exampleId, w / 2 - stringSize.x / 2, drawArea.height / 2 - stringSize.y / 2 );
			imageGc.drawRectangle(0, 0, w - 1, drawArea.height);
			
			drawArea.x += w - 1;
			for(int i = 0; i < nr; i++)
			{
				Rectangle r = new Rectangle(drawArea.x + i * drawArea.width / nr,
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
				Color c = null;
				Image image = null;
								
				//Draw CLabel for every criterion
				for(GmmlColorSetObject co : colorSet.colorSetObjects)
				{
					if(!(co instanceof GmmlColorCriterion)) continue; //skip objects other than criretia
					GmmlColorCriterion cc = (GmmlColorCriterion)co;
					c = SwtUtils.changeColor(c, cc.getColor(), getDisplay());
					createCriterionLabel(cc.getName(), c);
				}
				
				//Draw label for special criteria ('no gene found', 'no criteria met')
				String[] specialLabels = {"No criteria met", "Gene not found", "No data found"};
				RGB[] specialColors = {colorSet.color_no_criteria_met, colorSet.color_no_gene_found,
						colorSet.color_no_data_found};
				
				for(int i = 0; i < specialColors.length; i++)
				{
					c = SwtUtils.changeColor(c, specialColors[i], getDisplay());
					createCriterionLabel(specialLabels[i], c);
				}
				
				//This label requires an image
				Label multipleData = new Label(this, SWT.LEFT | SWT.FLAT);
				multipleData.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				GridData clabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
				clabelGrid.widthHint = CLABEL_SIZE;
				clabelGrid.heightHint = CLABEL_SIZE;
				multipleData.setLayoutData(clabelGrid);
				
				if(image != null && !image.isDisposed())
				{
					image.dispose();
				}
				image = new Image(getDisplay(), CLABEL_SIZE, CLABEL_SIZE);
				GC imageGc = new GC(image);
				imageGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
				imageGc.drawRectangle(1, 1, CLABEL_SIZE - 3, CLABEL_SIZE - 3);
				imageGc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
				imageGc.drawRectangle(0, 0, CLABEL_SIZE - 1, CLABEL_SIZE - 1);
				multipleData.setImage(image);
				Label multipleDataLabel = new Label(this, SWT.LEFT);
				multipleDataLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				multipleDataLabel.setText("Gene maps to multiple ids");
				
				layout();
				c.dispose();
				imageGc.dispose();
			}
		}
			
		private void createCriterionLabel(String text, Color c)
		{
			GridData clabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			clabelGrid.widthHint = CLABEL_SIZE;
			clabelGrid.heightHint = CLABEL_SIZE;
			
			CLabel cLabel = new CLabel(this, SWT.SHADOW_IN);
			Label label = new Label(this, SWT.LEFT);
			
			label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			label.setText(text);
			
			cLabel.setBackground(c);
			cLabel.setLayoutData(clabelGrid);
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
				//Divide canvas in diffSamples.size() rows
				Point size = getSize();
				HashMap<Integer, Rectangle> rectangles = new HashMap<Integer, Rectangle>();
				int n = 0;
				for(int i : diffSamples)
				{
					rectangles.put(i, new Rectangle(
							0,
							n * size.y / diffSamples.size(),
							size.x,
							size.y / diffSamples.size()));
					n++;
				}
				for(GmmlColorSetObject co : colorSetObjects)
					if(co instanceof GmmlColorGradient) {
						GmmlColorGradient cg = (GmmlColorGradient)co;
						Rectangle area = rectangles.get(cg.useSample);
						if(area != null) drawColorGradient(e, cg, area);
					}
				}
			}
		}
		
		final static int LABEL_WIDTH = 20;
		final static int MAX_BAR_HEIGHT = 35;
		final static int MARGIN_VERTICAL = 20;
		final static int MARGIN_HORIZONTAL = 10;
		final static int MARKER_LENGTH = 4;
		public void drawColorGradient(PaintEvent e, GmmlColorGradient cg, Rectangle r)
		{
			Color c = null;
			RGB oldBackground = getBackground().getRGB();
			
			double[] minmax = cg.getMinMax();
			double min = minmax[0];
			double max = minmax[1];
			
			if((float)max == (float)min) {
				return;
			}
			
			// Get region to draw
			int yStart = r.y + MARGIN_VERTICAL;
			int barHeight = Math.min(r.height - MARGIN_VERTICAL, MAX_BAR_HEIGHT - MARGIN_VERTICAL);
			int start = r.x + MARGIN_HORIZONTAL;
			int end = r.width - MARGIN_HORIZONTAL;
			
			int n = end - start;
			
			// Fill squares with color cg.getColor()
			for(int i = start; i < end; i++) {
				double colorValue = min + (i-start) * (max - min) / n;
				RGB rgb = cg.getColor(colorValue);
				if(rgb != null) {
					c = SwtUtils.changeColor(c, rgb, e.display);
					e.gc.setBackground(c);
					e.gc.fillRectangle(i, yStart, 1, barHeight);
				}
			}
			
			Font f = new Font(e.display, FONT, FONTSIZE, SWT.NONE);
			e.gc.setFont(f);
			
			int markerCenter = yStart + barHeight;
			c = SwtUtils.changeColor(c, oldBackground, e.display);
			e.gc.setBackground(c);
			for(ColorValuePair cvp : cg.getColorValuePairs())
			{
				int x = (int)(start + (cvp.value - min) / (max - min) * (end - start));
				e.gc.drawLine(x, markerCenter - MARKER_LENGTH, x, markerCenter + MARKER_LENGTH);
				Point labelSize = e.gc.textExtent(Double.toString(cvp.value));
				e.gc.drawString(Double.toString(cvp.value), x - labelSize.x / 2, 
						markerCenter + labelSize.y / 2, true);
			}
			
			int dataColumn = cg.useSample;
			String label;
			switch(dataColumn) {
			case GmmlColorGradient.USE_SAMPLE_NO:
				label = ""; break;
			case GmmlColorGradient.USE_SAMPLE_ALL:
				label = "All samples"; break;
			default:
				label = gmmlGex.getSamples().get(dataColumn).getName(); break;
			}
			Point labelSize = e.gc.textExtent(label);
			e.gc.drawString(label, (end - start) / 2 - labelSize.x / 2, 
					yStart - barHeight - labelSize.y / 2, true);	
			
			c.dispose();
			f.dispose();
	}

	private void setDiffGradients()
	{
		diffSamples = new ArrayList<Integer>();
		for(GmmlColorSetObject o : colorSetObjects)
		{
			if(o instanceof GmmlColorGradient)
			{
				int sampleId = ((GmmlColorGradient)o).useSample;
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
	boolean isCustomSize;
	public void mouseDoubleClick(MouseEvent arg0) {	}
	
	public void mouseDown(MouseEvent e) {
		isDragging = true;
		prevX = e.x;
		prevY = e.y;
	}
	
	public void mouseUp(MouseEvent arg0) {
		resetContents();
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
			
			if(addX != 0 || addY != 0)
			{
				isCustomSize = true;
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
