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
package org.pathvisio.desktop.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.bridgedb.IDMapperException;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.ImageExporter;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.data.DataException;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.visualization.VisualizationManager;

/**
 * A Pathway exporter for Bitmap formats,
 * based on the javax.imageio library,
 * that also includes data visualizations in the exported image.
 */
public class RasterImageWithDataExporter extends ImageExporter
{
	private final VisualizationManager visualizationManager;
	protected boolean dataVisible = true; // true by default
	private final GexManager gexManager;

	/**
	 * Use a buffered image for exporting
	 *
	 * @param type must be one of javax.imageio.ImageIO.getWriterFormatNames().
	 * 	e.g. "gif", "png" or "jpeg". Throws an IllegalArgumentException otherwise
	 */
	public RasterImageWithDataExporter(String type, GexManager gexManager, VisualizationManager mgr)
	{
		super(type);
		List<String> formatNames = Arrays.asList (ImageIO.getWriterFormatNames());
		if (!formatNames.contains (type))
			throw new IllegalArgumentException ("Unkown Image type " + type);
		visualizationManager = mgr;
		this.gexManager = gexManager;
	}

	public void doExport(File file, Pathway pathway) throws ConverterException
	{
		VPathway vPathway = new VPathway(null);
		vPathway.fromModel(pathway);

		// if data visualization is enabled, link this VPathway up to the visualization manager.
		if (dataVisible)
		{
			vPathway.addVPathwayListener(visualizationManager);
			try
			{
				if (gexManager.getCachedData() != null)
					gexManager.getCachedData().syncSeed(pathway.getDataNodeXrefs());
			}
			catch (IDMapperException ex)
			{
				Logger.log.error ("Could not get data", ex);
			}
			catch (DataException ex)
			{
				Logger.log.error ("Could not get data", ex);
			}
		}

		BufferedImage image = new BufferedImage(vPathway.getVWidth(), vPathway.getVHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		vPathway.draw(g2);
		g2.dispose();

		try
		{
			ImageIO.write(image, getType(), file);
		}
		catch (IOException ex)
		{
			throw new ConverterException(ex);
		}
		finally
		{
			vPathway.dispose();
		}
	}

}
