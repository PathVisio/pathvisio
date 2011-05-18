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
package org.pathvisio.core.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.pathvisio.core.view.VPathway;

/**
 * A Pathway exporter for Bitmap formats,
 * based on the javax.imageio library
 */
public class RasterImageExporter extends ImageExporter
{
	/**
	 * Use a buffered image for exporting
	 *
	 * @param type must be one of javax.imageio.ImageIO.getWriterFormatNames(),
	 * 	e.g. "gif", "png" or "jpeg". Throws an IllegalArgumentException otherwise
	 */
	public RasterImageExporter(String type)
	{
		super(type);
		List<String> formatNames = Arrays.asList (ImageIO.getWriterFormatNames());
		if (!formatNames.contains (type))
			throw new IllegalArgumentException ("Unkown Image type " + type);
	}

	public void doExport(File file, Pathway pathway) throws ConverterException
	{
		VPathway vPathway = new VPathway(null);
		vPathway.fromModel(pathway);
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
