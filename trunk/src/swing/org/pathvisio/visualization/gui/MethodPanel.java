package org.pathvisio.visualization.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.visualization.VisualizationMethod;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MethodPanel extends JPanel implements ActionListener {
	VisualizationMethod method;
	JPanel configPanel;
	JCheckBox checkBox;
	
	public MethodPanel(VisualizationMethod method) {
		this.method = method;
		
		JPanel top = new JPanel();
		FormLayout layout = new FormLayout(
				"pref, 4dlu, pref, 2dlu, pref",
				"pref"
		);
		top.setLayout(layout);

		checkBox = new JCheckBox();
		checkBox.addActionListener(this);
		JLabel nameLabel = new JLabel(method.getName());
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		CellConstraints cc = new CellConstraints();
		top.add(checkBox, cc.xy(1,1));
		top.add(nameLabel, cc.xy(3, 1));
		top.add(new JLabel(method.getDescription()), cc.xy(5, 1));
		
		configPanel = method.getConfigurationPanel();
		configPanel.setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(top);
		add(configPanel);
		
		//Initial values
		configPanel.setVisible(method.isActive());
		checkBox.setSelected(method.isActive());
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == checkBox) {
			method.setActive(checkBox.isSelected());
			if(method.isConfigurable()) {
				configPanel.setVisible(checkBox.isSelected());
				revalidate();
			}
		}
	}
}
