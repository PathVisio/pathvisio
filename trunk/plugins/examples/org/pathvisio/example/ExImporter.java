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
package org.pathvisio.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayImporter;
import org.pathvisio.plugin.Plugin;

/**
 * Example of how to create and register a Pathway importer.
 * <p>
 * Here we convert a text file to a pathway, by making a separate
 * label out of each line.
 */
public class ExImporter implements Plugin 
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop) 
	{
		this.desktop = desktop;
		
		// instantiate TextLinesImporter, our own importer, and register it
		desktop.getSwingEngine().getEngine().addPathwayImporter(new TextLinesImporter());
	}

	public void done() {}
	
	private static class TextLinesImporter implements PathwayImporter
	{
		/**
		 * Called after the user selected a file and picked this importer from the
		 * File types dropdown.
		 * <p>
		 * The Pathway parameter is an empty pathway. 
		 * Our job is to read file and add contents to pathway.
		 */
		public void doImport(File file, Pathway pathway)
				throws ConverterException 
		{
			try
			{
				// some constants: this is the size of each label
				double xpos = 300;
				double ypos = 300;
				double height = 300;
				double width = 1500;
				
				// open file for reading
				BufferedReader reader = null;
				reader = new BufferedReader(new FileReader (file));
				String line;
				// go over each line
				while ((line = reader.readLine()) != null)
				{
					line = line.trim();
					// skip empty lines
					if (line.equals ("")) { continue; }

					// construct a new label object, set the properties
					PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.LABEL);
					elt.setMWidth(width);
					elt.setMHeight(height);
					elt.setMTop(ypos);
					elt.setMLeft(xpos);
					elt.setTextLabel(line);
					// add the object to the pathway
					pathway.add(elt);
					ypos += height; // move to next line
				}
				// set the pathway title
				pathway.getMappInfo().setMapInfoName(file.getName());
				reader.close();
			}
			catch (IOException ex)
			{
				throw new ConverterException (ex);
			}
		}

		private static final String[] EXTENSIONS = new String[] { "txt" };
		
		public String[] getExtensions() 
		{
			return EXTENSIONS;
		}

		public String getName() 
		{
			return "Lines from text file";
		}
	}
}
