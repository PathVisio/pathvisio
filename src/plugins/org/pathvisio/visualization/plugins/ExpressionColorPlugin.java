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
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.jdom.Element;
import org.pathvisio.data.CachedData;
import org.pathvisio.data.Gex;
import org.pathvisio.data.CachedData.Data;
import org.pathvisio.data.Gdb.IdCodePair;
import org.pathvisio.data.Gex.Sample;
import org.pathvisio.debug.Logger;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.util.swt.SwtUtils;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.colorset.ColorSet;

public class ExpressionColorPlugin extends PluginWithColoredSamples {
	final String NAME = "Color by expression";
	static final String DESCRIPTION = 
		"This plugin colors gene product objects in the pathway by their expression data.";
			
	Color lineColor;
	boolean drawLine = false;
	
	public ExpressionColorPlugin(Visualization v) {
		super(v);
		setDisplayOptions(DRAWING | SIDEPANEL);
	}

	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }
		
	void drawNoDataFound(ConfiguredSample s, Rectangle area, Graphics2D g2d) {
		ColorSet cs = s.getColorSet();
		drawColoredRectangle(area, cs.getColor(ColorSet.ID_COLOR_NO_DATA_FOUND), g2d);
	}

	protected void drawSample(ConfiguredSample s, IdCodePair idc, Rectangle area, Graphics2D g2d) {
		ColorSample smp = (ColorSample)s;
		CachedData cache = Gex.getCachedData();
		
		if(cache.hasMultipleData(idc)) {
			switch(smp.getAmbigiousType()) {
			case ColorSample.AMBIGIOUS_AVG:
				drawSampleAvg(smp, idc, cache, area, g2d);
				break;
			case ColorSample.AMBIGIOUS_BARS:
				drawSampleBar(smp, idc, cache, area, g2d);
				break;
			}
		} else {
			ColorSet cs = smp.getColorSet();
			HashMap<Integer, Object> data = cache.getSingleData(idc).getSampleData();
			Color rgb = cs.getColor(data, smp.getId());
			drawColoredRectangle(area, rgb, g2d);
		}
	}

	void drawSampleAvg(ConfiguredSample s, IdCodePair idc, CachedData cache, Rectangle area, Graphics2D g2d) {
		ColorSet cs = s.getColorSet();
		Color rgb = cs.getColor(cache.getAverageSampleData(idc), s.getId());
		drawColoredRectangle(area, rgb, g2d);
	}
	
	void drawSampleBar(ConfiguredSample s, IdCodePair idc, CachedData cache, Rectangle area, Graphics2D g2d) {
		ColorSet cs = s.getColorSet();
		List<Data> refdata = cache.getData(idc);
		int n = refdata.size();
		int left = area.height % n;
		int h = area.height / n;
		for(int i = 0; i < n; i++) {
			Color rgb = cs.getColor(refdata.get(i).getSampleData(), s.getId());
			Rectangle r = new Rectangle(
					area.x, area.y + i*h,
					area.width, h + (i == n-1 ? left : 0));
			drawColoredRectangle(r, rgb, g2d);
		}
	}
	
	void drawColoredRectangle(Rectangle r, Color c, Graphics2D g2d) {			
		g2d.setColor(c);
		g2d.fill(r);
		
		if(drawLine) {
			g2d.setColor(getLineColor());
			g2d.draw(r);
		}
	}
	
	void setLineColor(Color rgb) {
		if(rgb != null)	{
			lineColor = rgb;
			fireModifiedEvent();
		}
	}
	
	Color getLineColor() { return lineColor == null ? LINE_COLOR_DEFAULT : lineColor; }
	
	void setDrawLine(boolean draw) {
		drawLine = draw;
		fireModifiedEvent();
	}
	
	static final String XML_ATTR_DRAWLINE = "drawLine";
	static final String XML_ELM_LINECOLOR = "lineColor";
	
	protected void saveAttributes(Element xml) {
		xml.setAttribute(XML_ATTR_DRAWLINE, Boolean.toString(drawLine));
		xml.addContent(ColorConverter.createColorElement(XML_ELM_LINECOLOR, getLineColor()));
	}
		
	protected void loadAttributes(Element xml) {
		try {
			lineColor = ColorConverter.parseColorElement(xml.getChild(XML_ELM_LINECOLOR));
			drawLine = Boolean.parseBoolean(xml.getAttributeValue(XML_ATTR_DRAWLINE));
		} catch(Exception e) {
			Logger.log.error("Unable to parse settings for plugin " + NAME, e);
		}
	}
	
	SampleConfigComposite sampleConfigComp;
	Button checkLine;
	org.eclipse.swt.graphics.Color labelColor;
	Composite createOptionsComp(Composite parent) {
		Group lineGroup = new Group(parent, SWT.NULL);
		lineGroup.setLayout(new GridLayout());
		lineGroup.setText("General options");
		
		checkLine = new Button(lineGroup, SWT.CHECK);
		checkLine.setText("Draw line around sample boxes");
		checkLine.setSelection(drawLine);
		
		final Composite colorComp = new Composite(lineGroup, SWT.NULL);
		colorComp.setLayout(new GridLayout(3, false));
		
		Label label = new Label(colorComp, SWT.NULL);
		label.setText("Line color: ");
		final CLabel colorLabel = new CLabel(colorComp, SWT.SHADOW_IN);
		colorLabel.setLayoutData(SwtUtils.getColorLabelGrid());
		labelColor = SwtUtils.changeColor(labelColor, getLineColor(), colorLabel.getDisplay());
		colorLabel.setBackground(labelColor);
		
		Button colorButton = new Button(colorComp, SWT.PUSH);
		colorButton.setText("...");
		colorButton.setLayoutData(SwtUtils.getColorLabelGrid());
		colorButton.addListener(SWT.Selection | SWT.Dispose, new Listener() {
			public void handleEvent(Event e) {
				switch(e.type) {
				case SWT.Selection:
					RGB rgb = new ColorDialog(colorLabel.getShell()).open();
					if(rgb != null) {
						labelColor = SwtUtils.changeColor(labelColor, rgb, e.display);
						colorLabel.setBackground(labelColor);
						setLineColor(SwtUtils.rgb2color(rgb));
					}
				break;
				case SWT.Dispose:
					labelColor.dispose();
				break;
				}
			}
		});
		
		checkLine.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean doDraw = checkLine.getSelection();
				SwtUtils.setCompositeAndChildrenEnabled(colorComp, doDraw);
				setDrawLine(doDraw);
			}
		});
				
		return lineGroup;
	}
	
	ConfiguredSample getSelectedUseSample() {
		return (ConfiguredSample)
		((IStructuredSelection)useSampleTable.getSelection()).getFirstElement();
	}
	
	protected SampleConfigComposite createSampleConfigComp(Composite parent) {
		return new ColorSampleConfigComposite(parent, SWT.NULL);
	}
	
	protected class ColorSampleConfigComposite extends SampleConfigComposite {
		Button radioBar, radioAvg;
		
		public ColorSampleConfigComposite(Composite parent, int style) {
			super(parent, style);
		}
		
		void createContents() {
			setLayout(new FillLayout());
			Group group = new Group(this, SWT.NULL);
			group.setText("Selected sample confguration");
			group.setLayout(new GridLayout());
			
			Composite ambComp = createAmbigiousComp(group);
			ambComp.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			setInput(null);
		}
		
		ColorSample getInput() { 
			return input == null || input.length == 0 ? null : (ColorSample)input[0]; 
		}
		
		Composite createAmbigiousComp(Composite parent) {
			Group ambGroup = new Group(parent, SWT.NULL);
			ambGroup.setText("How to treat ambigious reporters?");
			
			ambGroup.setLayout(new RowLayout(SWT.VERTICAL));
			radioAvg = new Button(ambGroup, SWT.RADIO);
			radioAvg.setText("Use average value for color");
			radioBar = new Button(ambGroup, SWT.RADIO);
			radioBar.setText("Divide in horizontal bars");
			SelectionListener listener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int type;
					if(e.widget == radioAvg) type = ColorSample.AMBIGIOUS_AVG;
					else type = ColorSample.AMBIGIOUS_BARS;
					if(input != null) getInput().setAmbigiousType(type);
				}
			};
			radioAvg.addSelectionListener(listener);
			radioBar.addSelectionListener(listener);
			radioAvg.setSelection(true);
			
			return ambGroup;
		}
					
		public void refresh() {
			if(input == null || input.length != 1) setAllEnabled(false);
			else {
				setAllEnabled(true);
				boolean avg = getInput().getAmbigiousType() == ColorSample.AMBIGIOUS_AVG;
				radioAvg.setSelection(avg);
				radioBar.setSelection(!avg);
			}
		}
	}
	
	protected ConfiguredSample createConfiguredSample(Sample s) {
		return new ColorSample(s);
	}
	
	protected ConfiguredSample createConfiguredSample(Element xml) throws Exception {
		return new ColorSample(xml);
	}
	
	protected class ColorSample extends ConfiguredSample {
		public static final int AMBIGIOUS_AVG = 0;
		public static final int AMBIGIOUS_BARS = 1;
		
		int ambigious = AMBIGIOUS_BARS;
		
		public ColorSample(int idSample, String name, int dataType) {
			super(idSample, name, dataType);
		}
		
		public ColorSample(Sample s) {
			super(s.getId(), s.getName(), s.getDataType());
		}
		
		public ColorSample(Element xml) throws Exception {
			super(xml);
		}
		
		int getAmbigiousType() { return ambigious; }
		
		void setAmbigiousType(int type) { 
			ambigious = type;
			fireModifiedEvent();
		}
		
		static final String XML_ATTR_AMBIGIOUS = "ambigious";
				
		protected void saveAttributes(Element xml) {
			xml.setAttribute(XML_ATTR_AMBIGIOUS, Integer.toString(ambigious));
		}
		
		protected void loadAttributes(Element xml) {
			int amb = Integer.parseInt(xml.getAttributeValue(XML_ATTR_AMBIGIOUS));
			setAmbigiousType(amb);
		}
	}
	
	public Composite visualizeOnToolTip(Composite parent, Graphics g) { return null; }
}
