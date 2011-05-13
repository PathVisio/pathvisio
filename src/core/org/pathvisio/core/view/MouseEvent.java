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
package org.pathvisio.core.view;

import java.awt.Point;

/**
 * Represents a mouse event in a way that is independent of toolkit (swing or SWT)
 * TODO: no longer needed. Just use swing event
 */
public abstract class MouseEvent extends InputEvent {

	//Buttons
	public static final int BUTTON_NONE = -1;
	public static final int BUTTON1 = 1;
	public static final int BUTTON2 = 2;
	public static final int BUTTON3 = 3;

	//Types
	public static final int MOUSE_DOWN = 10;
	public static final int MOUSE_UP = 11;
	public static final int MOUSE_MOVE = 12;
	public static final int MOUSE_EXIT = 13;
	public static final int MOUSE_ENTER = 14;
	public static final int MOUSE_HOVER = 15;
	public static final int MOUSE_CLICK = 16;

	private int type;
	private int button;
	private int clickCount;
	private int x; //x relative to source
	private int y; //y relative to source
	private boolean isPopupTrigger;

	public MouseEvent(Object source, int type, int button, int x, int y, int clickCount, int modifier, boolean isPopupTrigger) {
		super(source, modifier);
		this.x = x;
		this.y = y;
		this.type = type;
		this.button = button;
		this.clickCount = clickCount;
		this.isPopupTrigger = isPopupTrigger;
	}

	//public abstract Point getLocationOnScreen();

	public int getButton() {
		return button;
	}

	public int getClickCount() {
		return clickCount;
	}

	public int getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Point getLocation() {
		return new Point(x, y);
	}

	public boolean isPopupTrigger() {
		return isPopupTrigger;
	}

	/*
	public int getXOnScreen() {
		return getLocationOnScreen().x;
	}

	public int getYOnScreen() {
		return getLocationOnScreen().y;
	}
	*/
}
