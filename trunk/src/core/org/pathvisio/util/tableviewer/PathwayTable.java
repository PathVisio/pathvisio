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
package org.pathvisio.util.tableviewer;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.pathvisio.gui.Engine;
import org.pathvisio.preferences.Preferences;
import org.pathvisio.util.TableColumnResizer;
import org.pathvisio.util.tableviewer.TableData.Row;
import org.pathvisio.util.SwtUtils.FileInputDialog;


/**
 * This composite displays a table on which {@link TableData} can be
 * displayed and opened
 */
public class PathwayTable extends Composite {
	public static final String COLNAME_FILE = "fileName";
	
	protected TableViewer tableViewer;
	private TableColumnResizer columnResizer;
	private Display display;
	
	public PathwayTable(Composite parent, int style) {
		super(parent, SWT.NULL);
		display = getShell().getDisplay();

		createContents();
	}
	
	public void setTableData(final TableData srs) {
		display.asyncExec(new Runnable() { 
			//asyncExec, because can be accessed from seperate thread
			public void run() {
				//Recreate the table's columns to display the TableData columns
				Table t = tableViewer.getTable();
				for(TableColumn tc : t.getColumns()) tc.dispose();
				
				ArrayList<String> attrNames = srs.getColNames();
				String[] colProps = new String[attrNames.size()];
				for(int i = 0; i < attrNames.size(); i++) {
					final TableColumn tc = new TableColumn(t, SWT.NULL);
					tc.setText(attrNames.get(i));
					tc.setWidth(20);
					colProps[i] = attrNames.get(i);
					tc.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							Table t = tableViewer.getTable();
							if(tableViewer.getTable().getSortColumn() == tc)
								t.setSortDirection(t.getSortDirection() == SWT.DOWN ? SWT.UP : SWT.DOWN);
							else {
								tableViewer.getTable().setSortColumn(tc);
								if(t.getSortDirection() == SWT.NONE) t.setSortDirection(SWT.DOWN);
							}
							tableViewer.setSorter(new PathwaySorter(tc.getText()));
						}
					});
				}
				tableViewer.setColumnProperties(colProps);
				tableViewer.setInput(srs);
				columnResizer.doResize();
			}
		});

	}
	
	public void refreshTableViewer(final boolean updateLabels) {
		display.asyncExec(new Runnable() { 
			//asyncExec, because can be accessed from seperate thread
			public void run() {
				tableViewer.refresh(updateLabels);
			}
		});
	}
	
	public int getNrRows() { return tableViewer.getTable().getItemCount(); }
	
	public TableViewer getTableViewer() { return tableViewer; }
		
	protected void createContents() {
		setLayout(new FillLayout());
		initTable(this);
	}
	
	protected void initTable(Composite parent) {
		Table t = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		columnResizer = new TableColumnResizer(t, parent);
		t.addControlListener(columnResizer);
		t.addMouseListener(tableMouseListener);
		t.setHeaderVisible(true);
		tableViewer = new TableViewer(t); 
		tableViewer.setContentProvider(tableContentProvider);
		tableViewer.setLabelProvider(tableLabelProvider);
	}
	
	private MouseAdapter tableMouseListener = new MouseAdapter() {
		public void mouseDoubleClick(MouseEvent e) {
			Row sr = (Row)
			((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
			if(sr == null) return;
			try {
				String pw = sr.getCell(COLNAME_FILE).getText();
				File pwFile = new File(pw);
				if(!pwFile.canRead()) {
					FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
					fd.setFilterPath(Engine.getPreferences().getString(Preferences.PREF_DIR_PWFILES));
					FileInputDialog fid = new FileInputDialog(getShell(), "Specify pathway file", 
							"Couldn't find pathway file, please specify which pathway to open",
							pwFile.getAbsolutePath(), null, fd);
					if(fid.open() == FileInputDialog.OK) {
						pw = fid.getValue();
					}
				}
				Engine.openPathway(pw);
			} catch(Exception ex) { 
				Engine.log.error("when trying to open pathway from pathway table", ex);
			}
		}
	};
	
	private IStructuredContentProvider tableContentProvider = new IStructuredContentProvider() {

		public Object[] getElements(Object inputElement) {			
			if(inputElement instanceof TableData) {
				TableData srs = (TableData)inputElement;
				return srs.getResults().toArray();
			}
			return new Object[] {};
		}

		public void dispose() {	}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
		
	};
	
	private ITableLabelProvider tableLabelProvider = new ITableLabelProvider() {

		public Image getColumnImage(Object element, int columnIndex) { return null;	}

		public String getColumnText(Object element, int columnIndex) {
			TableData.Row sr = (TableData.Row)element;
			String name = (String)tableViewer.getColumnProperties()[columnIndex];
			
			try { return sr.getCell(name).getText(); } catch (Exception e) { return "error"; }
		}

		public void addListener(ILabelProviderListener listener) {	}
		public void dispose() {	}
		public boolean isLabelProperty(Object arg0, String arg1) { return false; }
		public void removeListener(ILabelProviderListener arg0) { }
		
	};
	
	private class PathwaySorter extends ViewerSorter {
		String property;
		int propertyIndex;
		
		public PathwaySorter(String sortByProperty) {
			property = sortByProperty;
		}
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			int sortDirection = SWT.DOWN;
			if(viewer instanceof TableViewer)
				sortDirection = ((TableViewer)viewer).getTable().getSortDirection();
			
			Row r1, r2;
			if(sortDirection == SWT.UP) {
				r1 = (Row)e1;
				r2 = (Row)e2;
			} else {
				r1 = (Row)e2;
				r2 = (Row)e1;
			}

			return r1.getCell(property).compareTo(r2.getCell(property));
		}
	}
}	
