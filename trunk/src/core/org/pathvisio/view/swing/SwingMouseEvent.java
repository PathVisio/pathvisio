// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.view.swing;

import java.awt.event.MouseEvent;

public class SwingMouseEvent extends org.pathvisio.view.MouseEvent {
	MouseEvent awtEvent;
	
	public SwingMouseEvent(MouseEvent e) {
		super(e.getSource(), convertType(e), e.getButton(), 
				e.getX(), e.getY(), e.getClickCount(), e.getModifiers());
		awtEvent = e;
	}

	protected static int convertType(MouseEvent e) {
		if(e.isPopupTrigger()) return MOUSE_HOVER;

		switch(e.getID()) {
		case MouseEvent.MOUSE_ENTERED:
			return org.pathvisio.view.MouseEvent.MOUSE_ENTER;
		case MouseEvent.MOUSE_EXITED:
			return org.pathvisio.view.MouseEvent.MOUSE_EXIT;
		case MouseEvent.MOUSE_MOVED:
		case MouseEvent.MOUSE_DRAGGED:
			return org.pathvisio.view.MouseEvent.MOUSE_MOVE;
		case MouseEvent.MOUSE_PRESSED:
			return org.pathvisio.view.MouseEvent.MOUSE_DOWN;
		case MouseEvent.MOUSE_RELEASED:
			return org.pathvisio.view.MouseEvent.MOUSE_UP;
		case MouseEvent.MOUSE_CLICKED:
			return org.pathvisio.view.MouseEvent.MOUSE_CLICK;
		default:
			throw new IllegalArgumentException("Mouse event type not supported");
		}
	}
}
