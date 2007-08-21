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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.jdom.Element;
import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.util.Utils;
import org.pathvisio.util.swt.SwtUtils;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.Visualization.PluginSet;

/**
 * Provides label for Gene Product
 * @author thomas
 *
 */
public class LabelPlugin extends VisualizationPlugin {
	static final String NAME = "Gene product label";
	static final String DESCRIPTION = 
		"This plugin shows a label with customizable font on Gene Products.\n" +
		"The label text can be set to the Gene Product's ID or Symbol.";
	static final Font DEFAULT_FONT = new Font("Arial narrow", Font.PLAIN, 10);
			
	final static int STYLE_ID = 0;
	final static int STYLE_SYMBOL = 1;
	
	final static int ALIGN_CENTER = 0;
	final static int ALIGN_LEFT = 1;
	final static int ALIGN_RIGHT = 2;
	
	Label labelSidePanel;
	
	int style = STYLE_SYMBOL;
	boolean adaptFontSize;
	int align;
	
	Font font;
	Color fontColor;
	
	public LabelPlugin(Visualization v) {
		super(v);		
	    setIsConfigurable(true);
		setDisplayOptions(DRAWING | TOOLTIP);
		setIsGeneric(true);
		setUseProvidedArea(true);
	}

	void setStyle(int style) {
		this.style = style;
		fireModifiedEvent();
	}
	
	void setAlignment(int alignMode) {
		align = alignMode;
		fireModifiedEvent();
	}
	
	int getAlignment() { return align; }
	
	void setOverlay(boolean overlay) {
		if(overlay) {
			Visualization v = getVisualization();
			PluginSet p = v.getPluginSet(this.getClass());
			v.setDisplayOrder(p, Utils.ORDER_FIRST);
		}
		setUseProvidedArea(!overlay);
		fireModifiedEvent();
	}
	
	boolean getOverlay() { return !isUseProvidedArea(); }
	
	public String getName() { return NAME; }
	public String getDescription() { return DESCRIPTION; }
	
