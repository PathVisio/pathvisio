package graphics;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
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

public class GmmlLegend extends ScrolledComposite {
	
	public int colorSetIndex;
	ArrayList<Integer> diffSamples;
	HashMap extremes;
		
	public GmmlLegend(Composite parent, int style)
	{
		super(parent, style);
			
		createContents();
	}
	
	Composite topComposite;
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
		topComposite = new Composite(this, SWT.NULL);
		topComposite.setLayout(new GridLayout(1, false));
		
		setContent(topComposite);
		setExpandHorizontal(true);
		setExpandVertical(true);
		
		title = new Label(topComposite, SWT.CENTER);
		sg = new Group(topComposite, SWT.SHADOW_IN);
		gg = new Group(topComposite, SWT.SHADOW_IN);
		cg = new Group(topComposite, SWT.SHADOW_IN);
		gradients = new GradientCanvas(gg, SWT.NONE);
		gradients.setLegend(this);
		criteria = new CriteriaComposite(cg, SWT.NONE);
		criteria.setLegend(this);
		samples = new SampleComposite(sg, SWT.NONE);
		samples.setLegend(this);

		gg.setLayoutData(new GridData(GridData.FILL_BOTH));
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
	}
	
	Vector<GmmlColorSetObject> colorSetObjects;
	GmmlColorSet colorSet;

	static final String FONT = "arial narrow";
	static final int FONTSIZE = 8;

	public void resetContents()
	{
		if(!GmmlGex.isConnected()) return;
		
		colorSetIndex = GmmlGex.getColorSetIndex();
		if(colorSetIndex < 0) return;
			
		colorSet = (GmmlColorSet)GmmlGex.getColorSets().get(colorSetIndex);
		colorSetObjects = colorSet.colorSetObjects;
		
		setDiffGradients();
	
		samples.resetContents();
		gradients.resetContents();
		criteria.resetContents();
		topComposite.layout(true, true);
		
		setMinSize(topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
		
	private class SampleComposite extends Composite
	{
		GmmlLegend legend;
		Canvas sampleCanvas;
		
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
			
			sampleCanvas = getSampleCanvas(this, SWT.NONE);

			if(legend.colorSetObjects != null)
			{				
				int i = 0;
				for(Sample s : colorSet.useSamples)
				{
					i++;
					Label l = new Label(this, SWT.FLAT);
					l.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
					l.setText(i + ": " + s.getName());
				}
			}
		}
		
		final static int MARGIN = 5;
		final static int SAMPLE_IMAGE_HEIGHT = GmmlGeneProduct.INITIAL_HEIGHT;
		
		private Canvas getSampleCanvas(Composite parent, int style) {
			final Canvas c = new Canvas(parent, style);
			
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = SAMPLE_IMAGE_HEIGHT + 1;
			c.setLayoutData(gd);
			
			c.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			c.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					GC gc = e.gc;
					Font f = new Font(getDisplay(), FONT, FONTSIZE, SWT.NONE);
					
					int nr = colorSet.useSamples.size();
				
					gc.setFont(f);
					String exampleId = "Gene";
					Point stringSize = gc.textExtent(exampleId);
					
					Point p = ((Canvas)(e.widget)).getSize();
					Rectangle drawArea = new Rectangle(0, 0, p.x, p.y);
					drawArea.width = drawArea.width;
					drawArea.height = SAMPLE_IMAGE_HEIGHT;
					
					Rectangle sampleArea = new Rectangle(0, 0, drawArea.width, drawArea.height);
					sampleArea.width = (int)Math.ceil(GmmlGpColor.COLOR_AREA_RATIO * drawArea.width);
					if(nr > 0) sampleArea.width -= sampleArea.width % nr;
					
					int stringSpace = drawArea.width - sampleArea.width;
					
					//If sample numbers don't fit, steal space from gene label if possible
					Point sampleSize = gc.textExtent(Integer.toString(nr));
					int sampleSpace = sampleArea.width / nr;
					if(sampleSize.x > sampleSpace) { 		
						if(stringSpace > stringSize.x) {
							int steal = sampleSize.x * nr - sampleArea.width;
							if(!(stringSpace - steal > stringSize.x)) steal = stringSpace - stringSize.x;
							stringSpace -= steal;
							sampleArea.width += steal;
						}
					}
										
					gc.drawString(exampleId, stringSpace / 2 - stringSize.x / 2, sampleArea.height / 2 - stringSize.y / 2 );
					gc.drawRectangle(0, 0, stringSpace - 1, sampleArea.height);
					
					sampleArea.x += stringSpace - 1;
					for(int i = 0; i < nr; i++)
					{
						Rectangle r = new Rectangle(sampleArea.x + i * sampleArea.width / nr,
								sampleArea.y, sampleArea.width / nr, sampleArea.height);
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
						gc.drawRectangle(r.x, r.y, r.width, r.height);
						Point numberSize = gc.textExtent(Integer.toString(i + 1));
						gc.drawString(Integer.toString(i + 1), r.x + r.width / 2 - numberSize.x / 2,
								r.height / 2 - numberSize.y / 2, true);
					}

					f.dispose();
				}
			});
			return c;
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
			setGridData();
		}
		
		public void setLegend(GmmlLegend legend)
		{
			this.legend = legend;
		}
		
		public void resetContents() {
			setGridData();
			redraw();
		}
		
		public void setGridData() {
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = diffSamples != null ? (MAX_BAR_HEIGHT + 20) * diffSamples.size() : 40;
			setLayoutData(gGrid);
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
			int barHeight = Math.min(r.height - MARGIN_VERTICAL -LABEL_WIDTH, MAX_BAR_HEIGHT - MARGIN_VERTICAL);
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
				label = GmmlGex.getSamples().get(dataColumn).getName(); break;
			}
			Point labelSize = e.gc.textExtent(label);
			e.gc.drawString(label, (end - start) / 2 - labelSize.x / 2, 
					yStart - barHeight - labelSize.y / 2, true);	
			
			c.dispose();
			f.dispose();
	}

	/**
	 * Sets the number of gradients to draw in the legend. That is
	 * the number of gradients applying to a different sample
	 */
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
}
