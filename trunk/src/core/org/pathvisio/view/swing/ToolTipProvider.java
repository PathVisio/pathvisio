package org.pathvisio.view.swing;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JToolTip;

import org.pathvisio.view.VPathwayElement;

public interface ToolTipProvider {
	public Component createToolTipComponent(JToolTip parent, Collection<VPathwayElement> elements);
}
