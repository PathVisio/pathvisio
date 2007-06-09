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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jdom.Element;

import org.pathvisio.gui.Engine;
import org.pathvisio.util.ColorConverter;
import org.pathvisio.util.TableColumnResizer;

/**
 * This class represent a color gradient used for data visualization
 */
public class ColorGradient extends ColorSetObject {
	public static final String XML_ELEMENT_NAME = "ColorGradient";

	private ArrayList<ColorValuePair> colorValuePairs;
	
	/**
	 * Constructor for this class
	 * @param parent 		colorset this gradient belongs to
	 * @param name 			name of the gradient
	 */
	public ColorGradient(ColorSet parent, String name)
	{
		super(parent, name);
		getColorValuePairs();
	}
		
	public ColorGradient(ColorSet parent, Element xml) {
		super(parent, xml);
	}
	
	/**
	 * Get the the colors and corresponding values used in this gradient as {@link ColorValuePair}
	 * @return ArrayList containing the ColorValuePairs
	 */
	public ArrayList<ColorValuePair> getColorValuePairs() 
	{ 
		if(colorValuePairs == null) {//Not initialized yet, use defaults
			colorValuePairs = new ArrayList<ColorValuePair>();
			colorValuePairs.add(new ColorValuePair(new RGB(0,255,0), -1));
			colorValuePairs.add(new ColorValuePair(new RGB(255,255,0), 0));
			colorValuePairs.add(new ColorValuePair(new RGB(255,0,0), 1));
		}
		return colorValuePairs;
	}
	/**
	 * Add a {@link ColorValuePair} to this gradient
	 */
	public void addColorValuePair(ColorValuePair cvp)
	{
		if(colorValuePairs == null) { 
			colorValuePairs = new ArrayList<ColorValuePair>();
		}
		colorValuePairs.add(cvp);
		fireModifiedEvent();
	}
	/**
	 * Remove a {@link ColorValuePair} from this gradient
	 */
	public void removeColorValuePair(ColorValuePair cvp)
	{
		if(colorValuePairs == null || !colorValuePairs.contains(cvp)) return;
		colorValuePairs.remove(cvp);
		fireModifiedEvent();
	}
			
	/**
	 * get the color of the gradient for this value
	 * @param value
	 * @return	{@link RGB} containing the color information for the corresponding value
	 * or null if the value does not have a valid color for this gradient
	 */
	public RGB getColor(double value)
	{
		double[] minmax = getMinMax(); //Get the minimum and maximum values of the gradient
		double valueStart = 0;
		double valueEnd = 0;
		RGB colorStart = null;
		RGB colorEnd = null;
		Collections.sort(colorValuePairs);
		//If value is larger/smaller than max/min then set the value to max/min
		//TODO: make this optional
		if(value < minmax[0]) value = minmax[0]; else if(value > minmax[1]) value = minmax[1];
		
		//Find what colors the value is in between
		for(int i = 0; i < colorValuePairs.size() - 1; i++)
		{
			ColorValuePair cvp = colorValuePairs.get(i);
			ColorValuePair cvpNext = colorValuePairs.get(i + 1);
			if(value >= cvp.value && value <= cvpNext.value)
			{
				valueStart = cvp.getValue();
				colorStart = cvp.getColor();
				valueEnd = cvpNext.getValue();
				colorEnd = cvpNext.getColor();
			}
		}
		if(colorStart == null || colorEnd == null) return null; //Check if the values/colors are found
		// Interpolate to find the color belonging to the given value
		double alpha = (value - valueStart) / (valueEnd - valueStart);
		double red = colorStart.red + alpha*(colorEnd.red - colorStart.red);
		double green = colorStart.green + alpha*(colorEnd.green - colorStart.green);
		double blue = colorStart.blue + alpha*(colorEnd.blue - colorStart.blue);
		RGB rgb = null;
		
		//Try to create an RGB, if the color values are not valid (outside 0 to 255)
		//This method returns null
		try {
			rgb = new RGB((int)red, (int)green, (int)blue);
		} catch (Exception e) { 
			Engine.log.error("GmmlColorGradient:getColor: " + 
					red + "," + green + "," +blue + ", for value " + value, e);
		}
		return rgb;
	}
	
