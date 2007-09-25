//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.dialogs.PublicationXRefDialog;
import org.pathvisio.model.PathwayElement;

public class LitReferencePanel extends PathwayElementPanel implements ActionListener {
	static final String ADD = "Add";
	static final String REMOVE = "Remove";
	static final String EDIT = "Edit";

	BiopaxElementManager biopax;
	List<PublicationXRef> xrefs;

	JTable refTable;
	DefaultTableModel references;
	JPanel buttons;

	public LitReferencePanel() {
		setLayout(new BorderLayout(5, 5));
		xrefs = new ArrayList<PublicationXRef>();

		references = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		final WrapRenderer cr = new WrapRenderer();
		refTable = new JTable(references);
		refTable.setDefaultRenderer(PublicationXRef.class, cr);

		refTable.setTableHeader(null);
		refTable.setIntercellSpacing(new Dimension(0, 5));

		//Table doesn't adjust to border
		//See bug: 4222732 at bugs.sun.com
		//refTable.setBorder(BorderFactory.createTitledBorder("References"));
		//Workaround, create a JPanel with border, add table to JPanel
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(new JScrollPane(refTable), BorderLayout.CENTER);
		tablePanel.setBorder(BorderFactory.createTitledBorder("References"));
		refTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					editPressed();
				}
			}
		});


		buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));

		JButton add = new JButton(ADD);
		JButton remove=  new JButton(REMOVE);
		JButton edit = new JButton(EDIT);
		add.addActionListener(this);
		remove.addActionListener(this);
		edit.addActionListener(this);
		buttons.add(add);
		buttons.add(remove);
		buttons.add(edit);

		add(tablePanel, BorderLayout.CENTER);
		add(buttons, BorderLayout.LINE_END);
	}

	public void setReadOnly(boolean readonly) {
		super.setReadOnly(readonly);
		setChildrenEnabled(buttons, !readonly);
	}

	public void setInput(PathwayElement e) {
		if(e != getInput()) {
			biopax = new BiopaxElementManager(e);
		}
		super.setInput(e);
	}

	public void refresh() {
		xrefs = biopax.getPublicationXRefs();
		Object[][] data = new Object[xrefs.size()][1];
		for(int i = 0; i < xrefs.size(); i++) {
			data[i][0] = xrefs.get(i);
		}
		references.setDataVector(data, new Object[] { "" });
		int prefferedWidth = refTable.getWidth();
		TableColumn column = null;
		for (int i = 0; i < references.getColumnCount(); i++) {
			column = refTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(prefferedWidth);
			column.setCellRenderer(new WrapRenderer());
		}

	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(ADD)) {
			addPressed();
		} else if(e.getActionCommand().equals(REMOVE)) {
			removePressed();
		} else if(e.getActionCommand().equals(EDIT)) {
			editPressed();
		}
	}

	private void editPressed() {
		int r = refTable.getSelectedRow();
		if(r > -1) {
			PublicationXRef xref = (PublicationXRef)references.getValueAt(r, 0);
			if(xref != null) {
				PublicationXRefDialog d = new PublicationXRefDialog(xref, null, this, false);
				d.setVisible(true);
			}
			refresh();
		}
	}

	private void removePressed() {
		for(int r : refTable.getSelectedRows()) {
			Object o = references.getValueAt(r, 0);
			biopax.removeElementReference((PublicationXRef)o);
		}
		refresh();
	}

	private void addPressed() {
		PublicationXRef xref = new PublicationXRef(biopax.getUniqueID());

		final PublicationXRefDialog d = new PublicationXRefDialog(xref, null, this);
		if(!SwingUtilities.isEventDispatchThread()) {	
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						d.setVisible(true);
					}
				});
			} catch (Exception e) {
				Logger.log.error("Unable to open dialog");
			}
		} else {
			d.setVisible(true);
		}
		if(d.getExitCode().equals(PublicationXRefDialog.OK)) {
			biopax.addElementReference(xref);
			refresh();			
		}
	}

	//Modified from:
	//http://forum.java.sun.com/thread.jspa?threadID=753164&messageID=4305668
	//(TK)
	static class WrapRenderer extends JEditorPane	implements TableCellRenderer {
		protected final DefaultTableCellRenderer adaptee =
			new DefaultTableCellRenderer();
		/** map from table to map of rows to map of column heights */
		private final Map cellSizes = new HashMap();

		public WrapRenderer() {
//			setLineWrap(true);
//			setWrapStyleWord(true);
			setContentType("text/html");
			setEditable(false);
		}

		public Component getTableCellRendererComponent(//
				JTable table, Object obj, boolean isSelected,
				boolean hasFocus, int row, int column) {
			// set the colours, etc. using the standard for that platform
			adaptee.getTableCellRendererComponent(table, obj,
					isSelected, hasFocus, row, column);
			setForeground(adaptee.getForeground());
			setBackground(adaptee.getBackground());
			setBorder(adaptee.getBorder());
			setFont(adaptee.getFont());
			setText(adaptee.getText());

			// This line was very important to get it working with JDK1.4
			TableColumnModel columnModel = table.getColumnModel();
			setSize(columnModel.getColumn(column).getWidth(), 100000);
			int height_wanted = (int) getPreferredSize().getHeight();
			addSize(table, row, column, height_wanted);
			height_wanted = findTotalMaximumRowSize(table, row);
			if (height_wanted != table.getRowHeight(row)) {
				table.setRowHeight(row, height_wanted);
			}
			return this;
		}

		protected void addSize(JTable table, int row, int column,
				int height) {
			Map rows = (Map) cellSizes.get(table);
			if (rows == null) {
				cellSizes.put(table, rows = new HashMap());
			}
			Map rowheights = (Map) rows.get(new Integer(row));
			if (rowheights == null) {
				rows.put(new Integer(row), rowheights = new HashMap());
			}
			rowheights.put(new Integer(column), new Integer(height));
		}

		/**
		 * Look through all columns and get the renderer.  If it is
		 * also a TextAreaRenderer, we look at the maximum height in
		 * its hash table for this row.
		 */
		protected int findTotalMaximumRowSize(JTable table, int row) {
			int maximum_height = 0;
			Enumeration columns = table.getColumnModel().getColumns();
			while (columns.hasMoreElements()) {
				TableColumn tc = (TableColumn) columns.nextElement();
				TableCellRenderer cellRenderer = tc.getCellRenderer();
				if (cellRenderer instanceof WrapRenderer) {
					WrapRenderer tar = (WrapRenderer) cellRenderer;
					maximum_height = Math.max(maximum_height,
							tar.findMaximumRowSize(table, row));
				}
			}
			return maximum_height + 5;
		}

		private int findMaximumRowSize(JTable table, int row) {
			Map rows = (Map) cellSizes.get(table);
			if (rows == null) return 0;
			Map rowheights = (Map) rows.get(new Integer(row));
			if (rowheights == null) return 0;
			int maximum_height = 0;
			for (Iterator it = rowheights.entrySet().iterator();
			it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				int cellHeight = ((Integer) entry.getValue()).intValue();
				maximum_height = Math.max(maximum_height, cellHeight);
			}
			return maximum_height;
		}
	}

}
