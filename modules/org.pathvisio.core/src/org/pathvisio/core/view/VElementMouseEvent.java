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

import java.util.EventObject;

/**
 * An event for mouse events that apply to a single VPathwayElement
 * @author thomas
 */
public class VElementMouseEvent extends EventObject {
	public static final int TYPE_MOUSEENTER = 0;
	public static final int TYPE_MOUSEEXIT = 1;
	
	// are used for change of cursor when the mouse is over a label
	// with href and the ctrl button is pressed
	public static final int TYPE_MOUSE_SHOWHAND = 2;
	public static final int TYPE_MOUSE_NOTSHOWHAND = 3;

	public int type;
	public VPathwayElement element;
	public MouseEvent mouseEvent;

	public VElementMouseEvent(VPathway source, int type, VPathwayElement element, MouseEvent mouseEvent) {
		super(source);
		this.type = type;
		this.element = element;
		this.mouseEvent = mouseEvent;
	}
	
	public VElementMouseEvent(VPathway source, int type, VPathwayElement element) {
		super(source);
		this.type = type;
		this.element = element;
	}

	public int getType() {
		return type;
	}

	public VPathwayElement getElement() {
		return element;
	}

	public MouseEvent getMouseEvent() {
		return mouseEvent;
	}
}