	public RGB getColor(HashMap<Integer, Object> data, int idSample) throws NumberFormatException
	{
		double value = (Double)data.get(idSample);
		return getColor(value);
	}
	
	String getXmlElementName() {
		return XML_ELEMENT_NAME;
	}
	
	public Element toXML() {
		Element elm = super.toXML();
		for(ColorValuePair cvp : colorValuePairs)
			elm.addContent(cvp.toXML());
		return elm;
	}
	
	protected void loadXML(Element xml) {
		super.loadXML(xml);
		colorValuePairs = new ArrayList<ColorValuePair>();
		for(Object o : xml.getChildren(ColorValuePair.XML_ELEMENT))
			colorValuePairs.add(new ColorValuePair((Element) o));
	}
	
	/**
	 * Find the minimum and maximum values used in this gradient
	 * @return a double[] of length 2 with respecively the minimum and maximum values
	 */
	public double[] getMinMax()
	{
		double[] minmax = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
		for(ColorValuePair cvp : colorValuePairs)
		{
			minmax[0] = Math.min(cvp.value, minmax[0]);
			minmax[1] = Math.max(cvp.value, minmax[1]);
		}
		return minmax;
	}
	
	/**
	 * This class contains a color and its corresponding value used for the {@link ColorGradient}
	 */
	public class ColorValuePair implements Comparable<ColorValuePair> {
		static final String XML_ELEMENT = "color-value";
		static final String XML_ATTR_VALUE = "value";
		static final String XML_ELM_COLOR = "color";
		private RGB color;
		private double value;
		
		public ColorValuePair(RGB color, double value)
		{
			this.color = color;
			this.value = value;
		}
		
		public ColorValuePair(Element xml) {
			Object o = xml.getChildren(XML_ELM_COLOR).get(0);
			color = ColorConverter.parseColorElement((Element)o);
			value = Double.parseDouble(xml.getAttributeValue(XML_ATTR_VALUE));
		}
		
		public RGB getColor() { return color; }
		public void setColor(RGB rgb) {
			color = rgb;
			fireModifiedEvent();
		}
		
		public double getValue() { return value; }
		public void setValue(double v) {
			value = v;
			fireModifiedEvent();
		}
		
		public int compareTo(ColorValuePair o)
		{
			return (int)(value - o.value);
		}
		
		public Element toXML() {
			Element elm = new Element(XML_ELEMENT);
			elm.setAttribute(XML_ATTR_VALUE, Double.toString(value));
			elm.addContent(ColorConverter.createColorElement(XML_ELM_COLOR, color));
			return elm;
		}
	}
	
	public static class ColorGradientComposite extends ConfigComposite {	
		static final String[] tableColumns = new String[] {"Color", "Value"};
		TableViewer colorTable;
		
		public ColorGradientComposite(Composite parent, int style) {
			super(parent, style);
		}
		
		ColorGradient getInput() {
			return (ColorGradient)input;
		}
		
		public void setInput(ColorSetObject o) {
			super.setInput(o);
			colorTable.setInput(o);
		}
				
		void addColor() {
			getInput().addColorValuePair(getInput().new ColorValuePair(new RGB(255, 0, 0), 0));
    		colorTable.refresh();
		}
		
		void removeColor() {
    		ColorValuePair cvp = (ColorValuePair)
    		((IStructuredSelection)colorTable.getSelection()).getFirstElement();
    		getInput().removeColorValuePair(cvp);
    		colorTable.refresh();
		}
		
