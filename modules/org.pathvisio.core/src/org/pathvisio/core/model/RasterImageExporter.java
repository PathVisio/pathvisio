/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2024 PathVisio
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
	
	

	private static final double MAX_ZOOM = 500;
	
	private static final int DEFAULT_RESOLUTION = 600;
	private static final List<Integer> RESOLUTION_VALUES = Arrays.asList(
			new Integer[] { DEFAULT_RESOLUTION, 100, 150, 300, 600 });

	public int widthInPixels;
	public double widthInInches;
	
	public int heightInPixels;
	public double heightInInches;

	public int initialWPixel, initialHPixel; 
	
	public double zoom;
	
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
		try
		{
			BufferedImage image = exportAsImage(pathway);
			ImageIO.write(image, getType(), file);
		}
		catch (IOException ex)
		{
			throw new ConverterException(ex);
		}
	}
	
	
	@Override
	public void doExport(File file, Pathway pathway, int zoom)
			throws ConverterException {
		// TODO Auto-generated method stub
		try
		{
			
			
			BufferedImage image = exportAsImage(pathway, zoom);
			ImageIO.write(image, getType(), file);
		}
		catch (IOException ex)
		{
			throw new ConverterException(ex);
		}
	}
	
	
	public BufferedImage exportAsImage(Pathway pathway)
	{
		VPathway vPathway = new VPathway(null);
		try
		{
			vPathway.fromModel(pathway);
			BufferedImage image = new BufferedImage(vPathway.getVWidth(), vPathway.getVHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = image.createGraphics();
			vPathway.draw(g2);
			g2.dispose();
			return image;
		}
		finally
		{
			vPathway.dispose();
		}
	}

	public BufferedImage exportAsImage(Pathway pathway, int zoom)
	{
		VPathway vPathway = new VPathway(null);
		try
		{		
			vPathway.fromModel(pathway);
			
			initialWPixel = vPathway.getVWidth();
			initialHPixel = vPathway.getVHeight();
			
			// update height
			heightInPixels = (int) (( zoom / 100 ) * initialHPixel);
			// update width
			widthInPixels = (int) (( zoom / 100 ) * initialWPixel);

			final double scale = zoom / 100.0;
			final BufferedImage image = new BufferedImage(widthInPixels, heightInPixels, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.scale(scale, scale);
			vPathway.draw(g2);
			g2.dispose();
			return image;
		}
		finally
		{
			vPathway.dispose();
		}
	}

}
