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
package org.pathvisio.gui.swing.panels;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.pathvisio.core.model.PathwayElement;

public abstract class PathwayElementPanel extends JPanel {
	private PathwayElement input;
	JTabbedPane dialogPane;
	boolean readonly;

	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
	}

	protected PathwayElement getInput() {
		return input;
	}

	public void setInput(PathwayElement e) {
		input = e;
		refresh();
	}

	public abstract void refresh();

	public final void setChildrenEnabled(JComponent c, boolean enabled) {
		for(Component child : c.getComponents()) {
			child.setEnabled(enabled);
			if(child instanceof JComponent) {
				setChildrenEnabled((JComponent)child, enabled);
			}
		}
	}
}
