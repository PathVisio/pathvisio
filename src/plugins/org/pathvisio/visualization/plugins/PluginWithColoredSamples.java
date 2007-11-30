// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.visualization.plugins;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jdom.Element;
import org.pathvisio.data.CachedData;
import org.pathvisio.data.Gex;
import org.pathvisio.data.Sample;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.Xref;
import org.pathvisio.util.swt.SwtUtils;
import org.pathvisio.util.swt.TableColumnResizer;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetManager;

/**
 * Extend this class if you want to create a visualization plug-in where the user
 * can select which samples to visualize.
 * 
 * For an example of an implementation see {@link PluginWithColoredSamples} 
 * @author Thomas
 *
 */
public abstract class PluginWithColoredSamples extends VisualizationPlugin {	
	static final String[] useSampleColumns = { "sample", "color set" };
	static final Color LINE_COLOR_DEFAULT = Color.BLACK;
	
	private List<ConfiguredSample> useSamples = new ArrayList<ConfiguredSample>();
	private Canvas sidePanel;
	private Collection<Graphics> spGraphics;
	
	public PluginWithColoredSamples(Visualization v) {
		super(v);
		setIsConfigurable(true);
		setIsGeneric(false);
		setUseProvidedArea(true);
	}
	
	/**
	 * This method determines the area in the gene-box to use for visualization and calls
	 * {@link #drawArea(GmmlGeneProduct, Rectangle, PaintEvent, GC)} to draw the samples.
	 * If you want to visualize the data in the gene-box, implement 
	 * {@link #drawSample(visualization.plugins.PluginWithColoredSamples.ConfiguredSample, Xref, Rectangle, PaintEvent, GC)}
	 * and
	 * {@link #drawNoDataFound(visualization.plugins.PluginWithColoredSamples.ConfiguredSample, Rectangle, PaintEvent, GC)}.
	 * @see VisualizationPlugin#visualizeOnDrawing(GmmlGraphics, PaintEvent, GC)
	 */
	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if(!(g instanceof GeneProduct)) return;
		if(useSamples.size() == 0) return; //Nothing to draw
		
		GeneProduct gp = (GeneProduct) g;
		
		Shape da = getVisualization().provideDrawArea(this, g);
		Rectangle area = da.getBounds();
		
		drawArea(gp, area, g2d);
		
