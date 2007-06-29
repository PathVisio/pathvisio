package org.pathvisio.gui.swt.awt;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.eclipse.swt.widgets.Composite;

public class VPathwaySwingComposite extends EmbeddedSwingComposite {
	JScrollPane scrollPane;
	
	public VPathwaySwingComposite(Composite parent, int style) {
		super(parent, style);
		populate();
	}
	
	protected JComponent createSwingComponent() {
		scrollPane = new JScrollPane();
		return scrollPane;
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
}
