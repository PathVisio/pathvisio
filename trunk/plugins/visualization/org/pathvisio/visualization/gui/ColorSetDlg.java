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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JScrollPane;

import org.pathvisio.gex.GexManager;
import org.pathvisio.gui.swing.dialogs.OkCancelDialog;
import org.pathvisio.visualization.colorset.ColorSet;

/**
 * Use this class to pop up a colorset dialog
 * for editing a color set
 * If you want to create a new color set,
 * you have to do that before popping up this dialog
 */
public class ColorSetDlg extends OkCancelDialog
{
	ColorSet cs;

	public ColorSetDlg (ColorSet cs, Frame frame, Component locationComp, GexManager gexManager)
	{
		super (frame, "Edit Color Set", locationComp, true, true);
		this.cs = cs;
		setDialogComponent(new JScrollPane(new ColorSetPanel(cs, gexManager)));
		setMinimumSize(new Dimension(200, 300));
		pack();
	}
}
