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
package org.pathvisio.gui.swing.propertypanel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.pathvisio.model.PropertyType;
import org.pathvisio.model.StaticPropertyType;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Pathway;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.gui.swing.SwingEngine;

/**
 * This class knows how to edit comments.
 */
public class CommentsHandler extends AbstractCellEditor implements ContextSensitiveEditor, TableCellEditor, TypeHandler, ActionListener {
	private static final String BUTTON_LABEL = "View/edit comments";
	private static final String BUTTON_COMMAND = "editComments";
	private JButton button;

	private boolean canEdit;
	private SwingEngine swingEngine;
	private PathwayElement currentElement;


	public CommentsHandler() {
		button = new JButton(BUTTON_LABEL);
		button.setActionCommand(BUTTON_COMMAND);
		button.addActionListener(this);
	}


	//-- TypeHandler methods --//

	public PropertyType getType() {
		return StaticPropertyType.COMMENTS;
	}

	public TableCellRenderer getLabelRenderer() {
		return null;
	}

	public TableCellRenderer getValueRenderer() {
		return null;
	}

	public TableCellEditor getValueEditor() {
		return this;
	}


	//-- ContextSensitiveEditor methods --//

	public void updateEditor(SwingEngine aSwingEngine, Collection<PathwayElement> elements,
			Pathway pathway, PropertyView propHandler)
	{
		// can only edit comments for a single item at a time
		canEdit = propHandler.elements.size() == 1;
		swingEngine = aSwingEngine;
		if (canEdit) {
			currentElement = propHandler.elements.iterator().next();
		} else {
			currentElement = null;
		}
	}


	//-- TableCellEditor methods --//

	public Object getCellEditorValue() {
		return currentElement.getComments();
	}


	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return button;
	}


	//-- ActionListener methods --//

	public void actionPerformed(ActionEvent e) {

		if(canEdit && BUTTON_COMMAND.equals(e.getActionCommand())) {
			PathwayElementDialog d = PathwayElementDialog.getInstance(swingEngine, currentElement, false, null, button);
			d.selectPathwayElementPanel(PathwayElementDialog.TAB_COMMENTS);
			d.setVisible(true);
		}
		fireEditingCanceled();  // always fire - PathwayElementDialog saves data itself
	}

}
