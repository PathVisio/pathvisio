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
package org.pathvisio.desktop.plugin;

import java.io.File;
import java.util.List;

import javax.swing.JFrame;

import org.pathvisio.desktop.PvDesktop;

public interface IPluginManager {

	public void installLocalPlugins(List<File> plugins, List<File> dependencies);
	public void init(PvDesktop desktop);
	public void showGui(JFrame parent);
}
