// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jdom.Element;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.dialogs.OkCancelDialog;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.util.Utils;
import org.pathvisio.util.swing.FontChooser;
import org.pathvisio.view.GeneProduct;
import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationMethod;

/**
 * Visualization method to put the label pack on a DataNode. Since
 * visualizations are drawn on top of the original DataNode, it becomes
 * necessary to redraw the original textLabel to see which gene / metabolite
 * you're dealing with.
 *
 * This visualization method also lets you use the id instead of the symbol
 * as label.
 */
public class DataNodeLabel extends VisualizationMethod implements ActionListener {
	static final String DISPLAY_ID = "Identifier";
	static final String DISPLAY_LABEL = "Text label";
	static final String ACTION_APPEARANCE = "Appearance...";

	static final Font DEFAULT_FONT = new Font("Arial narrow", Font.PLAIN, 10);

	final static int ALIGN_CENTER = 0;
	final static int ALIGN_LEFT = 1;
	final static int ALIGN_RIGHT = 2;

	String display = DISPLAY_LABEL;

	boolean adaptFontSize;
	int align = ALIGN_CENTER;

	Font font;
	Color fontColor;

	public DataNodeLabel(Visualization v, String registeredName) {
		super(v, registeredName);
	    setIsConfigurable(true);
		setUseProvidedArea(false); //Overlay by default
	}

	public String getDescription() {
		return "Draws a label";
	}

	public String getName() {
		return "Text label";
	}

	public JPanel getConfigurationPanel() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout(
				"pref, 4dlu, pref, 4dlu, pref, 8dlu, pref",
				"pref"
		);
		panel.setLayout(layout);

		JRadioButton radioId = new JRadioButton(DISPLAY_ID);
		JRadioButton radioLabel = new JRadioButton(DISPLAY_LABEL);
		radioId.setActionCommand(DISPLAY_ID);
		radioLabel.setActionCommand(DISPLAY_LABEL);
		radioId.addActionListener(this);
		radioLabel.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(radioId);
		group.add(radioLabel);

		JButton appearance = new JButton(ACTION_APPEARANCE);
		appearance.setActionCommand(ACTION_APPEARANCE);
		appearance.addActionListener(this);

		CellConstraints cc = new CellConstraints();
		panel.add(new JLabel("Display: "), cc.xy(1, 1));
		panel.add(radioLabel, cc.xy(3, 1));
		panel.add(radioId, cc.xy(5, 1));
		panel.add(appearance, cc.xy(7, 1));

