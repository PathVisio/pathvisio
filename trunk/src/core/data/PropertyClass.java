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

/**
 * The properties in {@link PropertyType} define properties of different classes,
 * all the possible classes are defined here.
 */
public enum PropertyClass 
{
	BOOLEAN,
	DOUBLE,
	INTEGER, 
	DATASOURCE,
	LINESTYLE,
	COLOR,
	STRING,
	ORIENTATION,
	SHAPETYPE,
	LINETYPE,
	GENETYPE,
	FONT,
	ANGLE,
	ORGANISM,
	DB_ID,
	DB_SYMBOL,
}
