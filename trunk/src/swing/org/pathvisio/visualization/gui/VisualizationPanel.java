package org.pathvisio.visualization.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.pathvisio.Engine;
import org.pathvisio.visualization.VisualizationManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mammothsoftware.frwk.ddb.DropDownButton;

public class VisualizationPanel extends JPanel implements PropertyChangeListener {
	static final String ACTION_NEW = "New";
	static final String ACTION_REMOVE = "Remove";
	static final String ACTION_RENAME = "Rename";
	
	VisualizationManager vizMgr;
	
	JComboBox visCombo;
	JPanel methodPanel;
	
	public VisualizationPanel() {
		FormLayout layout = new FormLayout(
				"pref, 4dlu, 100dlu:grow, 4dlu, left:pref",
				"pref, 4dlu, 250dlu:grow"
		);
		setLayout(layout);
		
		visCombo = new JComboBox();
		
		DropDownButton visButton = new DropDownButton(new ImageIcon(
				Engine.getCurrent().getResourceURL("icons/edit.gif"))
		);
		JMenuItem m_new = new JMenuItem(ACTION_NEW);
		JMenuItem m_remove = new JMenuItem(ACTION_REMOVE);
		JMenuItem m_rename = new JMenuItem(ACTION_RENAME);
		m_new.addPropertyChangeListener(ACTION_NEW, this);
		m_remove.addPropertyChangeListener(ACTION_REMOVE, this);
		m_rename.addPropertyChangeListener(ACTION_RENAME, this);
		visButton.addComponent(m_new);
		visButton.addComponent(m_remove);
		visButton.addComponent(m_rename);
		
		methodPanel = new JPanel();
		methodPanel.setBackground(Color.WHITE);
		
		CellConstraints cc = new CellConstraints();
		add(new JLabel("Visualization"), cc.xy(1, 1));
		add(visCombo, cc.xy(3, 1));
		add(visButton, cc.xy(5, 1));
		add(new JScrollPane(methodPanel), cc.xyw(1, 3, 5));
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String action = evt.getPropertyName();
		if(ACTION_NEW.equals(action) || 
				ACTION_RENAME.equals(action) || 
				ACTION_REMOVE.equals(action)) {
			JOptionPane.showMessageDialog(null, action + " not implemented yet");
		}
	}
	
	public void setVisualizationManager(VisualizationManager mgr) {
		this.vizMgr = mgr;
		refresh();
	}
	
	private void refresh() {
		if(vizMgr != null) {
			visCombo.setModel(new DefaultComboBoxModel(
					vizMgr.getVisualizations().toArray()
			));
		} else {
			visCombo.setModel(new DefaultComboBoxModel());
		}
	}
}
