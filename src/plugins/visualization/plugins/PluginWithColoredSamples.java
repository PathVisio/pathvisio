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
package visualization.plugins;

import gmmlVision.GmmlVision;
import graphics.GmmlGeneProduct;
import graphics.GmmlGraphics;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
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

import util.SwtUtils;
import util.TableColumnResizer;
import visualization.Visualization;
import visualization.colorset.ColorSet;
import visualization.colorset.ColorSetManager;
import visualization.plugins.VisualizationPlugin;
import data.GmmlGex;
import data.GmmlGdb.IdCodePair;
import data.GmmlGex.Sample;
import data.GmmlGex.CachedData.Data;

public abstract class PluginWithColoredSamples extends VisualizationPlugin {	
	static final String[] useSampleColumns = { "sample", "color set" };
	static final RGB LINE_COLOR_DEFAULT = new RGB(0, 0, 0);
	
	List<ConfiguredSample> useSamples = new ArrayList<ConfiguredSample>();
	Canvas sidePanel;
	Collection<GmmlGraphics> spGraphics;
	
	public PluginWithColoredSamples(Visualization v) {
		super(v);
		setIsConfigurable(true);
		setIsGeneric(false);
		setUseProvidedArea(true);
	}
		
	public void draw(GmmlGraphics g, PaintEvent e, GC buffer) {
		if(!(g instanceof GmmlGeneProduct)) return;
		if(useSamples.size() == 0) return; //Nothing to draw
		
		GmmlGeneProduct gp = (GmmlGeneProduct) g;
		
		Region region = getVisualization().getReservedRegion(this, g);
		Rectangle area = region.getBounds();
		
		drawArea(gp, area, e, buffer);
		
		Color c = SwtUtils.changeColor(null, gp.getGmmlData().getColor(), e.display);
		buffer.setForeground(c);
		buffer.drawRectangle(area);
		
		c.dispose();
		region.dispose();
	}
	
	void drawArea(GmmlGeneProduct gp, Rectangle area, PaintEvent e, GC buffer) {
		int nr = useSamples.size();
		int left = area.width % nr; //Space left after dividing, give to last rectangle
		int w = area.width / nr;
		for(int i = 0; i < nr; i++) {
			Rectangle r = new Rectangle(
					area.x + w * i,
					area.y,
					w + ((i == nr - 1) ? left : 0), area.height);
			ConfiguredSample s = (ConfiguredSample)useSamples.get(i);
			Data data = GmmlGex.getCachedData(new IdCodePair(gp.getID(), gp.getSystemCode()));
			
			if(s.getColorSet() == null) continue; //No colorset for this sample
			if(data == null) drawNoDataFound(s, area, e, buffer);
			else drawSample(s, data, r, e, buffer);
		}
	}
	
	abstract void drawNoDataFound(ConfiguredSample s, Rectangle area, PaintEvent e, GC buffer);
	abstract void drawSample(ConfiguredSample s, Data data, Rectangle area, PaintEvent e, GC buffer);

	static final int SIDEPANEL_SPACING = 3;
	static final int SIDEPANEL_MARGIN = 5;
	void drawSidePanel(PaintEvent e) {
		if(spGraphics == null) return;
		
		Rectangle area = sidePanel.getClientArea();
		area.x += SIDEPANEL_MARGIN;
		area.y += SIDEPANEL_MARGIN;
		area.width -= SIDEPANEL_MARGIN * 2;
		area.height -= SIDEPANEL_MARGIN * 2;
		
		int nr = 0;
		for(GmmlGraphics g : spGraphics) if(g instanceof GmmlGeneProduct) nr++;

		if(nr == 0) {
			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
			e.gc.fillRectangle(sidePanel.getClientArea());
			return;
		}
		
		GmmlGeneProduct[] gps = new GmmlGeneProduct[nr];
		int x = 0;
		for(GmmlGraphics g : spGraphics) 
			if(g instanceof GmmlGeneProduct) gps[x++] = (GmmlGeneProduct)g;
		
		e.gc.setFont(e.display.getSystemFont());
		int tw = 0;
		for(GmmlGeneProduct g : gps) tw = Math.max(tw, e.gc.textExtent(g.getName()).x);
		
		int h = area.height / nr;
		for(int i = 0; i < nr; i++) {
			int y = area.y + i*h;
			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
			e.gc.drawText(gps[i].getName(), area.x, y + h / 2 - e.gc.getFontMetrics().getHeight() / 2);
			Rectangle r = new Rectangle(area.x + tw, y, area.width - tw, h - SIDEPANEL_SPACING);
			drawArea(gps[i], r, e, e.gc);
		}
	}
	