		void createContents() {
			setLayout(new GridLayout());
			Composite nameComp = createNameComposite(this);
			nameComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Composite buttonComp = createButtonComp(this);

			Composite tableComp = createColorTable(this);
			tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
		
		Composite createButtonComp(Composite parent) {
			Composite comp = new Composite(parent, SWT.NULL);
			comp.setLayout(new RowLayout(SWT.HORIZONTAL));
			
			
			final Button addColor = new Button(comp, SWT.PUSH);
			addColor.setText("Add color");
		    final Button removeColor = new Button(comp, SWT.PUSH);
		    removeColor.setText("Remove color");
		    
			SelectionListener buttonAdapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(e.widget == addColor) addColor();
					else removeColor();
				}
			};
			addColor.addSelectionListener(buttonAdapter);
		    removeColor.addSelectionListener(buttonAdapter);
		    return comp;
		}

		Composite createColorTable(Composite parent) {
			Composite tableComp = new Composite(parent, SWT.NULL);
			tableComp.setLayout(new GridLayout());
					    			
		    Table table = new Table(tableComp, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		    
		    table.setHeaderVisible(true);
		    TableColumn colorCol = new TableColumn(table, SWT.LEFT);
		    TableColumn valueCol = new TableColumn(table, SWT.LEFT);
		    valueCol.setText(tableColumns[1]);
		    colorCol.setText(tableColumns[0]);

		    colorCol.setWidth(45);
		    colorCol.setResizable(false);
		    
		    table.addControlListener(
		    		new TableColumnResizer(table, tableComp, new int[] { 0, 100 }));
		    
		    colorTable = new TableViewer(table);
		    colorTable.setColumnProperties(tableColumns);
		    
		    colorTable.setLabelProvider(createLabelProvider());
		    colorTable.setContentProvider(createContentProvider());
		    colorTable.setCellModifier(createCellModifier());
		    
		    CellEditor[] cellEditors = new CellEditor[2];
		    cellEditors[1] = new TextCellEditor(table);
		    cellEditors[0] = new ColorCellEditor(table);
		    colorTable.setCellEditors(cellEditors);
		    
		    return tableComp;
		}
		
		IStructuredContentProvider createContentProvider() {
			return new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					return ((ColorGradient)inputElement).getColorValuePairs().toArray();
				}
				public void dispose() {	}
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
			};
		}
		
		ITableLabelProvider createLabelProvider() {
			return new ITableLabelProvider() {
				private Image colorImage;
				
				public void dispose() {
					if(colorImage != null) colorImage.dispose();
				}
				
				public Image getColumnImage(Object element, int columnIndex) { 
					if(columnIndex == 0) {
						RGB rgb = ((ColorValuePair)element).color;
						colorImage = new Image(null, ColorSetComposite.createColorImage(rgb));
						return colorImage;
					}
					return null;
				}
				
				public String getColumnText(Object element, int columnIndex) {
					if(columnIndex == 1) {
						return Double.toString(((ColorValuePair)element).getValue());
					}
					return null;
				}

				public void addListener(ILabelProviderListener listener) {}
				public boolean isLabelProperty(Object element, String property) {
					return false;
				}
				public void removeListener(ILabelProviderListener listener) {}
			};
		}

		ICellModifier createCellModifier() {
			return new ICellModifier() {
				public boolean canModify(Object element, String property) {
					return true;
				}

				public Object getValue(Object element, String property) {
					ColorValuePair cvp = (ColorValuePair)element;
					if(property.equals(tableColumns[0])) {
						return cvp.getColor();
					} else {
						return Double.toString(cvp.getValue());
					}
				}

				public void modify(Object element, String property, Object value) {
					if(element instanceof TableItem) {
						element = ((TableItem)element).getData();
					}
					ColorValuePair cvp = (ColorValuePair)element;
					if(property.equals(tableColumns[0])) {
						cvp.setColor((RGB)value);
					} else {
						cvp.setValue(Double.parseDouble((String)value));
					}
					colorTable.refresh();
				}
			};
		}
	}
}
