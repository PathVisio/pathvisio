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

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdom.Element;
import org.pathvisio.Engine;
import org.pathvisio.data.CachedData;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.Sample;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.data.CachedData.Data;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.DataSource;
import org.pathvisio.model.Xref;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationMethod;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TextByExpression extends VisualizationMethod implements ActionListener {
	static final Font DEFAULT_FONT = new Font("Arial narrow", Font.PLAIN, 10);
	static final int SPACING = 3;
	static final String ACTION_SAMPLE = "sample";
	
	final static String SEP = ", ";	
	int roundTo = 2;
	boolean mean = false;
			
	Font font;
	Set<Sample> useSamples = new LinkedHashSet<Sample>();
	
	public TextByExpression(Visualization v, String registeredName) {
		super(v, registeredName);
		setIsConfigurable(true);
		setUseProvidedArea(false);
	}
	
	public String getDescription() {
		return "Display a numerical value next to a DataNode";
	}

	public String getName() {
		return "Expression as numerical value";
	}

	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if(g instanceof GeneProduct) {
			GeneProduct gp = (GeneProduct) g;
			SimpleGex gex = GexManager.getCurrent().getCurrentGex();
			if(gex == null) return;
			
			CachedData  cache = gex.getCachedData();
			
			String id = gp.getPathwayElement().getGeneID();
			DataSource ds = gp.getPathwayElement().getDataSource();
			Xref idc = new Xref(id, ds);
			
			if(cache == null || !cache.hasData(idc)|| useSamples.size() == 0) {
				return;
			}

			int startx = (int)(g.getVLeft() + g.getVWidth() + SPACING);
			int starty = (int)(g.getVTop() + g.getVHeight() / 2);
			Font f = getFont(true);
			g2d.setFont(f);
			int th = g2d.getFontMetrics().getHeight();
			int w = 0, i = 0;
			for(Sample s : useSamples) {
				String str = getDataString(s, idc, cache, SEP + "\n") + 
					(++i == useSamples.size() ? "" : SEP);
				TextLayout tl = new TextLayout(str, f, g2d.getFontRenderContext());
				Rectangle2D tb = tl.getBounds();
				g2d.drawString(str, startx + w, starty + th / 2);
				w += tb.getWidth() + SPACING;
			}
		}
	}

	public Component visualizeOnToolTip(Graphics g) {
		if(g instanceof GeneProduct) {
			GeneProduct gp = (GeneProduct) g;
			CachedData  cache = GexManager.getCurrent().getCurrentGex().getCachedData();
			
			Xref idc = new Xref(
					gp.getPathwayElement().getGeneID(), 
					gp.getPathwayElement().getDataSource());
			
			if(!cache.hasData(idc)|| useSamples.size() == 0) {
				return null;
			}
			
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder("Expression data"));
			panel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = -1;
			for(Sample s : useSamples) {
				gbc.gridy++;
				gbc.gridx = 0;
				panel.add(new JLabel(getLabelLeftText(s)), gbc);
				gbc.gridx = 1;
				panel.add(new JLabel(getLabelRightText(s, idc, cache)), gbc);
			}
			return panel;
		} else return null;
	}

	String getLabelLeftText(Sample s) {
		return s.getName() + ":";
	}
	
	String getLabelRightText(Sample s, Xref idc, CachedData cache) {
		return getDataString(s, idc, cache, SEP);
	}
	
	String getDataString(Sample s, Xref idc, CachedData cache, String multSep) {	
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
	
	Object getSampleStringMult(Sample s, Xref idc, CachedData cache, String sep) {
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
	
	void setFont(Font f) {
		if(f != null) {
			font = f;
			modified();
		}
	}
	
	Font getFont() {
		return getFont(false);
	}
	
	Font getFont(boolean adjustZoom) {
		Font f = font == null ? DEFAULT_FONT : font;
		if(adjustZoom) {
			int size = (int)Math.ceil(Engine.getCurrent().getActiveVPathway().vFromM(f.getSize()) * 15);
			f = new Font(f.getName(), f.getStyle(), size);
		}
		return f;
	}
	
	void addUseSample(Sample s) {
		if(s != null) {
			useSamples.add(s);
			modified();
		}
	}
	
	void removeUseSample(Sample s) {
		if(s != null) {
			useSamples.remove(s);
			modified();
		}
	}
	
	public int getRoundTo() { return roundTo; }
	
	public void setRoundTo(int dec) {
		if(dec >= 0 && dec < 10) {
			roundTo = dec;
			modified();
		}
	}
	
	public void setCalcMean(boolean doCalcMean) {
		mean = doCalcMean;
		modified();
	}
	
	SampleCheckList sampleList;
	
	public JPanel getConfigurationPanel() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout(
			"fill:50dlu:grow, 4dlu, pref",
			"pref, 4dlu, fill:100dlu:grow, pref"
		);
		panel.setLayout(layout);
		
		sampleList = new SampleCheckList(useSamples);

		sampleList.setActionCommand(ACTION_SAMPLE);
		sampleList.addActionListener(this);
		CellConstraints cc = new CellConstraints();
		panel.add(new JLabel("Select samples:"), cc.xy(1, 1));
		panel.add(new JScrollPane(sampleList), cc.xy(1, 3));
		return panel;
	}
	
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		System.out.println("ACTION");
		if(ACTION_SAMPLE.equals(action)) {
			Sample s = sampleList.getSelectedSample();
			if(sampleList.isSelected(s)) {
				addUseSample(s);
			} else {
				removeUseSample(s);
			}
		}
	}
	
	static final String XML_ATTR_FONTDATA = "font";
	static final String XML_ATTR_AVG = "mean";
	static final String XML_ATTR_ROUND = "round-to";
	static final String XML_ELM_ID = "sample-id";
	public Element toXML() {
		Element elm = super.toXML();
		elm.setAttribute(XML_ATTR_FONTDATA, getFont().toString());
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
				useSamples.add(GexManager.getCurrent().getCurrentGex().getSample(id));
			} catch(Exception e) { Logger.log.error("Unable to add sample", e); }
		}
		roundTo = Integer.parseInt(xml.getAttributeValue(XML_ATTR_ROUND));
		setFont(Font.decode(xml.getAttributeValue(XML_ATTR_FONTDATA)));
		mean = Boolean.parseBoolean(xml.getAttributeValue(XML_ATTR_AVG));
	}
}
