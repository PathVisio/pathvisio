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
import java.awt.Color;

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
import org.pathvisio.util.swt.SwtUtils;
import org.pathvisio.util.swt.TableColumnResizer;
import org.pathvisio.visualization.colorset.ColorGradient.ColorValuePair;

class ColorGradientComposite extends ColorSetObjectComposite 
{	
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
		getInput().addColorValuePair(getInput().new ColorValuePair(Color.RED, 0));
		refresh();
	}

	protected void refresh()
	{
		colorTable.refresh();
	}
	
	void removeColor() {
		ColorValuePair cvp = (ColorValuePair)
		((IStructuredSelection)colorTable.getSelection()).getFirstElement();
		getInput().removeColorValuePair(cvp);
		refresh();
	}
	
	protected void createContents() {
		setLayout(new GridLayout());
		
		createButtonComp(this);

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
					RGB rgb = SwtUtils.color2rgb(((ColorValuePair)element).getColor());
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
					return SwtUtils.color2rgb(cvp.getColor());
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
					cvp.setColor(SwtUtils.rgb2color((RGB)value));
				} else {
					cvp.setValue(Double.parseDouble((String)value));
				}
				refresh();
			}
		};
	}
}
