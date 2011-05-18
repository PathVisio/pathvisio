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
package org.pathvisio.desktop.util;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

/** 
 * Utility class for attaching a "Browse" button to a text field 
 */
public class BrowseButtonActionListener implements ActionListener
{
	private final JTextField txt;
	private final Container frame;
	private final int fileSelectionMode;
	
	/**
	 * @param txt: JTextField, will read default file from this field, and will write selected file to this field.
	 * @param fileSelectionMode: one of JFileChooser.DIRECTORIES_ONLY, JFileChooser.FILES_ONLY or JFileChooser.FILES_AND_DIRECTORIES
	 */
	public BrowseButtonActionListener (JTextField txt, Container frame, int fileSelectionMode)
	{
		this.txt = txt;
		this.frame = frame;
		this.fileSelectionMode = fileSelectionMode;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(fileSelectionMode);
		jfc.setCurrentDirectory(new File(txt.getText()));
		if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			txt.setText("" + jfc.getSelectedFile());
	}
	
}