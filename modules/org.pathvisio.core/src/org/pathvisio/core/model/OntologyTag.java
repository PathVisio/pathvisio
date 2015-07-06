// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2015 BiGCaT Bioinformatics
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
package org.pathvisio.core.model;
/**
 * Class storing information about ontology tags for pathways.
 * @author jonathan
 *
 */
public class OntologyTag {
	private String id;
	private String term;
	private String ontology;
		
	public OntologyTag(String id, String term, String ontology) {
		this.id = id;
		this.term = term;
		this.ontology = ontology;
	}

	public String getId() {
		return id;
	}

	public String getTerm() {
		return term;
	}

	public String getOntology() {
		return ontology;
	}


}
