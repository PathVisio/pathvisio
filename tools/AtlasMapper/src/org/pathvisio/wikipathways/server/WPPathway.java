// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.wikipathways.server;

import org.pathvisio.model.Pathway;

public class WPPathway {
	Pathway pathway;
	String id;
	String revision;

	public WPPathway(String id, String revision, Pathway pathway) {
		this.pathway = pathway;
		this.id = id;
		this.revision = revision;
	}

	public String getId() {
		return id;
	}

	public Pathway getPathway() {
		return pathway;
	}

	public String getRevision() {
		return revision;
	}
}
