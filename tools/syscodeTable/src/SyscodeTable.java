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

import org.pathvisio.model.DataSource;

/**
Makes a table of data sources in various formats
*/
class SyscodeTable
{
	abstract static private class Formatter
	{
		void header() {};
		abstract void row (DataSource ds);
		void footer() {}
	}
	
	static private class HtmlFormatter extends Formatter
	{
		private void cell(String prefix, String s)
		{
			System.out.println (prefix + "<td>" + s + "</td>");
		}
		
		private void row(String prefix, String[] l)
		{
			System.out.println (prefix + "<tr>");
			for (String s : l)
			{
				cell ("  " + prefix, s);
			}
			System.out.println (prefix + "</tr>");
		}
		
		void row(DataSource ds)
		{
			row ("  ", new String[] {ds.getFullName(), ds.getSystemCode() });
		}
		
		void header()
		{
			System.out.println ("<table>");
		}
		
		void footer()
		{
			System.out.println ("</table>");
		}
	}

	static private class TabFormatter extends Formatter
	{
		void row(DataSource ds)
		{
			System.out.println (ds.getFullName() + "\t" + ds.getSystemCode() );
		}
	}
	
	public static void main (String[] argv)
	{
		Formatter f = new HtmlFormatter();
		
		for (String s : argv)
		{
			if (s.equals ("html"))
			{
				f = new HtmlFormatter();
			}
			else if (s.equals ("tab"))
			{
				f = new TabFormatter();
			}
		}
		
		f.header();
		for (DataSource ds : DataSource.getDataSources())
		{
			f.row (ds);
		}
		f.footer();
	}
}