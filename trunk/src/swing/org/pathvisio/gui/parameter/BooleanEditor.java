package org.pathvisio.gui.parameter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class BooleanEditor implements Editor, ActionListener
{
	private JCheckBox ckVal;
	
	private final ParameterModel model;
	private final int index;
	private final ParameterPanel parent;
	
	public BooleanEditor(ParameterModel model, int index, ParameterPanel parent,
			DefaultFormBuilder builder)
	{
		this.index = index;
		this.parent = parent;
		this.model = model;

		ckVal = new JCheckBox();
		ckVal.setToolTipText(model.getHint(index));
		ckVal.addActionListener(this);
        builder.append(model.getLabel(index), ckVal, 2);				
        builder.nextLine();
	}

	@Override
	public Object getValue()
	{
		return Boolean.valueOf(ckVal.isSelected());
	}

	@Override
	public void setValue(Object val)
	{
		if (ignoreEvent) return;
		ckVal.setSelected ((Boolean)val);
	}

	private boolean ignoreEvent = false;
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{		
		ignoreEvent = true;
		model.setValue(index, ckVal.isSelected());
		ignoreEvent = false;
	}

}
