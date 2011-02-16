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
package org.pathvisio.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

/**
 * Wrapper for VPathway that handles toolkit (swing / SWT) dependent differences.
 */
public abstract interface VPathwayWrapper
{
	public void redraw();
	public void redraw(Rectangle r);
	public Rectangle getViewRect();
	
	/** signal to indicate that the pathway changed size */
	public void resized();

	public VPathway createVPathway();

	public void registerKeyboardAction(KeyStroke k, Action a);
	public void copyToClipboard(Pathway source, List<PathwayElement> copyElements);
	public void pasteFromClipboard();
	public void positionPasteFromClipboard(Point cursorPosition);

	/** make sure r is visible */
	public void scrollTo(Rectangle r);
	public void scrollCenterTo(int x, int y);	

	/** called by VPathway.dispose() */
	public void dispose();
}
