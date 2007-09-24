package org.pathvisio.gui.wikipathways;

import java.awt.BorderLayout;
import java.awt.Container;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.panels.LitReferencePanel;
import org.pathvisio.model.Pathway;

public class BibliographyApplet extends PathwayPageApplet {
	protected void createGui() {
		Container content = getContentPane();
		
		Pathway pathway = Engine.getCurrent().getActivePathway();
		LitReferencePanel refPanel = new LitReferencePanel();
		refPanel.setInput(pathway.getMappInfo());
		content.add(refPanel, BorderLayout.CENTER);
	}
}