	public void initSidePanel(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new FillLayout());
		labelSidePanel = new Label(comp, SWT.CENTER);
	}

	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if(g instanceof GeneProduct) {
			Font f = getFont();
			
			Shape region;
			
			if(isUseProvidedArea()) {
				region = getVisualization().provideDrawArea(this, g);
			} else {
				region = g.createVisualizationRegion();
			}
			
			Rectangle area = region.getBounds();
			
			if(!getOverlay()) {
				g2d.setColor(Color.WHITE);
				g2d.fill(area);
			}
			g2d.setColor(Color.BLACK);
			g2d.draw(area);
			
			g2d.clip(region);
					
			String label = getLabelText((GeneProduct) g);
			
			if(adaptFontSize) {
				//TODO: adapt font size for awt
				//f = SwtUtils.adjustFontSize(f, new Dimension(area.width, area.height), label, g2d);
			}	
			g2d.setFont(f);
			
			g2d.setColor(getFontColor());
			
			Rectangle2D textSize = g2d.getFontMetrics().getStringBounds(label, g2d);
			
			switch(align) {
			case ALIGN_RIGHT: 
				area.x += area.width - textSize.getWidth();
				break;
			case ALIGN_CENTER:
				area.x += (int)(area.width / 2) - (int)(textSize.getWidth()/ 2);
			}
		
			TextLayout tl = new TextLayout(label, g2d.getFont(), g2d.getFontRenderContext());
			Rectangle2D tb = tl.getBounds();
			tl.draw(g2d, 	area.x + (int)(area.width / 2) - (int)(tb.getWidth() / 2), 
						area.y + (int)(area.height / 2) + (int)(tb.getHeight() / 2));			
		}
	}
	
	void setAdaptFontSize(boolean adapt) {
		adaptFontSize = adapt;
		fireModifiedEvent();
	}
	
	void setFont(Font f) {
		if(f != null) {
			font = f;
			fireModifiedEvent();
		}
	}
	
	void setFontColor(Color fc) {
		fontColor = fc;
		fireModifiedEvent();
	}
	
	Color getFontColor() { 
		return fontColor == null ? Color.BLACK : fontColor;
	}
	
	int getFontSize() {
		return getFont().getSize();
	}
	
	Font getFont() {
		return getFont(false);
	}
	
	Font getFont(boolean adjustZoom) {
		Font f = font == null ? DEFAULT_FONT : font;
		if(adjustZoom) {
			int fs = (int)Math.ceil(Engine.getCurrent().getActiveVPathway().vFromM(f.getSize()) * 15);
			f = new Font(f.getName(), f.getStyle(), f.getSize());
		}
		return f;
	}
	
	public Composite visualizeOnToolTip(Composite parent, Graphics g) {
		if(g instanceof GeneProduct) {
			Composite comp = new Composite(parent, SWT.NULL);
			comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			comp.setLayout(new FillLayout());
			Label label = new Label(comp, SWT.CENTER);
			label.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			label.setText(getLabelText((GeneProduct) g));
			return comp;
		}
		return null;
	}

	protected Composite createConfigComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new FillLayout(SWT.VERTICAL));
				
		Group typeGroup = new Group(comp, SWT.NULL);
		typeGroup.setLayout(new RowLayout(SWT.VERTICAL));
		typeGroup.setText("Label text");
		final Button symbol = new Button(typeGroup, SWT.RADIO);
		symbol.setText("Geneproduct name");
		final Button id = new Button(typeGroup, SWT.RADIO);
		id.setText("Geneproduct ID");
		
		SelectionAdapter radioAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if		(e.widget == id) 		setStyle(STYLE_ID);
				else if (e.widget == symbol) 	setStyle(STYLE_SYMBOL);
			}
		};
		
		symbol.setSelection(style == STYLE_SYMBOL);
		id.setSelection(style == STYLE_ID);
		
		id.addSelectionListener(radioAdapter);
		symbol.addSelectionListener(radioAdapter);
		
		createFontComp(comp);
		createOtherComp(comp);
		
		return comp;
	}
		
	Composite createFontComp(Composite parent) {
		GridData span = new GridData(GridData.FILL_HORIZONTAL);
		span.horizontalSpan = 2;
		
		Group fontSizeComp = new Group(parent, SWT.NULL);
		fontSizeComp.setText("Label font");
		fontSizeComp.setLayout(new GridLayout(2, false));
		final Button font = new Button(fontSizeComp, SWT.PUSH);
		font.setLayoutData(span);
		font.setText("Change label font");
		font.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FontDialog fd = new FontDialog(font.getShell());
				fd.setRGB(SwtUtils.color2rgb(getFontColor()));
				fd.setFontList(new FontData[] { SwtUtils.awtFont2FontData(getFont()) });
				setFont(SwtUtils.fontData2awtFont(fd.open()));
				setFontColor(SwtUtils.rgb2color(fd.getRGB()));
			}
		});
		final Button adapt = new Button(fontSizeComp, SWT.CHECK);
		adapt.setLayoutData(span);
		adapt.setText("Adapt font size to genebox size");
		adapt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAdaptFontSize(adapt.getSelection());
			}
		});
		adapt.setSelection(adaptFontSize);
		Label alignLabel = new Label(fontSizeComp, SWT.NONE);
		alignLabel.setText("Alignment:");
		final Combo align = new Combo(fontSizeComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		align.setItems(new String[] { "Center", "Left", "Right" });
		align.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAlignment(align.getSelectionIndex());
			}
		});
		align.select(getAlignment());
		return fontSizeComp;
	}
	
	Composite createOtherComp(Composite parent) {		
		Group other = new Group(parent, SWT.NULL);
		other.setText("Other options");
		other.setLayout(new RowLayout(SWT.VERTICAL));
		final Button overlay = new Button(other, SWT.CHECK);
		overlay.setText("Draw over other visualizations");
		overlay.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setOverlay(overlay.getSelection());
			}
		});
		overlay.setSelection(getOverlay());
		return other;
	}
	
	
	public void visualizeOnSidePanel(Collection<Graphics> objects) { }
	
	private String getLabelText(GeneProduct g) {
		switch(style) {
		case STYLE_ID: 		return g.getGmmlData().getGeneID();
		case STYLE_SYMBOL:
		default:			return g.getGmmlData().getTextLabel();
		}
	}

	static final String XML_ATTR_STYLE = "style";
	static final String XML_ATTR_ADAPT_FONT = "adjustFontSize";
	static final String XML_ATTR_FONTDATA = "font";
	static final String XML_ELM_FONTCOLOR = "font-color";
	static final String XML_ATTR_OVERLAY = "overlay";
	static final String XML_ATTR_ALIGN = "alignment";
	public Element toXML() {
		Element elm = super.toXML();
		elm.setAttribute(XML_ATTR_STYLE, Integer.toString(style));
		elm.setAttribute(XML_ATTR_ADAPT_FONT, Boolean.toString(adaptFontSize));
		
		Font f = getFont();
		String style = "PLAIN";
		if(f.isBold() && f.isItalic()) style = "BOLDITALIC";
		else if (f.isBold()) style = "BOLD";
		else if (f.isItalic()) style = "ITALIC";
		String fs = f.getName() + "-" + style + "-" + f.getSize(); 
		elm.setAttribute(XML_ATTR_FONTDATA, fs);
		
		elm.addContent(ColorConverter.createColorElement(XML_ELM_FONTCOLOR, getFontColor()));
		elm.setAttribute(XML_ATTR_OVERLAY, Boolean.toString(getOverlay()));
		elm.setAttribute(XML_ATTR_ALIGN, Integer.toString(getAlignment()));
		return elm;
	}
	
	public void loadXML(Element xml) {
		super.loadXML(xml);
		
		String styleStr = xml.getAttributeValue(XML_ATTR_STYLE);
		String adaptStr = xml.getAttributeValue(XML_ATTR_ADAPT_FONT);
		String fontStr = xml.getAttributeValue(XML_ATTR_FONTDATA);
		String ovrStr = xml.getAttributeValue(XML_ATTR_OVERLAY);
		String alnStr = xml.getAttributeValue(XML_ATTR_ALIGN);
		Element fcElm = xml.getChild(XML_ELM_FONTCOLOR);
		try {
			if(styleStr != null) setStyle(Integer.parseInt(styleStr));
			if(adaptStr != null) adaptFontSize = Boolean.parseBoolean(adaptStr);
			if(fontStr != null) font = Font.decode(fontStr);
			if(ovrStr != null) setOverlay(Boolean.parseBoolean(ovrStr));
			if(fcElm != null) fontColor = ColorConverter.parseColorElement(fcElm);
			if(alnStr != null) align = Integer.parseInt(alnStr);
		} catch(NumberFormatException e) {
			Logger.log.error("Unable to load configuration for " + NAME, e);
		}
	}
}

