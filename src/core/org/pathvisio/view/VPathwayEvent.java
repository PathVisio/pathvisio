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

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * event sent by VPathway upon modification of one or more VPathwayElements.
 */
public class VPathwayEvent extends EventObject {

	public static final int ELEMENT_ADDED = 0;
	public static final int EDIT_MODE_ON = 1;
	public static final int EDIT_MODE_OFF = 2;
	public static final int MODEL_LOADED = 3;
	public static final int ELEMENT_DOUBLE_CLICKED = 4;
	public static final int ELEMENT_DRAWN = 5;
	public static final int ELEMENT_CLICKED_UP = 6;
	public static final int ELEMENT_CLICKED_DOWN = 7;
	public static final int ELEMENT_HOVER = 8;

	int type;
	List<VPathwayElement> affectedElements;
	Graphics2D g2d;
	MouseEvent mouseEvent;

	public VPathwayEvent(VPathway source, int type) {
		super(source);
		this.type = type;
	}

	public VPathwayEvent(VPathway source, List<VPathwayElement> affectedElements, int type) {
		this(source, type);
		this.affectedElements = affectedElements;
	}

	public VPathwayEvent(VPathway source, VPathwayElement affectedElement, int type) {
		this(source, type);
		List<VPathwayElement> afe = new ArrayList<VPathwayElement>();
		afe.add(affectedElement);
		this.affectedElements = afe;
	}

	public VPathwayEvent(VPathway source, VPathwayElement affectedElement, Graphics2D g2d, int type) {
		this(source, affectedElement, type);
		this.g2d = g2d;
	}

	public VPathwayEvent(VPathway source, VPathwayElement affectedElement, MouseEvent e, int type) {
		this(source, affectedElement, type);
		mouseEvent = e;
	}

	public VPathwayEvent(VPathway source, List<VPathwayElement> affectedElements, MouseEvent e, int type) {
		this(source, affectedElements, type);
		mouseEvent = e;
	}

	public MouseEvent getMouseEvent() {
		return mouseEvent;
	}

	public VPathwayElement getAffectedElement() {
		return affectedElements.get(0);
	}

	public List<VPathwayElement> getAffectedElements() {
		return affectedElements;
	}

	public int getType() {
		return type;
	}

	public Graphics2D getGraphics2D() {
		return g2d;
	}

	public VPathway getVPathway() {
		return (VPathway)getSource();
	}
}
