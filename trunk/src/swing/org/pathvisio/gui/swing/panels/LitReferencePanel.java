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
package org.pathvisio.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
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

		final WrapCellRenderer cr = new WrapCellRenderer();
		refTable = new JTable(references) {
			public TableCellRenderer getCellRenderer(int row, int column) {
				return cr;
			}
		};
		
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
		
		PublicationXRefDialog d = new PublicationXRefDialog(xref, null, this);
		d.setVisible(true);
		if(d.getExitCode().equals(PublicationXRefDialog.OK)) {
			biopax.addElementReference(xref);
			refresh();			
		}
	}
	
	class WrapCellRenderer extends JTextArea implements TableCellRenderer {
	     public WrapCellRenderer() {
	       setLineWrap(true);
	       setWrapStyleWord(true);
	    }
	 
	   public Component getTableCellRendererComponent(JTable table, Object
	           value, boolean isSelected, boolean hasFocus, int row, int column) {
	       setText(value == null ? "" : value.toString());
	       setSize(table.getColumnModel().getColumn(column).getWidth(),
	               getPreferredSize().height);
	       if (table.getRowHeight(row) != getPreferredSize().height) {
	               table.setRowHeight(row, getPreferredSize().height + table.getIntercellSpacing().height);
	       }
	       setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
	       return this;
	   }
	} 
}
