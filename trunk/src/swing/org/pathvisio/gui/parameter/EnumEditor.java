package org.pathvisio.gui.parameter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class EnumEditor implements Editor, ActionListener
{
	JComboBox cbBox;

	private final ParameterModel model;
	private final int index;
	private final ParameterPanel parent;

	public EnumEditor(ParameterModel model, int index, ParameterPanel parent,
			DefaultFormBuilder builder)
	{
		this.index = index;
		this.parent = parent;
		this.model = model;
		
		List<?> values = (List<?>)model.getMetaData(index);
		cbBox = new JComboBox(values.toArray());
		cbBox.addActionListener(this);
		
        builder.append(model.getLabel(index), cbBox, 2);				
        builder.nextLine();
	}

	@Override
	public Object getValue()
	{
		return cbBox.getSelectedItem();
	}

	@Override
	public void setValue(Object val)
	{
		if (ignoreEvent) return;
		cbBox.setSelectedItem(val);
	}

	private boolean ignoreEvent = false;
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		ignoreEvent = true;
		model.setValue(index, cbBox.getSelectedItem());
		ignoreEvent = false;		
	}
}
