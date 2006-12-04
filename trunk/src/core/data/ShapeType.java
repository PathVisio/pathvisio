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
package data;

import java.util.Arrays;
import java.util.List;

public class ShapeType 
{
	public static final int RECTANGLE = 0;
	public static final int OVAL = 1;
	public static final int ARC = 2;
	public static final int CELLA = 3;
	public static final int RIBOSOME = 4;
	public static final int ORGANA = 5;
	public static final int ORGANB = 6;	
	public static final int ORGANC = 7;
	public static final int PROTEINB = 8;
	public static final int TRIANGLE = 9; // poly in MAPP
	public static final int VESICLE = 10;
	public static final int PENTAGON = 11; // poly in MAPP
	public static final int HEXAGON = 12; // poly in MAPP
	
	
	
	public static final List MappMappings = Arrays.asList(new String[] {
			"Rectangle","Oval","Arc",
			"CellA", "Ribosome",
			"OrganA", "OrganB", "OrganC", "ProteinB", "Poly", "Vesicle", "Poly", "Poly"
	});

	public static final List GmmlMappings = Arrays.asList(new String[] {
			"Rectangle","Oval","Arc",
			"CellA", "Ribosome",
			"OrganA", "OrganB", "OrganC", "ProteinComplex", "Triangle", 
			"Vesicle", "Pentagon", "Hexagon"
	});

	/*
	 * Warning when using fromMappName: in case value == Poly, 
	 * this will return Triangle. The caller needs to check for 
	 * this special
	 * case.
	 */
	public static int fromMappName (String value)
	{
		return MappMappings.indexOf(value);
	}
	
	public static String toMappName (int value)
	{
		return (String)MappMappings.get(value);
	}

	public static int fromGmmlName (String value)
	{
		return GmmlMappings.indexOf(value);
	}
	
	public static String toGmmlName (int value)
	{
		return (String)GmmlMappings.get(value);
	}

}

