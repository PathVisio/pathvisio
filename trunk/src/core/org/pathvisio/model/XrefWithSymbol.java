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
package org.pathvisio.model;

/**
 * Stores a combination of DataSource, id and symbol. Extension of Xref,
 * which only stores DataSource and id.
 */
public class XrefWithSymbol extends Xref implements Comparable<Xref> 
{
	private String symbol;
	
	public XrefWithSymbol(Xref xref, String symbol)
	{
		this (xref.id, xref.ds, symbol);
	}
	
	/**
	 * null values for all three params are allowed,
	 * because many times you build an XrefWithSymbol on the go.
	 */
	public XrefWithSymbol(String id, DataSource ds, String symbol) 
	{
		super (id, ds);
		this.symbol = symbol;
	}

	public String getSymbol() 
	{
		return symbol;
	}
	
	public void setSymbol(String value) 
	{ 
		symbol = value; 
	}

	/**
	 * Override to search by symbol first, then on the value of the xref
	 */
	@Override
	public int compareTo (Xref o2) 
	{
		if (o2 instanceof XrefWithSymbol)
		{
			XrefWithSymbol p2 = (XrefWithSymbol)o2;
			if(symbol != null && p2.symbol != null)
				return symbol.compareTo(p2.symbol);
		}
		return super.compareTo(o2);
	}	
}