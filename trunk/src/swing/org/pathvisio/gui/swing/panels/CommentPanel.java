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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.pathvisio.model.PathwayElement.Comment;

public class CommentPanel extends PathwayElementPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	protected static final String ADD = "Add comment";
	protected static final String REMOVE = "Remove comment";
	
	CommentsTableModel tableModel;
	JTable commentsTable;
	JPanel buttonPanel;
	
	public CommentPanel() {
		setLayout(new BorderLayout(5, 5));
		
		commentsTable = new JTable();
		commentsTable.setBorder(BorderFactory.createCompoundBorder());
		commentsTable.setRowHeight(20);
		commentsTable.setMinimumSize(new Dimension(200, 200));
		buttonPanel = new JPanel();
		JButton add = new JButton(ADD);
		add.setActionCommand(ADD);
		add.addActionListener(this);
		JButton remove = new JButton(REMOVE);
		remove.setActionCommand(REMOVE);
		remove.addActionListener(this);
		
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(add);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(remove);
		
		JScrollPane contentPane = new JScrollPane(commentsTable);
		add(contentPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
		
		tableModel = new CommentsTableModel();
		commentsTable.setModel(tableModel);
		

	}

	public void setReadOnly(boolean readonly) {
		super.setReadOnly(readonly);
		setChildrenEnabled(buttonPanel, !readonly);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(ADD)) {
			getInput().addComment("Type you comment here", "");
		} else if(e.getActionCommand().equals(REMOVE)) {
			int row = commentsTable.getSelectedRow();
			if(row > -1) getInput().removeComment(tableModel.comments.get(row));
		}
		refresh();
	}
	
	public void refresh() {
		tableModel.setComments(getInput().getComments());	
	}
	
	class CommentsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		
		List<Comment> comments = new ArrayList<Comment>();
		void setComments(List<Comment> input) {
			if(input == null) input = new ArrayList<Comment>();
			this.comments = input;
			fireTableDataChanged();
		}
		
		public int getColumnCount() {
			return 2;
		}
		
		public int getRowCount() {
			return comments.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Comment c = comments.get(rowIndex);
			String value = null;
			if(c != null) {
				if(columnIndex == 0) value = c.getSource();
				else value = c.getComment();
			}
			return value;
		}
		
		public String getColumnName(int column) {
			return column == 0 ? "Source" : "Comment";
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			String value = (String)aValue;
			Comment c = comments.get(rowIndex);
			if(columnIndex == 0) c.setSource(value);
			else c.setComment(value);
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return !readonly;
		}
	}
}