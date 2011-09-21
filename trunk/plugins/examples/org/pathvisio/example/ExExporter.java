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
package org.pathvisio.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.pathvisio.core.biopax.reflect.BiopaxElement;
import org.pathvisio.core.biopax.reflect.PublicationXref;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayExporter;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
 * Shows how to implement a new file format,
 * and how to register it with PathVisio.
 * <p>
 * In this case we'll create an exporter to
 * export only the literature references of
 * a Pathway.
 */
public class ExExporter implements Plugin
{
	private PvDesktop desktop;

	public void init(PvDesktop desktop)
	{
		this.desktop = desktop;
		// here we register an instance of our exporter
		desktop.getSwingEngine().getEngine().addPathwayExporter(new BibliographyExporter());
	}

	/**
	 * Export all literature references of a pathway
	 * as a simple plain-text format.
	 * This is not any standard format, it's just an example
	 * of what an exporter can do.BibliographyExporter
	 */
	private static class BibliographyExporter implements PathwayExporter
	{
		/**  Called after the user has selected a file to export */
		public void doExport(File file, Pathway pathway)
				throws ConverterException {

			try {
				// open the file for writing
				FileWriter fos = new FileWriter(file);
				int i = 0;
				// loop over all embedded Biopax elements of the Pathway
				for (BiopaxElement be : pathway.getBiopaxElementManager().getElements())
				{
					fos.write ("BIBLIOGRAPHY\n");
					fos.write ("============\n");
					// check if this is a publication reference
					if (be instanceof PublicationXref)
					{
						// print some information in a very simple text format
						PublicationXref pxref = (PublicationXref)be;
						fos.write ("\n");
						i++;
						fos.write ("#" + i + ":\n");
						fos.write("Authors: " + pxref.getAuthors() + "\n");
						fos.write("Title: " + pxref.getTitle() + "\n");
						fos.write("Source: " + pxref.getSource() + "\n");
						fos.write("Year: " + pxref.getYear() + "\n");
						fos.write("PMID: " + pxref.getPubmedId() + "\n");
					}
				}
				// if i remains at zero, there were no references
				if (i == 0)
				{
					fos.write ("\nNo publication references found\n");
				}
				fos.close();
			}
			catch (IOException e)
			{
				throw new ConverterException (e);
			}

		}

		private static final String[] EXTENSIONS = new String[] { "txt" };

		/**
		 * Valid extensions for this file type. The first
		 * of these will be automatically appended to the File passed to doExport.
		 */
		public String[] getExtensions()
		{
			return EXTENSIONS;
		}

		/**
		 * A suitable name to display in the JFileChooser dialog.
		 */
		public String getName()
		{
			return "Text Bibliography Exporter";
		}
	}

	public void done() {}
}
