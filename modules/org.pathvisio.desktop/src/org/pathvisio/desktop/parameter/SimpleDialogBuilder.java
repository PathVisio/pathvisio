/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.desktop.parameter;

import java.io.File;

import javax.swing.JFrame;

import org.bridgedb.gui.ParameterPanel;
import org.bridgedb.gui.SimpleParameterModel;
import org.pathvisio.gui.dialogs.OkCancelDialog;

/**
 * Utility class for building simple input dialogs.
 * <p>
 * You can pass an array of Objects to the constructor, and depending on the type of those Objects,
 * either a JTextField, a JTextField with Browse button or a JCheckBox is added to the dialog.
 * <p>
 * To use SimpleDialogBuilder, override the okPressed() method and call getFile(i),
 * getString(i) or getBoolean(i) to get at the resulting values.
 */
public class SimpleDialogBuilder extends OkCancelDialog
{	
	private final ParameterPanel panel;
	private final SimpleParameterModel model;
	
	public SimpleDialogBuilder(JFrame frame, String title, Object[][] data)
	{
		super (frame, title, frame, true);
		model = new SimpleParameterModel(data);
		panel = new ParameterPanel(model);
		setDialogComponent(panel);
		pack();
	}
	
	public File getFile(int i)
	{
		return model.getFile(i);
	}
	
	public String getString (int i)
	{
		return model.getString(i);
	}
	
	public boolean getBoolean (int i)
	{
		return model.getBoolean(i);
	}
}
