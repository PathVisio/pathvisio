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
package org.pathvisio.statistics;

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

import org.bridgedb.AttributeMapper;
import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.PathwayParser.ParseException;
import org.pathvisio.core.util.Utils;
import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.data.IRow;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.statistics.PathwayMap.PathwayInfo;

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

	public static void main(String[] args) throws IDMapperException, ParseException, FileNotFoundException, DataException
	{
		PreferenceManager.init();
		GexManager gexManager = new GexManager();
		gexManager.setCurrentGex("" + fGex, false);

		// read all rows of gex;
		DataInterface gex = gexManager.getCurrentGex();

		Map<Xref, Xref> dataRefs = new HashMap<Xref, Xref>();
		Map<Xref, List<String>> counts = new HashMap<Xref, List<String>>();

		IDMapper gdb = BridgeDb.connect("idmapper-pgdb:" + fGdb);

		for (int i = 0; i < gex.getNrRow(); ++i)
		{
			IRow data = gex.getRow(i);
			Xref src = data.getXref();
			counts.put (src, new ArrayList<String>());
			for (Xref dest : gdb.mapID(data.getXref()))
			{
				dataRefs.put (dest, src);
			}
			dataRefs.put (src, src);
		}

		PathwayMap map = new PathwayMap(pwDir);

		for (PathwayInfo info : map.getPathways())
		{
			for (Xref ref : info.getSrcRefs())
			{
				Xref ref2 = new Xref (ref.getId(), ref.getDataSource());
				if (dataRefs.containsKey(ref2))
				{
					counts.get(dataRefs.get(ref2)).add(info.getName());
				}
			}
		}

		PrintStream out = new PrintStream(new FileOutputStream(outFile));
		for (int i = 0; i < gex.getNrRow(); ++i)
		{
			IRow data = gex.getRow(i);
			Xref ref = data.getXref();
			String bpText = Utils.oneOf (((AttributeMapper)gdb).getAttributes(ref, "Backpage"));
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
