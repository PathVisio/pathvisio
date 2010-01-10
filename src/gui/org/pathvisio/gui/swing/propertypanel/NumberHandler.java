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

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.awt.Component;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.DefaultCellEditor;
import javax.swing.BorderFactory;

import org.pathvisio.model.PropertyType;
import org.pathvisio.debug.Logger;

/**
 * This class knows how to handle numbers.  It renders and edits numbers and validates input based on the current
 * {@link java.util.Locale}.
 *
 * @author Mark Woon
 */
public class NumberHandler extends DefaultCellEditor implements TableCellRenderer, TypeHandler {
	private TableCellRenderer cellRenderer = new DefaultTableCellRenderer();
	private JTextField textField;
	private NumberFormat numberFormat;
	private Class valueClass;
	private PropertyType propertyType;


	/**
	 * Factory method for getting a NumberHandler.
	 *
	 * @param valueCls the subclass of {@link Number} expected
	 * @throws IllegalArgumentException if valueCls is not a subclass of {@link Number}.
	 */
	public static NumberHandler buildHandler(PropertyType type, Class valueCls) {

		if (!Number.class.isAssignableFrom(valueCls)) {
			throw new IllegalArgumentException(valueCls.toString() + " is not a subclass of java.lang.Number");
		}
		NumberFormat numFormat;
		if (valueCls == Integer.class || valueCls == Long.class || valueCls == Short.class || valueCls == Byte.class) {
			numFormat = NumberFormat.getIntegerInstance();
		} else {
			numFormat = NumberFormat.getNumberInstance();
		}
		return new NumberHandler(type, valueCls, numFormat);
	}


	/**
	 * Constructor.
	 *
	 * @param numFormat the number formatter to use to parse/validate the input
	 * @param valueCls the Class of the value to get back
	 */
	NumberHandler(PropertyType type, Class valueCls, NumberFormat numFormat) {
		super(new JTextField());
		textField = (JTextField)getComponent();
		textField.setBorder(BorderFactory.createEmptyBorder());
		numberFormat = numFormat;
		valueClass = valueCls;
		propertyType = type;
	}


	NumberFormat getNumberFormat() {
		return numberFormat;
	}

	JTextField getTextField() {
		return textField;
	}


	/**
	 * Formats the value for display.  Subclasses should override this to modify how the number should be displayed.
	 */
	Object formatValue(Object value) {
		return numberFormat.format(value);
	}



	//-- TypeHandler methods --//

	public PropertyType getType() {
		return propertyType;
	}

	public TableCellRenderer getLabelRenderer() {
		return null;
	}

	public TableCellRenderer getValueRenderer() {
		return this;
	}

	public TableCellEditor getValueEditor() {
		return this;
	}



	//-- TableCellRenderer methods --//

	/**
	 * Overriden to format value.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		value = formatValue(value);
		return cellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}



	//-- TableCellEditor methods --//


	@Override
	public Object getCellEditorValue() {

		try {
			Number val = numberFormat.parse(textField.getText());
			if (valueClass.isInstance(val)) {
				return val;
			}
			if (valueClass == Integer.class) {
				return val.intValue();
			}
			if (valueClass == Double.class) {
				return val.doubleValue();
			}
			if (valueClass == Short.class) {
				return val.shortValue();
			}
			if (valueClass == Long.class) {
				return val.longValue();
			}
			if (valueClass == Float.class) {
				return val.floatValue();
			}
			return val;
		} catch (ParseException ex) {
			Logger.log.warn("Error parsing '" + textField.getText() + "' into a number", ex);
		}
		try {
			return valueClass.newInstance();
		} catch (Exception ex) {
			return null;
		}
	}


	/**
	 * Overriden to format the value of the text field.
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		value = formatValue(value);
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}


	/**
	 * Overriden to only stop editing if value is valid.
	 */
	@Override
	public boolean stopCellEditing() {

		if (isValidNumber(textField.getText())) {
			return super.stopCellEditing();
		}
		JOptionPane.showMessageDialog(
				SwingUtilities.getWindowAncestor(textField),
				"The value is not a valid " + valueClass.getSimpleName().toLowerCase() +".",
				"Invalid " + valueClass.getSimpleName(),
				JOptionPane.ERROR_MESSAGE
		);
		return false;
	}


	/**
	 * Overriden to only cancel editing if value is valid.
	 */
	@Override
	public void cancelCellEditing() {

		if (isValidNumber(textField.getText())) {
			super.cancelCellEditing();
		}
	}


	/**
	 * Checks that value is a valid number.
	 * See http://www.ibm.com/developerworks/java/library/j-numberformat/index.html for issues with NumberFormat.
	 */
	private boolean isValidNumber(String value) {

		ParsePosition parsePosition = new ParsePosition(0);
		Number result = numberFormat.parse(value, parsePosition);
		return !(value.length() != parsePosition.getIndex() || result == null);
	}
}
