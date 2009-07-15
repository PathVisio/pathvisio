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
package org.pathvisio.plugins.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.XrefWithSymbol;
import org.bridgedb.rdb.DataDerby;
import org.bridgedb.rdb.SimpleGdb;
import org.bridgedb.rdb.SimpleGdbFactory;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.GexManager;
import org.pathvisio.gex.ReporterData;
import org.pathvisio.gex.SimpleGex;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.PathwayParser;
import org.pathvisio.util.PathwayParser.ParseException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Checks which genes from a dataset are not mapped in any pathway
 * Useful for targeted development of new pathways.
 * 
 * TODO: make a plug-in out of this.
 */
public class IsDataInPathways 
{

//	static File fGex = new File ("/home/martijn/uni-wrk/datasets/Starvation/Intestine/only_protemics_genmapp_format.pgex");
//	static File fGdb = new File ("/home/martijn/PathVisio-Data/gene databases/Mm_Derby_20080102.pgdb");
//	static File pwDir = new File ("/home/martijn/wikipathways/Mus_musculus");

	static File fGex = new File ("/media/KINGSTON/muscle_t12_vs_t0_PathVisio.pgex");
	static File fGdb = new File ("/home/martijn/PathVisio-Data/gene databases/Mm_Derby_20080102.pgdb");
	static File pwDir = new File ("/home/martijn/wikipathways/Mus_musculus");
	
	static File outFile = new File ("/home/martijn/Desktop/isdatainpahtways.txt");
	
	public static void main(String[] args) throws IDMapperException, ParseException, FileNotFoundException
	{
		PreferenceManager.init();
		GexManager gexManager = new GexManager();
		gexManager.setCurrentGex("" + fGex, false);
		
		XMLReader xmlReader;
		
		try
		{
			xmlReader = XMLReaderFactory.createXMLReader();
		}
		catch (SAXException e)
		{
			Logger.log.error("Problem while searching pathways", e);
			return;
		}
		
		// read all rows of gex;
		SimpleGex gex = gexManager.getCurrentGex();
	
		Map<Xref, Xref> dataRefs = new HashMap<Xref, Xref>();
		Map<Xref, List<String>> counts = new HashMap<Xref, List<String>>();
		
		SimpleGdb gdb = SimpleGdbFactory.createInstance("" + fGdb, new DataDerby(), 0);
		
		for (int i = 0; i < gex.getMaxRow(); ++i) 
		{
			ReporterData data = gex.getRow(i);
			Xref src = data.getXref();
			counts.put (src, new ArrayList<String>());
			for (Xref dest : gdb.mapID(data.getXref()))
			{
				dataRefs.put (dest, src);
			}
			dataRefs.put (src, src);
		}
		
		for (File f : FileUtils.getFiles(pwDir, "gpml", false))
		{
			PathwayParser pp = new PathwayParser (f, xmlReader);
			
			for (XrefWithSymbol ref : pp.getGenes())
			{
				Xref ref2 = new Xref (ref.getId(), ref.getDataSource());
				if (dataRefs.containsKey(ref2))
				{
					counts.get(dataRefs.get(ref2)).add(pp.getName());
				}
			}
		}
		
		PrintStream out = new PrintStream(new FileOutputStream(outFile));
		for (int i = 0; i < gex.getMaxRow(); ++i) 
		{
			ReporterData data = gex.getRow(i);
			Xref ref = data.getXref();
			String bpText = gdb.getBpInfo(ref);
			String desc = "";
			if (bpText != null)
			{
				Pattern pat = Pattern.compile("<TH>Description:<TH>(.*)<TR>");
				Matcher mat = pat.matcher (bpText);
				if (mat.find())
				{
					desc = mat.group(1);
				}
			}
			List<String> pwyNames = counts.get(ref);
			out.print (i + "\t" +
					ref.getId() + "\t" + 
					ref.getDataSource().getSystemCode()  + "\t" +
					desc + "\t" + 
					pwyNames.size() + "\t");
			
			boolean first = true;
			for (String name : pwyNames)
			{
				if (!first)
				{
					out.print (" \\\\\\ ");
				}
				first = false;
				out.print (name);
			}
			out.println();
		}
		out.close();
	}

}
