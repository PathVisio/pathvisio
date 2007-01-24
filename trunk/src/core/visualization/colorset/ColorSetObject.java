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
package visualization.colorset;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jdom.Element;

import visualization.VisualizationManager;
import visualization.VisualizationManager.VisualizationEvent;

/**
 * This class represent any object that can be present in a colorset
 * e.g. a gradient or boolean expression.
 */
public abstract class ColorSetObject {
	
	/**
	 * The parent colorset, that this colorSetObject is a part of.
	 */
	private ColorSet parent;
	
	/**
	 * The display name of this colorSetObject
	 */
	private String name;
	
	/**
	 * getter for name, the name of this colorSetObject
	 */
	public void setName(String _name) 
	{
		this.name = _name; 
	}
	
	/**
	 * setter for name, the name of this colorSetObject
	 */
	public String getName() { return name; }
	
	public ColorSet getColorSet() { return parent; }
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name 			name of the gradient
	 */
	public ColorSetObject(ColorSet parent, String name) 
	{	
		this.parent = parent;
		this.name = name;
	}
	
	public ColorSetObject(ColorSet parent, Element xml) {
		this.parent = parent;
		loadXML(xml);
	}
				
	/**
	 * get the color defined by the colorset object for the given data
	 * @param data {@link HashMap}<Integer, Object> containing data (String or double) for every sampleId 
	 * @param sample id of the sample that is visualized using this color
	 * @return {@link RGB} with the color returned by the colorset object after evaluating the input data,
	 * null if the input data doesn't result in a valid color
	 * @throws Exception 
	 */
	abstract RGB getColor(HashMap<Integer, Object> data, int idSample) throws Exception;
	
	/**
	 * Returns the parent colorset
	 */
	public ColorSet getParent()
	{
		return parent;
	}

	protected void fireModifiedEvent() {
		VisualizationManager.fireVisualizationEvent(
				new VisualizationEvent(this, VisualizationEvent.COLORSET_MODIFIED));
	}
	
	abstract String getXmlElementName();
	
	static final String XML_ATTR_NAME = "name";
	
	public Element toXML() {
		Element elm = new Element(getXmlElementName());
		elm.setAttribute(XML_ATTR_NAME, name);
		return elm;
	}
	
	protected void loadXML(Element xml) {
		name = xml.getAttributeValue(XML_ATTR_NAME);
	}
				
	public static abstract class ConfigComposite extends Composite {
		final int colorLabelSize = 15;
		ColorSetObject input;
		Text nameText;
		
		public ConfigComposite(Composite parent, int style) {
			super(parent, style);
			createContents();
		}
		
		public void setInput(ColorSetObject input) {
			this.input = input;
			refresh();
		}
		
		public boolean save() {
			return true;
		}
		
		void refresh() {
			String nm = "";
			if(input != null) nm = input.getName();
			nameText.setText(nm);
		}
				
		void changeName(String name) {
			input.setName(name);
		}
		
		abstract void createContents();
		
		protected Composite createNameComposite(Composite parent) {
			Composite comp = new Composite(parent, SWT.NULL);
			comp.setLayout(new GridLayout(2, false));
			
			Label nameLabel = new Label(comp, SWT.CENTER);
			nameLabel.setText("Name:");
		
			nameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    nameText.addModifyListener(new ModifyListener() {
		    	public void modifyText(ModifyEvent e) {
		    		changeName(nameText.getText());
		    	}
		    });
		    return comp;
		}
	}
}
