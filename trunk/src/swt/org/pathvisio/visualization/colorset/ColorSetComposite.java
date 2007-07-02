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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pathvisio.util.Utils;
import org.pathvisio.util.swt.SwtUtils;
import org.pathvisio.util.swt.TableColumnResizer;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationManager.VisualizationEvent;
import org.pathvisio.visualization.VisualizationManager.VisualizationListener;
import org.pathvisio.visualization.colorset.ColorCriterion.ColorCriterionComposite;
import org.pathvisio.visualization.colorset.ColorGradient.ColorGradientComposite;

public class ColorSetComposite extends Composite implements VisualizationListener {
	final int colorLabelSize = 15;
	ColorSet colorSet;
	
	TableViewer objectsTable;
	
	Composite colorButtons;
	Group objectsGroup;
	ObjectSettingsComposite objectSettings;
	Color colorNCM, colorNGF, colorNDF;
	CLabel labelColorNCM, labelColorNGF, labelColorNDF;
	Combo colorSetCombo;
	Text nameText;
	
	public ColorSetComposite(Composite parent, int style) {
		super(parent, style);
		createContents();
		VisualizationManager.addListener(this);
	}
	
	public void dispose() {
		colorNCM.dispose();
		colorNGF.dispose();
		colorNDF.dispose();
		super.dispose();
	}
	
	public void setInput(ColorSet cs) {
		colorSet = cs;
		if(colorSet == null) {
			setObjectsGroupEnabled(false);
		} else {
			setObjectsGroupEnabled(true);
			initColorLabels();
			initName();
			objectsTable.setInput(colorSet);
			objectsTable.getTable().select(0);
		}
	}
		
	void setObjectsGroupEnabled(boolean enable) {
		SwtUtils.setCompositeAndChildrenEnabled(objectsGroup, enable);
	}
		
	void initName() {
		nameText.setText(colorSet.getName());
	}
	
	void initColorLabels() {
		changeColorLabel(labelColorNCM, SwtUtils.color2rgb(colorSet.getColor(ColorSet.ID_COLOR_NO_CRITERIA_MET)));
		changeColorLabel(labelColorNGF, SwtUtils.color2rgb(colorSet.getColor(ColorSet.ID_COLOR_NO_GENE_FOUND)));
		changeColorLabel(labelColorNDF, SwtUtils.color2rgb(colorSet.getColor(ColorSet.ID_COLOR_NO_DATA_FOUND)));
	}
		
	public void refreshCombo() {
		colorSetCombo.setItems(ColorSetManager.getColorSetNames());
		colorSetCombo.layout();
		colorSetCombo.select(0);
	}

	void createContents() {
		setLayout(new GridLayout());
		Composite colorSetComp = createColorSetComposite(this);
		colorSetComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		objectsGroup = new Group(this, SWT.NULL);
		objectsGroup.setText("Criteria");
		objectsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		objectsGroup.setLayout(new GridLayout(2, false));
		
		Composite listComp = createObjectList(objectsGroup);
		listComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		objectSettings = new ObjectSettingsComposite(objectsGroup, SWT.NONE);
		objectSettings.setLayoutData(new GridData(GridData.FILL_BOTH));

		refreshCombo();
		
		colorSetCombo.select(0);
	}

	Composite createObjectList(Composite parent) {
		Composite listComp = new Composite(parent, SWT.NULL);
		listComp.setLayout(new GridLayout());
		
		Composite tableComp = new Composite(listComp, SWT.NULL);
		tableComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableComp.setLayout(new FillLayout());
		Table table = new Table(tableComp, SWT.BORDER | SWT.SINGLE);
		TableColumn coCol = new TableColumn(table, SWT.LEFT);
		coCol.setText("Name");
		table.addControlListener(new TableColumnResizer(table, listComp));
		
		objectsTable = new TableViewer(table);
		objectsTable.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return ((ColorSet)inputElement).getObjects().toArray();
			}
			
