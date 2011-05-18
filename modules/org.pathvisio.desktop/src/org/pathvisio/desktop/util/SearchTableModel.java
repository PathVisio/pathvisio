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
package org.pathvisio.desktop.util;


/**
 * SearchTableModel defines a specific type of ListWithPropertiesTableModel,
 * namely one that is meant to store a list of Search results.
 */
public class SearchTableModel extends
	ListWithPropertiesTableModel<SearchTableModel.Column, MatchResult>
{

	/**
	 * Defines the columns that can be displayed in a SearchTable
	 */
	public static enum Column implements PropertyColumn
	{
		PATHWAY_NAME("pathway"),
		DIRECTORY("directory"),
		IDS("ids"),
		NAMES("names");

		private String title;
		public String getTitle() { return title; }

		private Column (String title)
		{
			this.title = title;
		}
	}

}
