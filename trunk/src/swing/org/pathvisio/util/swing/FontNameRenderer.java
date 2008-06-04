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
			Font defaultFont = (Font)UIManager.get("Label.font");
			Font newFont = new Font(
					fontName, 
					defaultFont.getStyle(), 
					defaultFont.getSize()
			);
			setFont(newFont);
			setText(fontName);
		}
		return this;
	}
}
