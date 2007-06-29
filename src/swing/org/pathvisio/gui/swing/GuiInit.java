package org.pathvisio.gui.swing;

import org.pathvisio.Engine;
import org.pathvisio.model.GpmlFormat;

public class GuiInit {
	public static void init() {
		initImporters();
		initExporters();
	}
	
	private static void initImporters() {
		Engine.addPathwayImporter(new GpmlFormat());
	}
	
	private static void initExporters() {
		Engine.addPathwayExporter(new GpmlFormat());
	}
}
