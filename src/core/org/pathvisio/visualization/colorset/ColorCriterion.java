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
package org.pathvisio.visualization.colorset;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.jdom.Element;

import org.pathvisio.gui.swt.Engine;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.util.SwtUtils;

public class ColorCriterion extends ColorSetObject {	
	public static final String XML_ELEMENT_NAME = "ColorCriterion";
	
	Criterion criterion;
	
	public static final RGB INITIAL_COLOR = new RGB(255, 255, 255);
	private RGB color;
	public void setColor(RGB color) { 
		this.color = color;
		fireModifiedEvent();
	}
	
	public RGB getColor() { return color == null ? INITIAL_COLOR : color; }
	
	public Criterion getCriterion() { return criterion; }
	
	public ColorCriterion(ColorSet parent, String name) {
		super(parent, name);
		criterion = new Criterion();
	}

	public ColorCriterion(ColorSet parent, Element xml) {
		super(parent, xml);
	}
	
	RGB getColor(HashMap<Integer, Object> data, int idSample) throws Exception {
		if(criterion.evaluate(data, idSample)) return color;
		return null;
	}
	
	public String getXmlElementName() {
		return XML_ELEMENT_NAME;
	}
	
	protected void loadXML(Element xml) {
		super.loadXML(xml);
		try {
			String expression = xml.getAttributeValue(XML_ATTR_EXPRESSION);
			criterion = new Criterion();
			criterion.setExpression(expression);
			Element ce = xml.getChild(XML_ELM_COLOR);
			if(ce != null) color = ColorConverter.parseColorElement(ce);
		} catch(Exception e) {
			Engine.log.error("Unable to load ColorCriterion", e);
		}
	}
	
	static final String XML_ELM_COLOR = "color";
	static final String XML_ATTR_EXPRESSION = "expression";
	public Element toXML() {
		Element elm = super.toXML();
		Element ce = ColorConverter.createColorElement(XML_ELM_COLOR, getColor());
		elm.addContent(ce);
		elm.setAttribute(XML_ATTR_EXPRESSION, criterion.getExpression());
		
		return elm;
	}
		
	public static class ColorCriterionComposite extends ConfigComposite {
		final int colorLabelSize = 15;
		CriterionComposite critComp;
		Text exprText;
		CLabel colorLabel;
		Color color;
		
		public ColorCriterionComposite(Composite parent, int style) {
			super(parent, style);
		}
		
		void refresh() {
			super.refresh();
			critComp.refresh();
			changeColorLabel(getInput() == null ? null : getInput().getColor());
		}
		
		ColorCriterion getInput() {
			return (ColorCriterion)input;
		}
		
//		public boolean save() {
//			if(input != null) try {
//				critComp.saveToCriterion();
//			} catch(Exception e) {
//				return false;
//			}
//			return true;
//		}
			
		public void setInput(ColorSetObject o) {
			super.setInput(o);
			if(o == null) critComp.setInput(null);
			else critComp.setInput(((ColorCriterion)o).getCriterion());
			refresh();
		}
		
		RGB askColor() {
			ColorDialog dg = new ColorDialog(getShell());
			dg.setRGB(getInput().getColor());
			return dg.open();
		}
		
		void changeColor(RGB rgb) {
			if(rgb != null) {
				ColorCriterion c = getInput();
				if(c != null) c.setColor(rgb);
				changeColorLabel(rgb);
			}
		}
		
		void changeColorLabel(RGB rgb) {
			if(rgb != null) {
				color = SwtUtils.changeColor(color, rgb, colorLabel.getDisplay());
				colorLabel.setBackground(color);
			}
		}
		
				void createContents() {
			setLayout(new GridLayout());
			
			Composite superComp = super.createNameComposite(this);
			superComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Composite colorComp = createColorComp(this);
			colorComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					    
		    critComp = new CriterionComposite(this, null);
		    critComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		    critComp.fetchSymbolsFromGex();
		}
		
		Composite createColorComp(Composite parent) {
			Composite colorComp = new Composite(parent, SWT.NULL);
			colorComp.setLayout(new GridLayout(3, false));
			
			final GridData colorLabelGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			colorLabelGrid.widthHint = colorLabelGrid.heightHint = colorLabelSize;
			
			Label label = new Label(colorComp, SWT.CENTER);
			label.setText("Color:");

			colorLabel = new CLabel(colorComp, SWT.SHADOW_IN);
			colorLabel.setLayoutData(colorLabelGrid);
			colorLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

			Button colorButton = new Button(colorComp, SWT.PUSH);
			colorButton.addListener(SWT.Selection | SWT.Dispose, new Listener() {
				public void handleEvent(Event e) {
					switch(e.type) {
					case SWT.Selection:
						RGB rgb = askColor();
						changeColor(rgb);
					break;
					case SWT.Dispose:
						color.dispose();
					break;
					}
				}
			});
			
			colorButton.setLayoutData(colorLabelGrid);
			colorButton.setText("...");
			
			return colorComp;
		}
	}
}
