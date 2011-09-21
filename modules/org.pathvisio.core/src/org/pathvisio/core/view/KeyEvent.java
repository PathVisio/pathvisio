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

/** generic keyboard input event for VPathway, toolkit (swing / SWT) independent */
public class KeyEvent extends InputEvent {

	//Types
	public static final int KEY_PRESSED = 0;
	public static final int KEY_RELEASED = 1;

	//Keys
	public static final int MIN_VALUE = Character.MAX_VALUE;
	public static final int NONE = 0;
	public static final int CTRL = MIN_VALUE + 1;
	public static final int ALT = MIN_VALUE + 2;
	public static final int SHIFT = MIN_VALUE + 3;
	public static final int DEL = MIN_VALUE + 4;
	public static final int INSERT = MIN_VALUE + 5;

	private int keyCode;
	private int type;

	public KeyEvent(Object source, int keyCode, int type, int modifier) {
		super(source, modifier);
		this.keyCode = keyCode;
		this.type = type;
	}

	public KeyEvent(Object source, char keyCode, int type, int modifier) {
		this(source, Character.getNumericValue(keyCode), type, modifier);
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getType() {
		return type;
	}

	public boolean isKey(char c) {
		System.out.println("ask: " + (int)c);
		System.out.println("have: " + keyCode);
		return (int)c == keyCode;
	}

	public boolean isKey(int i) {
		return keyCode == i;
	}
}
