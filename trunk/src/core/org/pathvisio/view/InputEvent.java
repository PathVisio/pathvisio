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

import java.util.EventObject;

/**
 * base class for MouseEvent and KeyEvent: represents an input event
 * independent of toolkit (swing or SWT)
 * TODO: no longer needed. Just use swing event
 */
public class InputEvent extends EventObject {

	public static final int M_SHIFT = java.awt.event.InputEvent.SHIFT_DOWN_MASK;
	public static final int M_CTRL = java.awt.event.InputEvent.CTRL_DOWN_MASK;
	public static final int M_ALT = java.awt.event.InputEvent.ALT_DOWN_MASK;
	public static final int M_META = java.awt.event.InputEvent.META_DOWN_MASK;

	private int modifier;

	public InputEvent(Object source, int modifier) {
		super(source);
		this.modifier = modifier;
	}

	/**
	 * returns true if the given key is pressed.
	 * Nb, this is a mask, so you can use key | key to check multiple keys together */
	public boolean isKeyDown(int key) {
		return (modifier & key) != 0;
	}
}
