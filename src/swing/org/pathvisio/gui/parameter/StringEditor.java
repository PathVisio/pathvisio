package org.pathvisio.gui.parameter;

import java.io.File;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class StringEditor implements Editor, DocumentListener
{
	private JTextField txtField;
	final ParameterModel model;
	final int index;
	final ParameterPanel parent;	
	
	public StringEditor(ParameterModel model, int index, ParameterPanel parent,
			DefaultFormBuilder builder)
	{
		this.index = index;
		this.parent = parent;
		this.model = model;

		txtField = new JTextField();
		txtField.setText("" + model.getValue(index));
		txtField.getDocument().addDocumentListener(this);
		txtField.setToolTipText(model.getHint(index));
        builder.append(model.getLabel(index), txtField, 2);				
        builder.nextLine();
	}

	@Override
	public Object getValue()
	{
		return txtField.getText();
	}

	@Override
	public void setValue(Object val)
	{
		if (ignoreEvent) return;
		txtField.setText("" + val);		
	}
	
	// to prevent duplicate changes
	boolean ignoreEvent = false;
	
	private void handleDocumentEvent(DocumentEvent arg0)
	{
		ignoreEvent = true;
		model.setValue(index, txtField.getText());
		ignoreEvent = false;
	}

	@Override
	public void changedUpdate(DocumentEvent arg0)
	{
		handleDocumentEvent(arg0);		
	}

	@Override
	public void insertUpdate(DocumentEvent arg0)
	{		
		handleDocumentEvent(arg0);		
	}

	@Override
	public void removeUpdate(DocumentEvent arg0)
	{
		handleDocumentEvent(arg0);		
	}
	
}
