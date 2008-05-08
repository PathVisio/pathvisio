//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. 
//You may obtain a copy of the License at 

//http://www.apache.org/licenses/LICENSE-2.0 

//Unless required by applicable law or agreed to in writing, software 
//distributed under the License is distributed on an "AS IS" BASIS, 
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//See the License for the specific language governing permissions and 
//limitations under the License.

package org.pathvisio.model;

public class XrefWithSymbol implements Comparable<XrefWithSymbol> 
{
	private String symbol;
	private Xref xref;
	
	public XrefWithSymbol(Xref xref, String symbol) 
	{
		this.symbol = symbol;
		this.xref = xref;
	}

	public String getSymbol() 
	{
		return symbol;
	}

	public Xref getXref()
	{
		return xref;
	}
	
	public int compareTo (XrefWithSymbol o2) 
	{
		if(symbol != null && o2.symbol != null)
			return symbol.compareTo(o2.getSymbol());
		if(xref != null && o2.xref != null)
			return xref.compareTo (o2.xref);
		return hashCode() - o2.hashCode();
	}	
}