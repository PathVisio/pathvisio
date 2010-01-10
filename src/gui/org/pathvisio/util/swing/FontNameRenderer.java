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

package org.pathvisio.util.swing;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 * List cell renderer that takes in either a font name (String) or a Font object
 * and renders the list items with that font. In the case of a Font, the renderer uses
 * excactly the given font, in case of a font name, the renderer creates a new font with
 * the default size and style.
 * @author thomas
 */
public class FontNameRenderer extends DefaultListCellRenderer{
	public FontNameRenderer() {
		super();
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		if(value instanceof Font) {
			Font valueFont = (Font)value;
			setFont(valueFont);
			setText(valueFont.getFontName());
		} else if (value instanceof String) {
			String fontName = (String)value;
			setFont(buildFont(fontName));
			setText(fontName);
		}
		return this;
	}


	public static Font buildFont(String fontName) {

		Font defaultFont = (Font)UIManager.get("Label.font");
		return new Font(fontName, defaultFont.getStyle(), defaultFont.getSize());
	}
}
