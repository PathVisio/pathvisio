package org.pathvisio.visualization.colorset;

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
			new ColorSetDlg(new ColorSet(csMgr.getNewName()), null, null);
		}
	}
	
	class ColorSetRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			l.setText(((ColorSet)value).getName());
			return l;
		}
	}
}
