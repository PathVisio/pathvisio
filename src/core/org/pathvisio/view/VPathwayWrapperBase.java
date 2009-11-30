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
import java.awt.Rectangle;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;

/**
 * Nearly empty base class for VPathwayWrapper, the bare minimum.
 * Can be used when no interactive GUI is needed.
 */
public class VPathwayWrapperBase implements VPathwayWrapper {
	Dimension vSize = new Dimension();

	public void copyToClipboard(Pathway source, List<PathwayElement> result) {

	}

	public VPathway createVPathway() {
		return new VPathway(this);
	}

	public Rectangle getVBounds() {
		return new Rectangle(0, 0, vSize.width, vSize.height);
	}

	public Dimension getVSize() {
		return vSize;
	}

	public Dimension getViewportSize() {
		return vSize;
	}

	public void redraw() {

	}

	public void redraw(Rectangle r) {

	}

	public void registerKeyboardAction(KeyStroke k, Action a) {

	}

	public void setVSize(Dimension size) {
		vSize = size;
	}

	public void setVSize(int w, int h) {
		vSize.width = w;
		vSize.height = h;
	}

	public void pasteFromClipboard() {

	}

	public void scrollTo(Rectangle r) {
		// TODO Auto-generated method stub

	}

	public void dispose() {}

}
