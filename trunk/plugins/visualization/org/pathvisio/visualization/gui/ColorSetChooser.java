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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mammothsoftware.frwk.ddb.DropDownButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.pathvisio.gex.GexManager;
import org.pathvisio.util.Resources;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetManager;

/**
 * A panel containing a colorset combo + button to add/remove/modify colorsets.
 * @author thomas
 *
 */
public class ColorSetChooser extends JPanel implements ActionListener {
	static final String ACTION_NEW = "New";
	static final String ACTION_REMOVE = "Remove";
	static final String ACTION_MODIFY = "Modify";

	private final ColorSetManager csMgr;
	private ColorSetCombo colorSetCombo;
	private final GexManager gexManager;

	public ColorSetChooser(ColorSetManager csMgr, GexManager gexManager) {
		this.csMgr = csMgr;
		this.gexManager = gexManager;

		setLayout(new FormLayout(
				"fill:pref:grow, 4dlu, pref", "pref"
		));

		colorSetCombo = new ColorSetCombo(csMgr);

		DropDownButton csButton = new DropDownButton(new ImageIcon(
				Resources.getResourceURL("edit.gif"))
		);
		JMenuItem mNew = new JMenuItem(ACTION_NEW);
		JMenuItem mRemove = new JMenuItem(ACTION_REMOVE);
		JMenuItem mRename = new JMenuItem(ACTION_MODIFY);
		mNew.setActionCommand(ACTION_NEW);
		mRemove.setActionCommand(ACTION_REMOVE);
		mRename.setActionCommand(ACTION_MODIFY);
		mNew.addActionListener(this);
		mRemove.addActionListener(this);
		mRename.addActionListener(this);
		csButton.addComponent(mNew);
		csButton.addComponent(mRemove);
		csButton.addComponent(mRename);

		CellConstraints cc = new CellConstraints();
		add(colorSetCombo, cc.xy(1, 1));
		add(csButton, cc.xy(3, 1));
	}

	public ColorSetCombo getColorSetCombo() {
		return colorSetCombo;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(ACTION_NEW.equals(action)) {

			ColorSet cs = new ColorSet(csMgr);
			ColorSetDlg dlg = new ColorSetDlg(cs, null, this, gexManager);
			dlg.setVisible(true);
			csMgr.addColorSet(cs);
			colorSetCombo.refresh();
			colorSetCombo.setSelectedItem(cs);
		} else if(ACTION_REMOVE.equals(action)) {
			ColorSet cs = colorSetCombo.getSelectedColorSet();
			if(cs != null) {
				csMgr.removeColorSet(cs);
				colorSetCombo.setSelectedIndex(0);
			}
			colorSetCombo.refresh();
		} else if(ACTION_MODIFY.equals(action)) {
			ColorSet cs = colorSetCombo.getSelectedColorSet();
			if(cs != null) {
				ColorSetDlg dlg = new ColorSetDlg(cs, null, this, gexManager);
				dlg.setVisible(true);
			}
			colorSetCombo.refresh();
			colorSetCombo.setSelectedItem(cs);
		}
	}
}
