package org.pathvisio.visualization.gui;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.visualization.VisualizationMethod;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MethodPanel extends JPanel implements PropertyChangeListener {
	static final String ACTION_CHECKBOX = "check";
	VisualizationMethod method;
	
	public MethodPanel(VisualizationMethod method) {
		this.method = method;
		
		FormLayout layout = new FormLayout(
				"pref, 4dlu, pref, 2dlu, pref",
				"pref, 4dlu, pref"
		);
		setLayout(layout);
		
		JCheckBox checkBox = new JCheckBox();
		checkBox.addPropertyChangeListener(ACTION_CHECKBOX, this);
		JLabel nameLabel = new JLabel(method.getName());
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		CellConstraints cc = new CellConstraints();
		add(checkBox, cc.xy(1,1));
		add(nameLabel, cc.xy(2, 3));
		add(new JLabel(method.getDescription()), cc.xy(3, 5));
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		
	}
}
