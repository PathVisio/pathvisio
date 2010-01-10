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
package org.pathvisio.gui.swing.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.Globals;
import org.pathvisio.Revision;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.util.Resources;

/**
 * Creates and displays the About dialog,
 * showing some general information about the application.
 */
public class AboutDlg
{
	private static final URL IMG_ABOUT_LOGO = Resources.getResourceURL("logo.jpg");

	private SwingEngine swingEngine;

	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI()
	{
		final JFrame aboutDlg = new JFrame();

		FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");

		JLabel versionLabel = new JLabel (swingEngine.getEngine().getApplicationName());
		JLabel revisionLabel = new JLabel (Revision.REVISION);
		JLabel label = new JLabel();
		label.setText(
				"<html><h3>Core developers</h3>\n" +
				"<p>Thomas Kelder, Martijn van Iersel<br>\n" +
				"Kristina Hanspers, Alex Pico<br>\n" +
				"<h3>Contributors</h3>\n" +
				"<p>R.M.H. Besseling, S.P.M.Crijns, I. Kaashoek<br>\n" +
				"M.M. Palm, E.D. Pelgrim, E. Neuteboom,<br>\n" +
				"E.J. Creusen, P. Moeskops, Adem Bilican,<br>\n" +
				"Margot Sunshine, Mark Woon<br>\n" +
				"<h3>Visit our website</h3>" +
				"<p>http://www.pathvisio.org" +
				"</html>");
		JLabel iconLbl = new JLabel(new ImageIcon (IMG_ABOUT_LOGO));

		CellConstraints cc = new CellConstraints();

		JPanel dialogBox = new JPanel();
		dialogBox.setLayout (layout);
		dialogBox .add (iconLbl, cc.xy(2,2));
		dialogBox .add (label, cc.xy(4,2));

		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				aboutDlg.setVisible (false);
				aboutDlg.dispose();
			}
		});

		dialogBox.add (versionLabel, cc.xy(2, 4));
		dialogBox.add (revisionLabel, cc.xy(4, 4));
		dialogBox.add (btnOk, cc.xyw (2, 6, 3, "center, top"));

		aboutDlg.setResizable(false);
		aboutDlg.setTitle("About " + Globals.APPLICATION_NAME);
		aboutDlg.add (dialogBox);
		aboutDlg.pack();
		aboutDlg.setLocationRelativeTo(swingEngine.getFrame());
		aboutDlg.setVisible(true);
	}

	public AboutDlg(SwingEngine swingEngine)
	{
		this.swingEngine = swingEngine;


	}
}
