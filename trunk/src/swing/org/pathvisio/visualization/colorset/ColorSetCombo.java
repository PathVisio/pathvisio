// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.visualization.colorset;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.pathvisio.debug.Logger;
import org.pathvisio.visualization.gui.ColorSetDlg;

public class ColorSetCombo extends JComboBox implements ActionListener {
	ColorSet NEW = new ColorSet("new...");
	
	ColorSetManager csMgr;
	
	public ColorSetCombo(ColorSetManager csMgr, List<ColorSet> colorSets) {
		super();
		this.csMgr = csMgr;
		ArrayList<ColorSet> csClone = new ArrayList<ColorSet>();
		csClone.addAll(colorSets);
		csClone.add(NEW);
		csClone.add(NEW);
		setModel(new DefaultComboBoxModel(csClone.toArray()));
		setRenderer(new ColorSetRenderer());
		addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		Logger.log.trace("Action: " + getSelectedItem());
		if(getSelectedItem() == NEW) {
			ColorSet cs = new ColorSet(csMgr.getNewName());
			new ColorSetDlg(cs, null, this).setVisible(true);
		}
	}
	
	class ColorSetRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			l.setText(((ColorSet)value).getName());
			l.setBackground(Color.WHITE);
			return l;
		}
	}
}