		Color c = gp.getPathwayElement().getColor();
		g2d.setColor(c);
		g2d.draw(area);
		
	}
	
	/**
	 * Divides the given area in a rectangle for each sample and calls
	 * {@link #drawSample(visualization.plugins.PluginWithColoredSamples.ConfiguredSample, Xref, Rectangle, PaintEvent, GC)}
	 * (when data is available) or
	 * {@link #drawNoDataFound(visualization.plugins.PluginWithColoredSamples.ConfiguredSample, Rectangle, PaintEvent, GC)}
	 * (when no data is available).
	 * @param gp The gene-product to visualize the data for
	 * @param area The area in which to draw
	 * @param g2d The graphics context on which to draw
	 */
	void drawArea(GeneProduct gp, Rectangle area, Graphics2D g2d) {
		int nr = useSamples.size();
		int left = area.width % nr; //Space left after dividing, give to last rectangle
		int w = area.width / nr;
		for(int i = 0; i < nr; i++) {
			Rectangle r = new Rectangle(
					area.x + w * i,
					area.y,
					w + ((i == nr - 1) ? left : 0), area.height);
			ConfiguredSample s = (ConfiguredSample)useSamples.get(i);
			Xref idc = new Xref(gp.getPathwayElement().getGeneID(), gp.getPathwayElement().getDataSource());
			CachedData cache = Gex.getCachedData();
			if(cache == null) continue;
			
			if(s.getColorSet() == null) continue; //No ColorSet for this sample
			if(cache.hasData(idc)) 
				drawSample(s, idc, r, g2d);
			else 
				drawNoDataFound(s, area, g2d);
		}
	}
	
	/**
	 * Implement this method to perform the drawing operation for a single sample in case no data is found
	 * for a gene-product
	 * @see #visualizeOnDrawing(GmmlGraphics, PaintEvent, GC)
	 * @param s The sample that will be visualized
	 * @param area The area to draw in
	 * @param e	{@link PaintEvent} containing information about the paint
	 * @param gc Graphical context on which drawing operations can be performed
	 */
	abstract void drawNoDataFound(ConfiguredSample s, Rectangle area, Graphics2D g2d);
	
	/**
	 * Implement this method to perform the drawing operation for a single sample when data is
	 * present for the gene-product to visualize.
	 * @see #visualizeOnDrawing(GmmlGraphics, PaintEvent, GC)
	 * @see CachedData#getData(Xref)
	 * @param s The sample that will be visualized
	 * @param idc The id and code of the gene-product
	 * @param area The area to draw in
	 * @param e	{@link PaintEvent} containing information about the paint
	 * @param gc Graphical context on which drawing operations can be performed
	 */
	abstract void drawSample(ConfiguredSample s, Xref idc, Rectangle area, Graphics2D g2d);
	
	static final int SIDEPANEL_SPACING = 3;
	static final int SIDEPANEL_MARGIN = 5;
	
	/**
	 * This method implements a visualization on the side-panel, which is divided
	 * in horizontal bars, one for each selected gene-product. In the horizontal bars, the samples
	 * are visualized by calling {@link #drawArea(GmmlGeneProduct, Rectangle, PaintEvent, GC)}
	 * @see #drawSample(visualization.plugins.PluginWithColoredSamples.ConfiguredSample, Xref, Rectangle, PaintEvent, GC)
	 * @see #drawNoDataFound(visualization.plugins.PluginWithColoredSamples.ConfiguredSample, Rectangle, PaintEvent, GC)
	 * @param e
	 */
	void drawSidePanel(PaintEvent e) {
//		if(spGraphics == null) return;
//		
//		org.eclipse.swt.graphics.Rectangle area = sidePanel.getClientArea();
//		area.x += SIDEPANEL_MARGIN;
//		area.y += SIDEPANEL_MARGIN;
//		area.width -= SIDEPANEL_MARGIN * 2;
//		area.height -= SIDEPANEL_MARGIN * 2;
//		
//		int nr = 0;
//		for(Graphics g : spGraphics) if(g instanceof GeneProduct) nr++;
//
//		if(nr == 0) {
//			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
//			e.gc.fillRectangle(sidePanel.getClientArea());
//			return;
//		}
//		
//		GeneProduct[] gps = new GeneProduct[nr];
//		int x = 0;
//		for(Graphics g : spGraphics) 
//			if(g instanceof GeneProduct) gps[x++] = (GeneProduct)g;
//		
//		e.gc.setFont(e.display.getSystemFont());
//		int tw = 0;
//		for(GeneProduct g : gps) tw = Math.max(tw, e.gc.textExtent(g.getGmmlData().getTextLabel()).x);
//		tw += e.gc.getFontMetrics().getHeight();
//		
//		//Draw sample labels (vertical)
//		int lw = 0;
//		for(ConfiguredSample s : useSamples) lw = Math.max(lw, e.gc.textExtent(s.getName()).x);
//		
//		
//		Rectangle larea = new Rectangle(area.x + tw, area.y, area.width - tw, lw);
//
//		Transform t = new Transform(e.display);
//		int ns = useSamples.size();
//		
//		for(int i = 0; i < ns; i++) {
//			int tx = larea.x + i * (larea.width / ns) + larea.width / (2*ns);
//			int ty = larea.y;
//			t.translate(tx, ty);
//			t.rotate(90);
//			e.gc.setTransform(t);
//			e.gc.drawText(useSamples.get(i).getName(), 0, 0);
//			t.rotate(-90);
//			t.translate(-tx, -ty);
//			e.gc.setTransform(t);
//		}
//		t.dispose();
//		
//		area.y += lw;
//		area.height -= lw;
//		int h = area.height / nr;
//		for(int i = 0; i < nr; i++) {
//			int y = area.y + i*h;
//			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
//			e.gc.drawText(gps[i].getGmmlData().getTextLabel(), area.x, y + h / 2 - e.gc.getFontMetrics().getHeight() / 2);
//			Rectangle r = new Rectangle(area.x + tw, y, area.width - tw, h - SIDEPANEL_SPACING);
//			SWTGraphics2D g2d = new SWTGraphics2D(e.gc, e.display);
//			drawArea(gps[i], r, g2d);
//			g2d.dispose();
//		}
	}
	
	public void initSidePanel(Composite parent) { 
		sidePanel = new Canvas(parent, SWT.NULL);
		sidePanel.setBackground(sidePanel.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		sidePanel.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				drawSidePanel(e);
			}
		});
	}
	
	public void visualizeOnSidePanel(Collection<Graphics> objects) {
		spGraphics = objects;
		sidePanel.redraw();
	}
	
	/**
	 * Add a sample to use for visualization
	 * @param s The sample to add
	 */
	void addUseSample(Sample s) {
		if(s != null) {
			if(!useSamples.contains(s)) useSamples.add(createConfiguredSample(s));
			fireModifiedEvent();
		}
	}
	
	/**
	 * Add samples to use for visualization
	 * @param selection A selection containing samples to add
	 */
	void addUseSamples(IStructuredSelection selection) {
		Iterator<?> it = selection.iterator();
		while(it.hasNext()) {
			Sample s = (Sample)it.next();
			if(!useSamples.contains(s)) 
				useSamples.add(createConfiguredSample(s));
		}
		fireModifiedEvent();
	}
	
	/**
	 * Remove samples from the samples that will be used for visualization
	 * @param selection A selection containing samples to remove
	 */
	void removeUseSamples(IStructuredSelection selection) {
		Iterator<?> it = selection.iterator();
		while(it.hasNext()) {
			useSamples.remove((ConfiguredSample)it.next());
		}
		fireModifiedEvent();
	}
	
	/**
	 * Remove a sample from the samples that will be used for visualization
	 * @param s
	 */
	void removeUseSample(ConfiguredSample s) {
		if(s != null) {
			useSamples.remove(s);
			fireModifiedEvent();
		}
	}
	
	public final Element toXML() {
		Element xml = super.toXML();
		saveAttributes(xml);
		for(ConfiguredSample s : useSamples) xml.addContent(s.toXML());
		return xml;
	}
	
	/**
	 * Implement this method to save attributes to the XML element 
	 * that contain additional configuration of this plug-ins
	 * @param xml The XML element to save the attributes to
	 */
	abstract void saveAttributes(Element xml);
	
	public final void loadXML(Element xml) {
		super.loadXML(xml);
		loadAttributes(xml);
		for(Object o : xml.getChildren(ConfiguredSample.XML_ELEMENT)) {
			try {
				useSamples.add(createConfiguredSample((Element)o));
			} catch(Exception e) {
				Logger.log.error("Unable to save plugin settings", e);
			}
		}	
	}
	
	/**
	 * Implement this method to load additional attributes that were saved to XML
	 * by {@link #saveAttributes(Element)}
	 * @param xml The XML element containig the attributes
	 */
	abstract void loadAttributes(Element xml);
	
	public Composite createLegendComposite(Composite parent) {
		final Canvas canvas = new LegendCanvas(parent, SWT.NULL);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				drawLegend(canvas, e);
			}
		});
		SwtUtils.setCompositeAndChildrenBackground(canvas, parent.getBackground());
		return canvas;
	}
	
	private class LegendCanvas extends Canvas {
		public LegendCanvas(Composite parent, int style) {
			super(parent, style);
		}
		
		
		public Point computeSize(int wHint, int hHint, boolean changed) {
			Point size = super.computeSize(wHint, hHint, changed);
			size.y = 2*LEGEND_MARGIN + useSamples.size() * SAMPLE_HEIGHT + LEGEND_BOXHEIGHT;
			return size;
		}
		
	}
	
	final static int LEGEND_MARGIN = 5;
	final static int LEGEND_SPACING = 5;
	final static int SAMPLE_HEIGHT = LEGEND_SPACING + 15;
	final static int LEGEND_BOXHEIGHT = 20;
	void drawLegend(Canvas c, PaintEvent e) {
		e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
		
		//Draw a square divided into ns blocks
		//Draw labels for samplenames
		//Draw vertical line to labels
		
		org.eclipse.swt.graphics.Rectangle r = c.getClientArea();
		r.x += LEGEND_MARGIN;
		r.y += LEGEND_MARGIN;
		r.width -= 2*LEGEND_MARGIN;
		r.height -= 2*LEGEND_MARGIN;
		
		int ns = useSamples.size();
		int w = r.width / ns;
		int hi = (r.height - LEGEND_BOXHEIGHT) / ns + LEGEND_SPACING;
		
		for(int i = 0; i < ns; i++) {
			ConfiguredSample s = useSamples.get(i);
			org.eclipse.swt.graphics.Rectangle area = 
				new org.eclipse.swt.graphics.Rectangle(w * i, r.y, w, LEGEND_BOXHEIGHT);
			drawLegendSample(s, area, e, e.gc);
			
			Point ts = e.gc.textExtent(s.getName());
			int cx = area.x + area.width / 2;
			int cy = area.y + area.height / 2;
			int yo = (i == 0 ? 10 : 0);
			int xo = cx - ts.x / 2;
			if(i == 0) xo = Math.max(0, xo);
			else if(i == ns - 1) xo = Math.min(xo, area.x + area.width - ts.x);
		
			e.gc.drawLine(cx, cy, cx, area.y + area.height + (hi * i) + yo);
			e.gc.drawString(s.getName(), xo, 
					area.y + area.height + (hi * i) + yo, true);
			
		}
	}
	
	/**
	 * Draw an example visualization for the legend.
	 * (default implementation is an empty rectangle)
	 * @param s
	 * @param area
	 * @param e
	 * @param gc
	 */
	protected void drawLegendSample(ConfiguredSample s, org.eclipse.swt.graphics.Rectangle area, PaintEvent e, GC gc) {
		e.gc.drawRectangle(area);
	}
	
	TableViewer useSampleTable;
	SampleConfigComposite sampleConfigComp;
	protected Composite createConfigComposite(Composite parent) {
		Composite config = new Composite(parent, SWT.NULL);
		config.setLayout(new GridLayout());
		
		Composite optionsComp = createOptionsComp(config);
		optionsComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite samplesComp = createSamplesComp(config);
		samplesComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		sampleConfigComp = createSampleConfigComp(config);
		sampleConfigComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return config;
	}
	
	/**
	 * Create a composite that displays items for additional configuration
	 * of the visualization plug-in.
	 * @param parent The parent Composite
	 * @return A Composite for additional configuration
	 */
	abstract Composite createOptionsComp(Composite parent);
	
	Composite createSamplesComp(Composite parent) {
		Group samplesGroup = new Group(parent, SWT.NULL);
		samplesGroup.setText("Samples to display");
		samplesGroup.setLayout(new GridLayout(3, false));
		
		Label sampleLabel = new Label(samplesGroup, SWT.NULL);
		sampleLabel.setText("Available samples:");
		GridData span = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		span.horizontalSpan = 2;
		sampleLabel.setLayoutData(span);
		
		Label useSampleLabel = new Label(samplesGroup, SWT.NULL);
		useSampleLabel.setText("Selected samples\t\t\t\t\t");
		useSampleLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
				
		final ListViewer sampleList = new ListViewer(samplesGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		sampleList.getList().setLayoutData(new GridData(GridData.FILL_VERTICAL));
		sampleList.setContentProvider(new ArrayContentProvider());
		sampleList.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((Sample)element).getName();
			}
		});
		sampleList.setInput(Gex.getSamples(Types.REAL));
		
		Composite buttons = new Composite(samplesGroup, SWT.NULL);
		buttons.setLayout(new RowLayout(SWT.VERTICAL));
		final Button add = new Button(buttons, SWT.PUSH);
		add.setText(">");
		final Button remove = new Button(buttons, SWT.PUSH);
		remove.setText("<");
		
		SelectionListener buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget == add) {
					addUseSamples((IStructuredSelection)sampleList.getSelection());
				} else {
					removeUseSamples((IStructuredSelection)useSampleTable.getSelection());
				}
				useSampleTable.refresh();
			}
		};
		
		add.addSelectionListener(buttonListener);
		remove.addSelectionListener(buttonListener);
		
		Composite tableComp = new Composite(samplesGroup, SWT.NULL);
		tableComp.setLayout(new FillLayout());
		tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		Table t = new Table(tableComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		t.setHeaderVisible(true);
		
		TableColumn tcnm = new TableColumn(t, SWT.LEFT);
		tcnm.setText(useSampleColumns[0]);
		tcnm.setToolTipText("The samples that will be displayed in the gene box");
		TableColumn tccs = new TableColumn(t, SWT.LEFT);
		tccs.setText(useSampleColumns[1]);
		tccs.setToolTipText("The color set to apply on this sample");
		t.addControlListener(new TableColumnResizer(t, tableComp));
		useSampleTable = new TableViewer(t);
		useSampleTable.setContentProvider(new ArrayContentProvider());
		useSampleTable.setLabelProvider(new ITableLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
				switch(columnIndex) {
				case 0: return ((Sample)element).getName();
				case 1: return ((ConfiguredSample)element).getColorSetName();
				default: return null;
				}
			}
			public Image getColumnImage(Object element, int columnIndex) { return null; }
			public void addListener(ILabelProviderListener listener) { }
			public void dispose() { }
			public boolean isLabelProperty(Object element, String property) { return false; }
			public void removeListener(ILabelProviderListener listener) { }
		});
		useSampleTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				sampleConfigComp.setInput(getSelectedUseSamples());
			}
			
		});
		useSampleTable.setColumnProperties(useSampleColumns);
		final ComboBoxCellEditor editor = new ComboBoxCellEditor(useSampleTable.getTable(), ColorSetManager.getColorSetNames());
		useSampleTable.setCellEditors(new CellEditor[] { new TextCellEditor(), editor });
		useSampleTable.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return 
				property.equals(useSampleColumns[1]) &&
				editor.getItems().length > 0;
			}
			public Object getValue(Object element, String property) {
				if(property.equals(useSampleColumns[1]))
					return ((ConfiguredSample)element).getColorSetIndex();
				return null;
			}
			public void modify(Object element, String property, Object value) {
				if(property.equals(useSampleColumns[1])) {
					TableItem ti = (TableItem)element;
					((ConfiguredSample)ti.getData()).setColorSetIndex((Integer)value);
					useSampleTable.refresh();
				}
			}
			
		});
		
		useSampleTable.setInput(useSamples);
		return samplesGroup;
	}
		
	ConfiguredSample getSelectedUseSample() {
		return (ConfiguredSample)
		((IStructuredSelection)useSampleTable.getSelection()).getFirstElement();
	}
	
	ConfiguredSample[] getSelectedUseSamples() {
		Object[] selection = ((IStructuredSelection)useSampleTable.getSelection()).toArray();
		ConfiguredSample[] samples = new ConfiguredSample[selection.length];
		int i = 0;
		for(Object o : selection) samples[i++] = (ConfiguredSample)o;
		return samples;
	}
	/**
	 * Create a composite for configuring a sample selected for visualization.
	 * You can use an implementation of {@link SampleConfigComposite}.
	 * @see SampleConfigComposite
	 * @param parent
	 * @return
	 */
	abstract SampleConfigComposite createSampleConfigComp(Composite parent);
	
	/**
	 * This class provides a framework to create a composite to configure a sample selected
	 * for visualization.
	 * @author Thomas
	 *
	 */
	abstract class SampleConfigComposite extends Composite {
		ConfiguredSample[] input;
		
		public SampleConfigComposite(Composite parent, int style) {
			super(parent, style);
			createContents();
		}
		
		/**
		 * Create the contents for this composite
		 */
		abstract void createContents();
		
		/**
		 * Set the samples to display the configuration for
		 */
		public void setInput(ConfiguredSample[] samples) {
			input = samples;
			refresh();
		}

		/**
		 * Refresh the information in this composite
		 *
		 */
		void refresh() {
			if(input == null) setAllEnabled(false);
			else {
				setAllEnabled(true);
			}
		}
		
		protected final void setAllEnabled(boolean enable) {
			SwtUtils.setCompositeAndChildrenEnabled(this, enable);
		}
	}
	
	abstract ConfiguredSample createConfiguredSample(Sample s);
	
	abstract ConfiguredSample createConfiguredSample(Element xml) throws Exception;
	
	/**
	 * This class stores the configuration for a sample that is selected for
	 * visualization. In this implementation, a color-set to use for visualization is stored.
	 * Extend this class to store additional configuration data.
	 * @author Thomas
	 *
	 */
	abstract class ConfiguredSample extends Sample {		
		int colorSetIndex = 0;
		
		public ConfiguredSample(int idSample, String name, int dataType) {
			super(idSample, name, dataType);
		}
		
		/**
		 * Create a configured sample based on an existing sample
		 * @param s The sample to base the configured sample on
		 */
		public ConfiguredSample(Sample s) {
			super(s.getId(), s.getName(), s.getDataType());
		}
		
		/**
		 * Create a configured sample from the information in the given XML element
		 * @param xml The XML element containing information to create the configured sample from
		 * @throws Exception
		 */
		public ConfiguredSample(Element xml) throws Exception {
			super(0, "", 0);
			loadXML(xml);
		}
		
		/**
		 * Set the color-set to use for visualization of this sample
		 * @param index
		 */
		protected void setColorSetIndex(int index) { 
			colorSetIndex = index;
			fireModifiedEvent();
		}
		
		/**
		 * Get the color-set to use for visualization of this sample
		 * @return the color-set
		 */
		protected ColorSet getColorSet() { return ColorSetManager.getColorSet(colorSetIndex); }
		
		/**
		 * Get the name of the color-sets that is selected for visualization
		 * @return The name of the selected color-set, or "no colorsets available", if no
		 * color-sets exist
		 */
		protected String getColorSetName() {
			ColorSet cs = getColorSet();
			return cs == null ? "no colorsets available" : cs.getName();
		}
		
		/**
		 * Get the index of the color-set that is selected for visualization
		 * @return The index of the color-set
		 */
		protected int getColorSetIndex() { return colorSetIndex; }
				
		static final String XML_ELEMENT = "sample";
		static final String XML_ATTR_ID = "id";
		static final String XML_ATTR_COLORSET = "colorset";
		
		private final Element toXML() {
			Element xml = new Element(XML_ELEMENT);
			xml.setAttribute(XML_ATTR_ID, Integer.toString(getId()));
			xml.setAttribute(XML_ATTR_COLORSET, Integer.toString(colorSetIndex));
			saveAttributes(xml);
			return xml;
		}
		
		private final void loadXML(Element xml) throws Exception {
			int id = Integer.parseInt(xml.getAttributeValue(XML_ATTR_ID));
			int csi = Integer.parseInt(xml.getAttributeValue(XML_ATTR_COLORSET));
			Sample s = Gex.getSamples().get(id);
			setId(id);
			setName(s.getName());
			setDataType(s.getDataType());
			setColorSetIndex(csi);
			loadAttributes(xml);
		}
		
		/**
		 * Implement this method to save attributes to the XML element 
		 * that contain additional configuration of this configured sample
		 * @param xml The XML element to save the attributes to
		 */
		abstract void saveAttributes(Element xml);
		
		/**
		 * Implement this method to load additional attributes that were saved to XML
		 * by {@link #saveAttributes(Element)}
		 * @param xml The XML element containig the attributes
		 */
		abstract void loadAttributes(Element xml);
	}
}
