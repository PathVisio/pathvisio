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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.jdom.Element;

import util.SwtUtils;
import visualization.Visualization;
import data.CachedData;
import data.GmmlDataObject;
import data.GmmlGex;
import data.CachedData.Data;
import data.GmmlGdb.IdCodePair;
import data.GmmlGex.Sample;

/**
 * Provides label for Gene Product
 * @author thomas
 *
 */
public class ExpressionAsTextPlugin extends VisualizationPlugin {
	static final String NAME = "Text by expression";
	static final String DESCRIPTION = 
		"This plugin displays expression data for a given set of samples as text";
	
	static final FontData DEFAULT_FONTDATA = new FontData("Arial narrow", 10, SWT.NORMAL);
			
	final static String SEP = ", ";	
	int roundTo = 2;
	boolean mean = false;
			
	FontData fontData;
	Set<Sample> useSamples = new LinkedHashSet<Sample>();
	
	public ExpressionAsTextPlugin(Visualization v) {
		super(v);		
	    setIsConfigurable(true);
		setDisplayOptions(DRAWING | TOOLTIP);
		setIsGeneric(false);
		setUseProvidedArea(false);
	}
	
	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }
	
	static final int SPACING = 3;
	public void visualizeOnDrawing(GmmlGraphics g, PaintEvent e, GC buffer) {
		if(g instanceof GmmlGeneProduct) {
			GmmlGeneProduct gp = (GmmlGeneProduct) g;
			CachedData  cache = GmmlGex.getCachedData();
			
			IdCodePair idc = new IdCodePair(gp.getID(), gp.getSystemCode());
			
			if(cache == null || !cache.hasData(idc)|| useSamples.size() == 0) {
				return;
			}
						
			Font f = new Font(e.display, getFontData(true));
			
			int startx = (int)(g.getVLeft() + g.getVWidth() + SPACING);
			int starty = (int)(g.getVTop() + g.getVHeight() / 2);
			

			buffer.setFont(f);
			int w = 0, i = 0;
			for(Sample s : useSamples) {
				String str = getDataString(s, idc, cache, SEP + "\n") + 
				(++i == useSamples.size() ? "" : SEP);
				Point size = buffer.textExtent(str);
				buffer.drawText(str, startx + w, starty - size.y / 2, true);
				w += size.x;
			}
				
			f.dispose();
		}
	}
	
	public Composite visualizeOnToolTip(Composite parent, GmmlGraphics g) {
		if(g instanceof GmmlGeneProduct) {
			GmmlGeneProduct gp = (GmmlGeneProduct) g;
			CachedData  cache = GmmlGex.getCachedData();
			
			IdCodePair idc = new IdCodePair(gp.getID(), gp.getSystemCode());
			
			if(!cache.hasData(idc)|| useSamples.size() == 0) {
				return null;
			}
						
			Group group = new Group(parent, SWT.NULL);
			group.setLayout(new GridLayout(2, false));
			group.setText("Expression data");
			
			for(Sample s : useSamples) {
				Label labelL = new Label(group, SWT.NULL);
				labelL.setText(getLabelLeftText(s));
				Label labelR = new Label(group, SWT.NULL);
				labelR.setText(getLabelRightText(s, idc, cache));
			}
			SwtUtils.setCompositeAndChildrenBackground(group, 
					group.getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			return group;
		} else return null;
	}
	
	public Composite createLegendComposite(Composite parent) {
		Composite lc = new Composite(parent, SWT.NULL);
		lc.setLayout(new FillLayout());
		Label label = new Label(lc, SWT.NULL);
		String s = "Displayed value" + (useSamples.size() > 1 ? "s from left to right:" : " on drawing:");
		for(Sample smp : useSamples) {
			s += "\n- " + smp.getName();
		}
		label.setText(s);
		SwtUtils.setCompositeAndChildrenBackground(lc, parent.getBackground());
		return lc;
	}
	
	String getLabelLeftText(Sample s) {
		return s.getName() + ":";
	}
	
	String getLabelRightText(Sample s, IdCodePair idc, CachedData cache) {
		return getDataString(s, idc, cache, SEP);
	}
	
	String getDataString(Sample s, IdCodePair idc, CachedData cache, String multSep) {	
		Object str = null;
		if(cache.hasMultipleData(idc))
			str = formatData(getSampleStringMult(s, idc, cache, multSep));
		else
			str =  formatData(getSampleData(s, cache.getSingleData(idc)));
		return str == null ? "" : str.toString();
	}
	
	Object getSampleData(Sample s, Data data) {
		return data.getSampleData(s.getId());
	}
	
	Object getSampleStringMult(Sample s, IdCodePair idc, CachedData cache, String sep) {
		if(mean) return cache.getAverageSampleData(idc).get(s.getId());
		
		List<Data> refdata = cache.getData(idc);
		StringBuilder strb = new StringBuilder();
		for(Data d : refdata) {
			String str = formatData(d.getSampleData().get(s.getId())).toString();
			if(!str.equals("NaN")) {
				strb.append(str + sep);
			}
		}
		return strb.length() > sep.length() ? strb.substring(0, strb.length() - sep.length()) : strb;
	}
	
	Object formatData(Object data) {
		if(data instanceof Double) {
			double d = (Double)data;
			
			if(Double.isNaN(d)) return "NaN";
			
			int dec = (int)Math.pow(10, getRoundTo());
			double rounded = (double)(Math.round(d * dec)) / dec;
			data = dec == 1 ? Integer.toString((int)rounded) : Double.toString(rounded);
		}
		return data;
	}
	
	void setFontData(FontData fd) {
		if(fd != null) {
			fontData = fd;
			fireModifiedEvent();
		}
	}
	
	int getFontSize() {
		return getFontData().getHeight();
	}
	
	FontData getFontData() {
		return getFontData(false);
	}
	
	FontData getFontData(boolean adjustZoom) {
		FontData fd = fontData == null ? DEFAULT_FONTDATA : fontData;
		if(adjustZoom) {
			fd = new FontData(fd.getName(), fd.getHeight(), fd.getStyle());
			fd.setHeight((int)Math.ceil(GmmlVision.getDrawing().vFromM(fd.getHeight()) * 15));//TODO: get rid of 15
		}
		return fd;
	}
	
	void addUseSample(Sample s) {
		if(s != null) {
			useSamples.add(s);
			fireModifiedEvent();
		}
	}
	
	void addUseSamples(IStructuredSelection selection) {
		Iterator it = selection.iterator();
		while(it.hasNext()) {
			useSamples.add((Sample)it.next());
		}
		fireModifiedEvent();
	}
	
	void removeUseSamples(IStructuredSelection selection) {
		Iterator it = selection.iterator();
		while(it.hasNext()) {
			useSamples.remove((Sample)it.next());
		}
		fireModifiedEvent();
	}
	
	public int getRoundTo() { return roundTo; }
	
	public void setRoundTo(int dec) {
		if(dec >= 0 && dec < 10) {
			roundTo = dec;
			fireModifiedEvent();
		}
	}
	
	public void setCalcMean(boolean doCalcMean) {
		mean = doCalcMean;
		fireModifiedEvent();
	}
	
	protected Composite createConfigComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout());
		
		Composite sampleComp = createSampleComp(comp);
		sampleComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite options = createOptionsComp(comp);
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return comp;
	}
		
	Composite createSampleComp(Composite parent) {
		Composite sampleComp = new Composite(parent, SWT.NULL);
		sampleComp.setLayout(new GridLayout(2, false));

		Label expl = new Label(sampleComp, SWT.NULL);
		expl.setText("- Click on a sample in the left list to add to the samples that will" +
				"be shown as text\n" +
				"- Click on a sample on the right list to remove");
		GridData span = new GridData();
		span.horizontalSpan = 2;
		expl.setLayoutData(span);
		
		Label slabel = new Label(sampleComp, SWT.NULL);
		slabel.setText("All samples:");
		
		Label ulabel = new Label(sampleComp, SWT.NULL);
		ulabel.setText("Selected samples:");
		
		LabelProvider lprov = new LabelProvider() {
			public String getText(Object element) {
				return ((Sample)element).getName();
			}
		};
		
		final ListViewer samples = new ListViewer(sampleComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		samples.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		samples.setContentProvider(new ArrayContentProvider());
		samples.setLabelProvider(lprov);
			
		final ListViewer use = new ListViewer(sampleComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		use.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		use.setContentProvider(new ArrayContentProvider());
		use.setLabelProvider(lprov);
		
		ISelectionChangedListener slist = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if(event.getSource() == use) 
					removeUseSamples((IStructuredSelection)event.getSelection());
				else
					addUseSamples((IStructuredSelection)event.getSelection());
				use.refresh();
			}
		};
		use.addSelectionChangedListener(slist);
		samples.addSelectionChangedListener(slist);
		
		samples.setInput(GmmlGex.getSamples(-1));
		use.setInput(useSamples);
		
		return sampleComp;
	}
	
	Composite createOptionsComp(Composite parent) {
		Group optionsComp = new Group(parent, SWT.NULL);
		optionsComp.setText("Options");
		optionsComp.setLayout(new RowLayout(SWT.VERTICAL));
		final Button font = new Button(optionsComp, SWT.PUSH);
		font.setText("Change font");
		font.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FontDialog fd = new FontDialog(font.getShell());
				fd.setFontList(new FontData[] { getFontData() });
				FontData data = fd.open();
				if(data != null) setFontData(data);
			}
		});
		createRoundComp(optionsComp);
		final Button doAvg = new Button(optionsComp, SWT.CHECK);
		doAvg.setText("Show mean value of data with ambigious reporters");
		doAvg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setCalcMean(doAvg.getSelection());
			}
		});
		doAvg.setSelection(mean);
		return optionsComp;
	}
	
	Composite createRoundComp(Composite parent) {
		Composite roundComp = new Composite(parent, SWT.NULL);
		roundComp.setLayout(new RowLayout(SWT.VERTICAL));
		Label lb = new Label(roundComp, SWT.NULL);
		lb.setText("Number of decimals to round numeric data to:");
		final Spinner sp = new Spinner(roundComp, SWT.BORDER);
		sp.setMaximum(9);
		sp.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setRoundTo(sp.getSelection());
			}
		});
		sp.setSelection(getRoundTo());
		sp.pack();
		return roundComp;
	}
	
	
	public void visualizeOnSidePanel(Collection<GmmlGraphics> objects) { }
	public void initSidePanel(Composite parent) { }

	static final String XML_ATTR_FONTDATA = "font";
	static final String XML_ATTR_AVG = "mean";
	static final String XML_ATTR_ROUND = "round-to";
	static final String XML_ELM_ID = "sample-id";
	public Element toXML() {
		Element elm = super.toXML();
		elm.setAttribute(XML_ATTR_FONTDATA, getFontData().toString());
		elm.setAttribute(XML_ATTR_ROUND, Integer.toString(getRoundTo()));
		elm.setAttribute(XML_ATTR_AVG, Boolean.toString(mean));
		for(Sample s : useSamples) {
			Element selm = new Element(XML_ELM_ID);
			selm.setText(Integer.toString(s.getId()));
			elm.addContent(selm);
		}
		return elm;
	}
	
	public void loadXML(Element xml) {
		super.loadXML(xml);
		for(Object o : xml.getChildren(XML_ELM_ID)) {
			try {
				int id = Integer.parseInt(((Element)o).getText());
				useSamples.add(GmmlGex.getSample(id));
			} catch(Exception e) { GmmlVision.log.error("Unable to add sample", e); }
		}
		roundTo = Integer.parseInt(xml.getAttributeValue(XML_ATTR_ROUND));
		fontData = new FontData(xml.getAttributeValue(XML_ATTR_FONTDATA));
		mean = Boolean.parseBoolean(xml.getAttributeValue(XML_ATTR_AVG));
	}
}

