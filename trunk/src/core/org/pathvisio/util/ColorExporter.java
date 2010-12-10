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
package org.pathvisio.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.VPathwayWrapperBase;
import org.pathvisio.view.VPathwayEvent.VPathwayEventType;

/**
 * Utility that takes a set of graphId/Color pairs and exports a pathway
 * image after coloring the objects with the specified graphIds.
 * @author thomas
 */
public class ColorExporter implements VPathwayListener {
	Map<PathwayElement, List<Color>> colors;
	VPathway vPathway;

	public ColorExporter(Pathway pathway, Map<PathwayElement, List<Color>> colors) {
		this.colors = colors;
		vPathway = new VPathway(new VPathwayWrapperBase());
		vPathway.fromModel(pathway);
	}

	public void dispose() {
		vPathway.dispose();
	}

	public void export(BatikImageExporter exporter, File outputFile) throws ConverterException {
		vPathway.addVPathwayListener(this);
		doHighlight();
		exporter.doExport(outputFile, vPathway);
	}

	public void vPathwayEvent(VPathwayEvent e) {
		if(e.getType() == VPathwayEventType.ELEMENT_DRAWN) {
			VPathwayElement vpwe = e.getAffectedElement();
			if(vpwe instanceof Graphics) {
				PathwayElement pwe = ((Graphics)vpwe).getPathwayElement();
				List<Color> elmColors = colors.get(pwe);
				if(elmColors != null && elmColors.size() > 0) {
					Logger.log.info("Coloring " + pwe + " with " + elmColors);
					switch(pwe.getObjectType()) {
					case DATANODE:
						doColor(e.getGraphics2D(), (Graphics)vpwe, elmColors);
						drawLabel(e.getGraphics2D(), (Graphics)vpwe);
						break;
					case GROUP:
						doColor(e.getGraphics2D(), (Graphics)vpwe, elmColors);
						break;
					}
				}
			}
		}
	}

	private void drawLabel(Graphics2D g, Graphics pwe) {
		Graphics2D g2d = (Graphics2D)g.create();
		Rectangle2D area = pwe.getVBounds();
		g2d.setClip(area);
		g2d.setColor(Color.black);

		String label = pwe.getPathwayElement().getTextLabel();
		if(label != null && !"".equals(label)) {
			TextLayout tl = new TextLayout(label, g2d.getFont(), g2d.getFontRenderContext());
			Rectangle2D tb = tl.getBounds();

			tl.draw(g2d, 	(int)area.getX() + (int)(area.getWidth() / 2) - (int)(tb.getWidth() / 2),
					(int)area.getY() + (int)(area.getHeight() / 2) + (int)(tb.getHeight() / 2));
		}
	}

	private void doColor(Graphics2D g, Graphics vpe, List<Color> colors) {
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setClip(vpe.getVBounds());

		Rectangle area = vpe.getVBounds().getBounds();

		int nr = colors.size();
		int left = area.width % nr; //Space left after dividing, give to last rectangle
		int w = area.width / nr;
		for(int i = 0; i < nr; i++) {
			g2d.setColor(colors.get(i));
			Rectangle r = new Rectangle(
					area.x + w * i,
					area.y,
					w + ((i == nr - 1) ? left : 0), area.height);
			g2d.fill(r);
		}
		g2d.setColor(vpe.getPathwayElement().getColor());
		g2d.drawRect(area.x, area.y, area.width - 1, area.height - 1);
	}

	/**
	 * Highlight all object but DataNodes and Groups. Only the first color
	 * from the hashmap will be used.
	 */
	private void doHighlight() {
		for(VPathwayElement vpe : vPathway.getDrawingObjects()) {
			if(vpe instanceof Graphics) {
				PathwayElement pwe = ((Graphics)vpe).getPathwayElement();
				List<Color> elmColors = colors.get(pwe);
				if(elmColors != null && elmColors.size() > 0) {
					ObjectType ot = pwe.getObjectType();
					if(ot != ObjectType.DATANODE && ot != ObjectType.GROUP) {
						vpe.highlight(elmColors.get(0));
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		PreferenceManager.init();

		if(args.length < 2) {
			printHelp();
			System.exit(-1);
		}

		try {
			String inStr = args[0];
			String outStr = args[1];

			//Enable MiM support (for export to graphics formats)
			MIMShapes.registerShapes();

			Logger.log.setStream (System.err);
			Logger.log.setLogLevel (false, false, true, true, true, true);

			File inputFile = new File(inStr);
			File outputFile = new File(outStr);
			Pathway pathway = new Pathway();
			pathway.readFromXml(inputFile, true);

			//Parse commandline arguments
			Map<PathwayElement, List<Color>> colors = new HashMap<PathwayElement, List<Color>>();

			for(int i = 2; i < args.length - 1; i++) {
				if("-c".equals(args[i])) {
					PathwayElement pwe = pathway.getElementById(args[++i]);
					String colorStr = args[++i];
					if(pwe != null) {
						List<Color> pweColors = colors.get(pwe);
						if(pweColors == null) colors.put(pwe, pweColors = new ArrayList<Color>());
						int cv = Integer.parseInt(colorStr, 16);
						pweColors.add(new Color(cv));
					}
				}
			}

			BatikImageExporter exporter = null;
			if(outStr.endsWith(ImageExporter.TYPE_PNG)) {
				exporter = new BatikImageExporter(ImageExporter.TYPE_PNG);
			} else if(outStr.endsWith(ImageExporter.TYPE_PDF)) {
				exporter = new BatikImageExporter(ImageExporter.TYPE_PDF);
			} else if(outStr.endsWith(ImageExporter.TYPE_TIFF)) {
				exporter = new BatikImageExporter(ImageExporter.TYPE_TIFF);
			} else {
				exporter = new BatikImageExporter(ImageExporter.TYPE_SVG);
			}
			ColorExporter colorExp = new ColorExporter(pathway, colors);
			colorExp.export(exporter, outputFile);
			colorExp.dispose();

		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-2);
			printHelp();
		}
	}

	static void printHelp() {
		System.err.println(
				"Usage:\n" +
				"\tjava org.pathvisio.data.ColorExporter <inputFile> <outputFile> [-c graphId color]\n" +
				"Parameters:\n" +
				"\t-c\tA string containing the graphId of the object to color, followed " +
				"by the color to be used for that object (hexadecimal, e.g. FF0000 for red)\n" +
				"The export format is determined by the output file extension and can be one of: " +
				"svg, pdf, png, tiff"
		);
	}
}