	public void createSidePanelComposite(Composite parent) { 
		sidePanel = new Canvas(parent, SWT.NULL);
		sidePanel.setBackground(sidePanel.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		sidePanel.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				drawSidePanel(e);
			}
		});
	}
	
	public void updateSidePanel(Collection<GmmlGraphics> objects) {
		spGraphics = objects;
		sidePanel.redraw();
	}
	
	void addUseSample(Sample s) {
		if(s != null) {
			if(!useSamples.contains(s)) useSamples.add(createConfiguredSample(s));
			fireModifiedEvent();
		}
	}
	
	void addUseSamples(IStructuredSelection selection) {
		Iterator it = selection.iterator();
		while(it.hasNext()) {
			Sample s = (Sample)it.next();
			if(!useSamples.contains(s)) 
				useSamples.add(createConfiguredSample(s));
		}
		fireModifiedEvent();
	}
	
	void removeUseSamples(IStructuredSelection selection) {
		Iterator it = selection.iterator();
		while(it.hasNext()) {
			useSamples.remove((ConfiguredSample)it.next());
		}
		fireModifiedEvent();
	}
	
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
	
	abstract void saveAttributes(Element xml);
	
	public final void loadXML(Element xml) {
		super.loadXML(xml);
		loadAttributes(xml);
		for(Object o : xml.getChildren(ConfiguredSample.XML_ELEMENT)) {
			try {
				useSamples.add(createConfiguredSample((Element)o));
			} catch(Exception e) {
				GmmlVision.log.error("Unable to save plugin settings", e);
			}
		}	
	}
	
	abstract void loadAttributes(Element xml);
	
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
		sampleList.setInput(GmmlGex.getSamples(Types.REAL));
		
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
	
	abstract SampleConfigComposite createSampleConfigComp(Composite parent);
	
	abstract class SampleConfigComposite extends Composite {
		ConfiguredSample[] input;
		
		public SampleConfigComposite(Composite parent, int style) {
			super(parent, style);
			createContents();
		}
		
		abstract void createContents();
		
		public void setInput(ConfiguredSample[] samples) {
			input = samples;
			refresh();
		}
		
		public void refresh() {
			if(input == null) setAllEnabled(false);
			else {
				setAllEnabled(true);
			}
		}
		
		public void setAllEnabled(boolean enable) {
			SwtUtils.setCompositeAndChildrenEnabled(this, enable);
		}
	}
	
	abstract ConfiguredSample createConfiguredSample(Sample s);
	
	abstract ConfiguredSample createConfiguredSample(Element xml) throws Exception;
	
	abstract class ConfiguredSample extends Sample {		
		int colorSetIndex = 0;
		
		public ConfiguredSample(int idSample, String name, int dataType) {
			super(idSample, name, dataType);
		}
		
		public ConfiguredSample(Sample s) {
			super(s.getId(), s.getName(), s.getDataType());
		}
		
		public ConfiguredSample(Element xml) throws Exception {
			super(0, "", 0);
			loadXML(xml);
		}
		
		protected void setColorSetIndex(int index) { 
			colorSetIndex = index;
			fireModifiedEvent();
		}
		
		protected ColorSet getColorSet() { return ColorSetManager.getColorSet(colorSetIndex); }
		
		protected String getColorSetName() {
			ColorSet cs = getColorSet();
			return cs == null ? "no colorsets available" : cs.getName();
		}
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
			Sample s = GmmlGex.getSamples().get(id);
			setId(id);
			setName(s.getName());
			setDataType(s.getDataType());
			setColorSetIndex(csi);
			loadAttributes(xml);
		}
		
		abstract void saveAttributes(Element xml);
		
		abstract void loadAttributes(Element xml);
	}
}
