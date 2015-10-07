// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.gui.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.core.Engine;
import org.pathvisio.core.Globals;
import org.pathvisio.core.util.Resources;
import org.pathvisio.gui.SwingEngine;

/**
 * Creates and displays the About dialog,
 * showing some general information about the application.
 */
public class AboutDlg
{
	private static final URL IMG_ABOUT_LOGO = Resources.getResourceURL("new-logo-small.png");

	private SwingEngine swingEngine;

	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI()
	{
		final JFrame aboutDlg = new JFrame();
		aboutDlg.setBackground(Color.white);
		FormLayout layout = new FormLayout(
				" 4dlu, left:230dlu:grow, 4dlu",
				"4dlu, pref, 4dlu, 240dlu:grow, 4dlu, pref, 4dlu");

		JEditorPane label = new JEditorPane();
		label.setBackground(Color.white);
		label.setContentType("text/html");
		label.setEditable(false);
		label.setText(
				swingEngine.getEngine().getApplicationName() + "<br>Revision: " + Engine.getRevision() +
				"<br><br><hr><br>" + 
				"<html><b>Core developers</b><br>" +
				"Martina Kutmon, Anwesha Bohler, Jonathan Melius, Nuno Nunes, Thomas Kelder, " +
				"Martijn van Iersel, Kristina Hanspers, Alex Pico<br><br><hr><br>" +
				"<b>Contributors</b><br>" +
				"Adem Bilicna, Augustin Luna, Bing Lui, " + 
				"Christ Leemans, Eric Creussen, Erik Pelgrin, " + 
				"Esterh Neuteboom, Ferry Jagers, Hakim Achterberg, " + 
				"Harm Nijveen, Irene Kaashoek, Justin Elser, " + 
				"Kumar Chanden, Margot Sunshine, Mark Woon, " + 
				"Margiet Palm, Pim Moeskops, Praveen Kumar, " +
				"Rene Besseling, Rianne Fijten, Sjoerd Crijns, " + 
				"Sravanthi Sinha, Stefan van Helden<br><br><hr><br>" +
				"<b>Visit our website</b><br>" +
				"<a href=\"http://www.pathvisio.org\">http://www.pathvisio.org</a>" + 
				"</html>");
		label.addHyperlinkListener(swingEngine);
		JLabel iconLbl = new JLabel(new ImageIcon (IMG_ABOUT_LOGO));

		CellConstraints cc = new CellConstraints();

		JPanel dialogBox = new JPanel();
		dialogBox.setBackground(Color.white);
		dialogBox.setLayout (layout);
		dialogBox.add (iconLbl, cc.xy(2,2));
		dialogBox.add (label, cc.xy(2,4));

		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				aboutDlg.setVisible (false);
				aboutDlg.dispose();
			}
		});

		dialogBox.add (btnOk, cc.xyw (2, 6, 1, "center, top"));

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