		//Initial values
		if(DISPLAY_ID.equals(display)) {
			radioId.setSelected(true);
		} else {
			radioLabel.setSelected(true);
		}
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(DISPLAY_ID.equals(action) || DISPLAY_LABEL.equals(action)) {
			setDisplayAttribute(action);
		} else if(ACTION_APPEARANCE.equals(action)) {
			OkCancelDialog optionsDlg = new OkCancelDialog(
					null, ACTION_APPEARANCE, (Component)e.getSource(), true, false
			);
			optionsDlg.setDialogComponent(createAppearancePanel());
			optionsDlg.pack();
			optionsDlg.setVisible(true);
		}
	}

	JPanel createAppearancePanel() {
		final JLabel preview = new JLabel(getFont().getFamily());
		preview.setOpaque(true);
		preview.setBackground(Color.WHITE);
		preview.setFont(getFont());

		final JButton font = new JButton("...");
		font.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font f = FontChooser.showDialog(null, (Component)e.getSource(), getFont());
				if(f != null) {
					setFont(f);
					preview.setText(f.getFamily());
					preview.setFont(f);
				}
			}
		});
		final JComboBox align = new JComboBox(
				new String[] { "Center", "Left", "Right" }
		);
		align.setSelectedIndex(getAlignment());

		align.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAlignment(align.getSelectedIndex());
			}
		});

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, fill:pref:grow, 4dlu, pref", ""));
		builder.setDefaultDialogBorder();
		builder.append("Font: ", preview, font);
		builder.nextLine();
		builder.append("Alignment:", align, 3);
		return builder.getPanel();
	}

	void setDisplayAttribute(String display) {
		this.display = display;
		modified();
	}

	void setAlignment(int alignMode) {
		align = alignMode;
		modified();
	}

	int getAlignment() { return align; }

	void setOverlay(boolean overlay) {
		setUseProvidedArea(!overlay);
		modified();
	}

	boolean getOverlay() { return !isUseProvidedArea(); }

	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if(g instanceof GeneProduct) {
			String label = getLabelText((GeneProduct) g);
			if(label == null || label.length() == 0) {
				return;
			}

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

			g2d.clip(region);

			if(adaptFontSize) {
				//TODO: adapt font size for awt
				//f = SwtUtils.adjustFontSize(f, new Dimension(area.width, area.height), label, g2d);
			}
			g2d.setFont(f);

			g2d.setColor(getFontColor());

			TextLayout tl = new TextLayout(label, g2d.getFont(), g2d.getFontRenderContext());
			Rectangle2D tb = tl.getBounds();

			switch(align) {
			case ALIGN_LEFT:
				area.x -= area.width / 2 - tb.getWidth() / 2 - 1;
				break;
			case ALIGN_RIGHT:
				area.x += area.width / 2 - tb.getWidth() / 2 - 1;
			}

			tl.draw(g2d, 	(int)area.getX() + (int)(area.getWidth() / 2) - (int)(tb.getWidth() / 2),
					(int)area.getY() + (int)(area.getHeight() / 2) + (int)(tb.getHeight() / 2));
		}
	}

	public Component visualizeOnToolTip(Graphics g) {
		// TODO Auto-generated method stub
		return null;
	}

	void setAdaptFontSize(boolean adapt) {
		adaptFontSize = adapt;
		modified();
	}

	void setFont(Font f) {
		if(f != null) {
			font = f;
			modified();
		}
	}

	void setFontColor(Color fc) {
		fontColor = fc;
		modified();
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
			//int fs = (int)Math.ceil(Engine.getCurrent().getActiveVPathway().vFromM(f.getSize()));
			f = new Font(f.getName(), f.getStyle(), f.getSize());
		}
		return f;
	}

	private String getLabelText(GeneProduct g) {
		String text = g.getPathwayElement().getTextLabel();
		if(display != null) {
			if(DISPLAY_LABEL.equals(display)) {
				text = g.getPathwayElement().getTextLabel();
			} else if(DISPLAY_ID.equals(display)){
				text = g.getPathwayElement().getGeneID();
			}
		}
		return text;
	}

	static final String XML_ATTR_DISPLAY = "display";
	static final String XML_ATTR_ADAPT_FONT = "adjustFontSize";
	static final String XML_ATTR_FONTDATA = "font";
	static final String XML_ELM_FONTCOLOR = "font-color";
	static final String XML_ATTR_OVERLAY = "overlay";
	static final String XML_ATTR_ALIGN = "alignment";
	public Element toXML() {
		Element elm = super.toXML();
		elm.setAttribute(XML_ATTR_DISPLAY, display);
		elm.setAttribute(XML_ATTR_ADAPT_FONT, Boolean.toString(adaptFontSize));

		elm.setAttribute(XML_ATTR_FONTDATA, Utils.encodeFont(getFont()));

		elm.addContent(ColorConverter.createColorElement(XML_ELM_FONTCOLOR, getFontColor()));
		elm.setAttribute(XML_ATTR_OVERLAY, Boolean.toString(getOverlay()));
		elm.setAttribute(XML_ATTR_ALIGN, Integer.toString(getAlignment()));
		return elm;
	}

	public void loadXML(Element xml) {
		super.loadXML(xml);

		String styleStr = xml.getAttributeValue(XML_ATTR_DISPLAY);
		String adaptStr = xml.getAttributeValue(XML_ATTR_ADAPT_FONT);
		String fontStr = xml.getAttributeValue(XML_ATTR_FONTDATA);
		String ovrStr = xml.getAttributeValue(XML_ATTR_OVERLAY);
		String alnStr = xml.getAttributeValue(XML_ATTR_ALIGN);
		Element fcElm = xml.getChild(XML_ELM_FONTCOLOR);
		try {
			if(styleStr != null) setDisplayAttribute(styleStr);
			if(adaptStr != null) adaptFontSize = Boolean.parseBoolean(adaptStr);
			if(fontStr != null) font = Font.decode(fontStr);
			if(ovrStr != null) setOverlay(Boolean.parseBoolean(ovrStr));
			if(fcElm != null) fontColor = ColorConverter.parseColorElement(fcElm);
			if(alnStr != null) align = Integer.parseInt(alnStr);
		} catch(NumberFormatException e) {
			Logger.log.error("Unable to load configuration for " + getName(), e);
		}
	}

	@Override
	public int defaultDrawingOrder()
	{
		return 1;
	}
}