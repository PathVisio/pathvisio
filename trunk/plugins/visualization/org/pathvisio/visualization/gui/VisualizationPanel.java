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
//
package org.pathvisio.visualization.gui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mammothsoftware.frwk.ddb.DropDownButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.pathvisio.util.Resources;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationMethod;

/**
 * Main component of the {@link VisualizationDialog}. Has capabilities to
 * add / remove / rename visualizations,
 * and has a subpanel for all available Visualization Methods
 */
public class VisualizationPanel extends JPanel implements ActionListener
{
	static final String ACTION_NEW = "New";
	static final String ACTION_REMOVE = "Remove";
	static final String ACTION_RENAME = "Rename";
	static final String ACTION_COMBO = "Combo";

	VisualizationManager visMgr;

	JComboBox visCombo;
	JPanel methods;

	public VisualizationPanel() {
		FormLayout layout = new FormLayout(
				"pref, 4dlu, 100dlu:grow, 4dlu, left:pref",
				"pref, 4dlu, fill:max(250dlu;pref):grow"
		);
		setLayout(layout);

		visCombo = new JComboBox();
		visCombo.setActionCommand(ACTION_COMBO);
		visCombo.addActionListener(this);
		DropDownButton visButton = new DropDownButton(new ImageIcon(
				Resources.getResourceURL("edit.gif"))
		);
		JMenuItem mNew = new JMenuItem(ACTION_NEW);
		JMenuItem mRemove = new JMenuItem(ACTION_REMOVE);
		JMenuItem mRename = new JMenuItem(ACTION_RENAME);
		mNew.setActionCommand(ACTION_NEW);
		mRemove.setActionCommand(ACTION_REMOVE);
		mRename.setActionCommand(ACTION_RENAME);
		mNew.addActionListener(this);
		mRemove.addActionListener(this);
		mRename.addActionListener(this);
		visButton.addComponent(mNew);
		visButton.addComponent(mRemove);
		visButton.addComponent(mRename);

		methods = new JPanel();
		CellConstraints cc = new CellConstraints();
		add(new JLabel("Visualization"), cc.xy(1, 1));
		add(visCombo, cc.xy(3, 1));
		add(visButton, cc.xy(5, 1));
		add(methods, cc.xyw(1, 3, 5));
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(ACTION_NEW.equals(action)) {
			String name = visMgr.getNewName();
			name = JOptionPane.showInputDialog("Name: ", name);
			if(name == null || "".equals(name)) {
				name = visMgr.getNewName();
			}
			Visualization v = new Visualization(name);
			visMgr.addVisualization(v);
			visMgr.setActiveVisualization(v);
			refresh();

		} else if (ACTION_REMOVE.equals(action)) {
			Visualization v = visMgr.getActiveVisualization();
			if(v != null) {
				visMgr.removeVisualization(v);
			}
			List<Visualization> vl = visMgr.getVisualizations();
			if(vl.size() > 0) {
				visMgr.setActiveVisualization(vl.get(vl.size() - 1));
			}
			refresh();
		} else if (ACTION_RENAME.equals(action)) {
			Visualization v = visMgr.getActiveVisualization();
			if(v != null) {
				String name = v.getName();
				name = JOptionPane.showInputDialog("Name: ", name);
				if(name != null && !"".equals(name)) {
					v.setName(name);
				}
			}
			refresh();
		} else if (ACTION_COMBO.equals(action)) {
			visMgr.setActiveVisualization((Visualization)visCombo.getSelectedItem());
			refresh();
		}
	}

	public void setVisualizationManager(VisualizationManager mgr) {
		this.visMgr = mgr;
		refresh();
	}

	private void refresh() {
		methods.removeAll();
		if(visMgr != null) {
			Visualization v = visMgr.getActiveVisualization();

			visCombo.setModel(new DefaultComboBoxModel(
					visMgr.getVisualizations().toArray()
			));

			visCombo.setSelectedItem(v);

			//Refresh methods panel
			if(v != null) {
				FormLayout layout = new FormLayout("fill:pref:grow");
				DefaultFormBuilder builder =
					new DefaultFormBuilder(layout, methods);
				for(VisualizationMethod m : v.getMethods()) {
					MethodPanel mp = new MethodPanel(m);
					builder.append(mp);
					builder.nextLine();
				}
			}
		} else {
			visCombo.setModel(new DefaultComboBoxModel());
		}
		revalidate();
		repaint();
	}
}
