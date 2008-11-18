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
package org.pathvisio.data;

/**
 * This class represents a record in the Sample table of the Expression database. 
 */
public class Sample implements Comparable<Sample>
{
	int idSample;
	String name;
	int dataType;
	
	/**
	 * Constructor of this class
	 * @param idSample	represents the 'idSample' column in the Sample table, an unique identifier
	 * for this sample
	 * @param name		represents the 'name' column in the Sample table, the name of the
	 * sample
	 * @param dataType	represents the 'dataType' column in the Sample table, the data type of
	 * the values stored in the column (using the field contsants in {@link java.sql.Types})
	 */
	public Sample(int idSample, String name, int dataType)
	{
		this.idSample = idSample;
		this.name = name;
		this.dataType = dataType;
	}
	
	public String getName() { return name == null ? "" : name; }
	protected void setName(String nm) { name = nm; }
	public int getDataType() { return dataType; }
	protected void setDataType(int type) { dataType = type; }
	public int getId() { return idSample; }
	protected void setId(int id) { idSample = id; }
	/**
	 * Compares this object to another {@link Sample} object based on the idSample property
	 * @param o	The {@link Sample} object to compare with
	 * @return	integer that is zero if the objects are equal, negative if this object has a
	 * lower idSample, positive if this object has a higher idSample
	 * @throws ClassCastException
	 */
	public int compareTo(Sample o)
	{
		return idSample - o.idSample;
	}
	
	public int hashCode() {
		return idSample;
	}
	
	public boolean equals(Object o) {
		if(o instanceof Sample) return ((Sample) o).idSample == idSample;
		return false;
	}
	
	/**
	 * Returns a readable String representation of this object
	 */
	public String toString()
	{
		return Integer.toString(idSample);
	}
}