// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import org.pathvisio.model.*;
import org.pathvisio.data.*;
import java.util.*;
import java.io.*;

class TutorialFiles
{
	public static void main (String[] argv)
	{
		
		final String tutorialPwy = "Hs_Apoptosis.gpml";
		Pathway pwy = new Pathway();
		try
		{
			pwy.readFromXml (new File (tutorialPwy), true);
		}
		catch (ConverterException e)
		{
			e.printStackTrace();
			return;
		}
		
		List<Xref> refs = pwy.getDataNodeXrefs();

		assert (refs.contains (new Xref ("8717", DataSource.ENTREZ_GENE)));
		assert (refs.contains (new Xref ("7132", DataSource.ENTREZ_GENE)));
		assert (!refs.contains (new Xref ("1111", DataSource.ENTREZ_GENE)));
		assert (refs.size() == 94);

		// now look up all cross references in the human Gdb.

		try
		{
			Gdb.connect ("/home/martijn/PathVisio-Data/gene databases/Hs_41_36c.pgdb");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
}