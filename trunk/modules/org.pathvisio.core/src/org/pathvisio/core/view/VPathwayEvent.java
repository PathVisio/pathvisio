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
package org.pathvisio.core.view;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * event sent by VPathway upon modification of one or more VPathwayElements.
 */
public class VPathwayEvent extends EventObject {

	/** Possible event types */
	public static enum VPathwayEventType
	{
		ELEMENT_ADDED,
		EDIT_MODE_ON,
		EDIT_MODE_OFF,
		MODEL_LOADED,
		ELEMENT_DOUBLE_CLICKED,
		ELEMENT_DRAWN,
		ELEMENT_CLICKED_UP,
		ELEMENT_CLICKED_DOWN,
		ELEMENT_HOVER,
		HREF_ACTIVATED;
	}

	private VPathwayEventType type;
	private List<VPathwayElement> affectedElements;
	private Graphics2D g2d;
	private MouseEvent mouseEvent;

	public VPathwayEvent(VPathway source, VPathwayEventType type) {
		super(source);
		this.type = type;
	}

	public VPathwayEvent(VPathway source, List<VPathwayElement> affectedElements, VPathwayEventType type) {
		this(source, type);
		this.affectedElements = affectedElements;
	}

	public VPathwayEvent(VPathway source, VPathwayElement affectedElement, VPathwayEventType type) {
		this(source, type);
		List<VPathwayElement> afe = new ArrayList<VPathwayElement>();
		afe.add(affectedElement);
		this.affectedElements = afe;
	}

	public VPathwayEvent(VPathway source, VPathwayElement affectedElement, Graphics2D g2d, VPathwayEventType type) {
		this(source, affectedElement, type);
		this.g2d = g2d;
	}

	public VPathwayEvent(VPathway source, VPathwayElement affectedElement, MouseEvent e, VPathwayEventType type) {
		this(source, affectedElement, type);
		mouseEvent = e;
	}

	public VPathwayEvent(VPathway source, List<VPathwayElement> affectedElements, MouseEvent e, VPathwayEventType type) {
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

	public VPathwayEventType getType() {
		return type;
	}

	public Graphics2D getGraphics2D() {
		return g2d;
	}

	public VPathway getVPathway() {
		return (VPathway)getSource();
	}
}
