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
package org.wikipathways.gpmldiff;

import java.awt.event.MouseWheelEvent;

import javax.swing.JScrollPane;

import org.pathvisio.gui.view.VPathwaySwing;

/**
 * Same as VPathwaySwing, but overrides a few methods to allow for synchronized
 * scrolling and zooming.
 */
public class VPathwayDiffViewer extends VPathwaySwing
{
	final GpmlDiffWindow parent;
	
	public VPathwayDiffViewer(JScrollPane container, GpmlDiffWindow parent)
	{
		super(container);
		this.parent = parent;
	}

	public void resized()
	{
		parent.updatePreferredSize();
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		parent.mouseWheelMoved(e.getWheelRotation());
	}


}