			public void dispose() { }
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		});
		objectsTable.setLabelProvider(new ITableLabelProvider() {			
			private HashMap<Object, Image> images = new HashMap<Object, Image>();
											
			public void dispose() {
				for(Image img : images.values()) disposeImage(img);
			}
			
			void disposeImage(Image img) { if(img != null && !img.isDisposed()) img.dispose(); }
			
			public Image getColumnImage(Object element, int columnIndex) {
				Image img = images.get(element);
				if(element instanceof ColorGradient) {
					disposeImage(img);
					img = new Image(null, createGradientImage((ColorGradient)element));
					images.put(element, img);
					return img;
				}
				if(element instanceof ColorCriterion) {
					disposeImage(img);
					img = new Image(null, createColorImage(
							SwtUtils.color2rgb(((ColorCriterion)element).getColor())));
					images.put(element, img);
					return img;
				}
				return null;
			}
			
			public String getColumnText(Object element, int columnIndex) {
				if(element instanceof ColorSetObject)
					return ((ColorSetObject)element).getName();
				return "";
			}
			
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {}
			public void addListener(ILabelProviderListener listener) {}
		});
		
		objectsTable.addSelectionChangedListener(new ISelectionChangedListener() {
			boolean ignore;
			ColorSetObject previous = null;
			public void selectionChanged(SelectionChangedEvent event) {
				if(ignore) {
					ignore = false;
					return;
				}
				boolean save = true;
				if(previous != null && colorSet.getObjects().contains(previous))
					save = objectSettings.save();
				if(save) {
					previous = getSelectedObject();
					objectSettings.setInput(previous);
				} else {
					ignore = true;
					objectsTable.setSelection(new StructuredSelection(previous));
				}
			}
		});
		
		//Drag & Drop support
		DragSource ds = new DragSource(objectsTable.getTable(), DND.DROP_MOVE);
		ds.addDragListener(new ColorSetObjectDragAdapter());
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		DropTarget dt = new DropTarget(objectsTable.getTable(), DND.DROP_MOVE);
		dt.addDropListener(new ColorSetObjectDropAdapter());
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		
		Composite buttons = new Composite(listComp, SWT.NULL);
		buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
		final Button add = new Button(buttons, SWT.PUSH);
		add.setText("Add");
		final Button remove = new Button(buttons, SWT.PUSH);
		remove.setText("Remove");
		
		SelectionListener buttonAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget == add) addColorSetObject();
				else removeColorSetObject();
			}
		};
		
		add.addSelectionListener(buttonAdapter);
		remove.addSelectionListener(buttonAdapter);
		
		return  listComp;
	}
		
	RGB askColor(RGB current) {
		ColorDialog cd = new ColorDialog(getShell());
		cd.setRGB(current);
		return cd.open();
	}
	
	void changeColor(CLabel label) {
		ColorDialog cd = new ColorDialog(getShell());
		cd.setRGB(label.getBackground().getRGB());
		changeColor(label, cd.open());
	}
	
	void changeColor(CLabel label, RGB rgb) {
		int id = -1;
		if(label == labelColorNCM)
			id = ColorSet.ID_COLOR_NO_CRITERIA_MET;
		else if	(label == labelColorNDF)
			id = ColorSet.ID_COLOR_NO_DATA_FOUND;
		else if (label == labelColorNGF)
			id = ColorSet.ID_COLOR_NO_GENE_FOUND;

		if(id >= 0) {
			if(rgb != null) {
				colorSet.setColor(id, SwtUtils.rgb2color(rgb));
				changeColorLabel(label, rgb);
			}
		}
	}
	
	void changeColorLabel(CLabel label, RGB rgb) {
		Color c = null;
		if(label == labelColorNCM)
			c = colorNCM = SwtUtils.changeColor(colorNCM, rgb, getDisplay());
		else if	(label == labelColorNDF)
			c = colorNDF = SwtUtils.changeColor(colorNDF, rgb, getDisplay());
		else if (label == labelColorNGF)
			c = colorNGF = SwtUtils.changeColor(colorNGF, rgb, getDisplay());
		
		if(c != null) label.setBackground(c);
	}
	
	void addColorSet() {
		ColorSetManager.newColorSet(null);
		refreshCombo();
		colorSetCombo.select(ColorSetManager.getColorSets().size() - 1);
	}
	
	void removeColorSet() {
		ColorSetManager.removeColorSet(colorSetCombo.getSelectionIndex());
		refreshCombo();
	}
	
	void addColorSetObject() {
		final int NEW_GRADIENT = 10;
		final int NEW_EXPRESSION = 11;
		Dialog dialog = new Dialog(getShell()) {
			int newObject = NEW_GRADIENT;
			public int open() {
				int open = super.open();
				return open == CANCEL ? CANCEL : newObject;
			}
			protected Control createDialogArea(Composite parent) {
				setBlockOnOpen(true);
				Composite contents = new Composite(parent, SWT.NULL);
				contents.setLayout(new RowLayout(SWT.VERTICAL));
				final Button gradient = new Button(contents, SWT.RADIO);
				gradient.setText("Color by gradient");
				final Button expression = new Button(contents, SWT.RADIO);
				expression.setText("Color by boolean expression");
				
				SelectionListener lst = new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if(e.widget == gradient) newObject =  NEW_GRADIENT;
						else newObject = NEW_EXPRESSION;
					}
				};
				
				gradient.addSelectionListener(lst);
				expression.addSelectionListener(lst);
				gradient.setSelection(true);
				return contents;
			}
		};
		
		int type = dialog.open();
		if(type == Dialog.CANCEL) return;
		ColorSetObject newCso = null;
		switch(type) {
		case NEW_GRADIENT:
			newCso = new ColorGradient(colorSet, colorSet.getNewName("New gradient"));
			break;
		case NEW_EXPRESSION:
			newCso = new ColorCriterion(colorSet, colorSet.getNewName("New expression"));
			break;
		}
		if(newCso != null) {
			colorSet.addObject(newCso);
			objectsTable.refresh();
			objectsTable.setSelection(new StructuredSelection(newCso));
		}
		
	}
	
	void removeColorSetObject() {
		colorSet.removeObject(getSelectedObject());
		objectsTable.refresh();
	}
	
	ColorSetObject getSelectedObject() {
		return (ColorSetObject)
			((IStructuredSelection)objectsTable.getSelection()).getFirstElement();
	}
	void modifyName(String newName) {
		if(!newName.equals("")) colorSet.setName(newName);
	}
	
	void colorSetSelected() {
		int index = colorSetCombo.getSelectionIndex();
		if(index == -1)
			setInput(null);
		else
			setInput(ColorSetManager.getColorSets().get(index));
	}
	
	Composite createColorSetComposite(Composite parent) {
		Composite csComp = new Composite(parent, SWT.NULL);
		csComp.setLayout(new GridLayout(2, false));
		
		//Combo + buttons
		Composite comboComp = new Composite(csComp, SWT.NULL);
		comboComp.setLayout(new GridLayout());
		
		Label comboLabel = new Label(comboComp, SWT.NULL);
		comboLabel.setText("Color set:");
		colorSetCombo = new Combo(comboComp, SWT.SINGLE | SWT.READ_ONLY);
		colorSetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		colorSetCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				colorSetSelected();
				enableSettings(colorSet != null);
			}
			
			void enableSettings(boolean enable) {
				nameText.setEnabled(enable);
				SwtUtils.setCompositeAndChildrenEnabled(colorButtons, enable);
			}
		});
		
		Composite buttons = new Composite(comboComp, SWT.NULL);
		buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
		final Button add = new Button(buttons, SWT.PUSH);
		add.setText("Add");
		final Button remove = new Button(buttons, SWT.PUSH);
		remove.setText("Remove");
		
		SelectionListener buttonAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget == add) addColorSet();
				else removeColorSet();
			}
		};
		
		add.addSelectionListener(buttonAdapter);
		remove.addSelectionListener(buttonAdapter);
		
		//Name + colors
		Composite csSettings = new Composite(csComp, SWT.NULL);
		csSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		csSettings.setLayout(new GridLayout(2, false));
		
		Label nameLabel = new Label(csSettings, SWT.NULL);
		nameLabel.setText("Name: ");
		
		nameText = new Text(csSettings, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modifyName(nameText.getText());
			}
		});
		
				
		colorButtons = createFixedColors(csSettings);
		GridData span2cols = new GridData(GridData.FILL_HORIZONTAL);
		span2cols.horizontalSpan = 2;
		colorButtons.setLayoutData(span2cols);
		
		nameText.setEnabled(false);
		SwtUtils.setCompositeAndChildrenEnabled(colorButtons, false);
		return csComp;
	}
	
	Composite createFixedColors(Composite parent) {		
		Group buttonGroup = new Group(parent, SWT.NULL);
		buttonGroup.setLayout(new GridLayout(3, false));
		buttonGroup.setText("Colors");
		
		CLabel[] colorLabels = new CLabel[3];
		String[] names = new String[] {
				"No criteria met", "Gene not found", "No data found"
		};
		for(int i = 0; i < colorLabels.length; i++) {
			Composite comp = new Composite(buttonGroup, SWT.NULL);
			comp.setLayout(new GridLayout(3, false));
			final CLabel clabel = new CLabel(comp, SWT.SHADOW_IN);
			clabel.setLayoutData(SwtUtils.getColorLabelGrid());
			Button b = new Button(comp, SWT.PUSH);
			b.setLayoutData(SwtUtils.getColorLabelGrid());
			b.setText("...");
			b.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) { 
					changeColor(clabel);
				}
			});
			Label label = new Label(comp, SWT.NULL);
			label.setText(names[i]);
			colorLabels[i] = clabel;
		}
		labelColorNCM = colorLabels[0];
		labelColorNGF = colorLabels[1];
		labelColorNDF = colorLabels[2];
				
		return buttonGroup;
	}
	
	class ObjectSettingsComposite extends Composite {
		StackLayout stack;
		ColorSetObject input;
		ColorGradientComposite gradientComp;
		ColorCriterionComposite criterionComp;
		Composite nothing;
		
		public ObjectSettingsComposite(Composite parent, int style) {
			super(parent, style);
			createContents();
		}
		
		public void setInput(ColorSetObject cso) {
			input = cso;
			refresh();
		}
		
		public boolean save() {
			if(input instanceof ColorGradient) return gradientComp.save();
			else if (input instanceof ColorCriterion) return criterionComp.save();
			else return true;
		}
		
		public void refresh() {
			if(input == null) stack.topControl = nothing;
			else {
				if(input instanceof ColorGradient) {
					stack.topControl = gradientComp;
					gradientComp.setInput(getSelectedObject());
				} else {
					stack.topControl = criterionComp;
					criterionComp.setInput(getSelectedObject());
				}
			}
			layout();
		}
		
		void createContents() {
			stack = new StackLayout();
			setLayout(stack);
						
			//Gradient
			gradientComp = new ColorGradientComposite(this, SWT.NULL);
			//Criterion
			criterionComp = new ColorCriterionComposite(this, SWT.NULL);
			//Nothing
			new Composite(this, SWT.NULL);
		}		
	}
	
	/**
	 * creates an 16x16 image filled with the given color
	 * @param rgb the color to fill the image with
	 * @return imagedata of a 16x16 image filled with the given color
	 */
	static ImageData createColorImage(RGB rgb) {
		PaletteData colors = new PaletteData(new RGB[] { rgb, new RGB(0,0,0) });
		ImageData data = new ImageData(16, 16, 1, colors);
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				if(j == 0 || j == 15 || i == 0 || i == 15) //Black border
					data.setPixel(i, j, 1);
				else
					data.setPixel(i, j, 0);
			}
		}
		return data;
	}
	
	/**
	 * creates a 16x16 image representing the given {@link GmmlColorGradient}
	 * @param cg the gradient to create the image from
	 * @return imagedata representing the gradient
	 */
	static ImageData createGradientImage(ColorGradient cg)
	{
		PaletteData colors = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF);
		ImageData data = new ImageData(16, 16, 24, colors);
		double[] minmax = cg.getMinMax();
		for(int i = 0; i < 16; i++)
		{
			RGB rgb = SwtUtils.color2rgb(cg.getColor(minmax[0] + (i * (minmax[1]- minmax[0])) / 16 ));
			if(rgb == null)
				rgb = new RGB(255,255,255);
			for(int j = 0; j < 16; j++)
			{
				if(j == 0 || j == 15 || i == 0 || i == 15) //Black border
					data.setPixel(i, j, colors.getPixel(new RGB(0,0,0)));
				else
					data.setPixel(i, j, colors.getPixel(rgb));
			}
		}
		return data;
	}


	public void visualizationEvent(VisualizationEvent e) {
		switch(e.type) {
		case(VisualizationEvent.COLORSET_MODIFIED):
			if(objectsTable != null && !objectsTable.getTable().isDisposed())
				objectsTable.refresh();
		}
		
	}
	
    private class ColorSetObjectDragAdapter extends DragSourceAdapter {
    	public void dragStart(DragSourceEvent e) {
    		e.doit = getSelectedObject() == null ? false : true;
    	}
    	
    	public void dragSetData(DragSourceEvent e) {
    		ColorSetObject selected = getSelectedObject();
    		int csoIndex = colorSet.colorSetObjects.indexOf(selected);
    		e.data = Integer.toString(csoIndex);
    	}
    }
    
    private class ColorSetObjectDropAdapter extends DropTargetAdapter {
    	public void drop(DropTargetEvent e) {
    		TableItem item = (TableItem)e.item;
    		if(item != null)
    		{
    			Object selected = item.getData();
    			int index = Integer.parseInt((String)e.data);
    			if(index >= 0) {
    				ColorSetObject cso = colorSet.getObjects().get(index);
    				Utils.moveElement(colorSet.getObjects(), cso, colorSet.getObjects().indexOf(selected));
    				objectsTable.refresh();
    			}
    		}
    	}
    }
}
