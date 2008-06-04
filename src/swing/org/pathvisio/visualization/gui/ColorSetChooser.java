package org.pathvisio.visualization.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.pathvisio.Engine;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mammothsoftware.frwk.ddb.DropDownButton;

/**
 * A panel containing a colorset combo + button to add/remove/modify colorsets.
 * @author thomas
 *
 */
public class ColorSetChooser extends JPanel implements ActionListener {
	static final String ACTION_NEW = "New";
	static final String ACTION_REMOVE = "Remove";
	static final String ACTION_MODIFY = "Modify";
	
	ColorSetManager csMgr;
	ColorSetCombo colorSetCombo;

	public ColorSetChooser(ColorSetManager csMgr) {
		this.csMgr = csMgr;

		setLayout(new FormLayout(
				"fill:pref:grow, 4dlu, pref", "pref"
		));

		colorSetCombo = new ColorSetCombo(csMgr);

		DropDownButton csButton = new DropDownButton(new ImageIcon(
				Engine.getCurrent().getResourceURL("edit.gif"))
		);
		JMenuItem m_new = new JMenuItem(ACTION_NEW);
		JMenuItem m_remove = new JMenuItem(ACTION_REMOVE);
		JMenuItem m_rename = new JMenuItem(ACTION_MODIFY);
		m_new.setActionCommand(ACTION_NEW);
		m_remove.setActionCommand(ACTION_REMOVE);
		m_rename.setActionCommand(ACTION_MODIFY);
		m_new.addActionListener(this);
		m_remove.addActionListener(this);
		m_rename.addActionListener(this);
		csButton.addComponent(m_new);
		csButton.addComponent(m_remove);
		csButton.addComponent(m_rename);
		
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
			ColorSetDlg dlg = new ColorSetDlg(cs, null, this);
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
				ColorSetDlg dlg = new ColorSetDlg(cs, null, this);
				dlg.setVisible(true);
			}
			colorSetCombo.refresh();
			colorSetCombo.setSelectedItem(cs);
		}
	}
}
