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
package org.pathvisio.biopax;

import java.awt.Color;

import org.biopax.paxtools.model.level2.ControlType;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.pathvisio.model.ConnectorType;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.LineType;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.PathwayElement;

public class DefaultStyleMapper implements StyleMapper {
	public void mapControl(control c, PathwayElement line) {
		line.setConnectorType(ConnectorType.CURVED);
		ControlType type = c.getCONTROL_TYPE();
		switch(type) {
		case ACTIVATION:
		case ACTIVATION_ALLOSTERIC:
		case ACTIVATION_NONALLOSTERIC:
		case ACTIVATION_UNKMECH:
			line.setEndLineType(LineType.fromName("mim-catalysis"));
			break;
		case INHIBITION:
		case INHIBITION_ALLOSTERIC:
		case INHIBITION_COMPETITIVE:
		case INHIBITION_IRREVERSIBLE:
		case INHIBITION_NONCOMPETITIVE:
		case INHIBITION_OTHER:
		case INHIBITION_UNCOMPETITIVE:
		case INHIBITION_UNKMECH:
			line.setEndLineType(LineType.TBAR);
			break;
			
		}
	}
	
	public void mapConversion(conversion c, PathwayElement line) {
		line.setEndLineType(LineType.ARROW);
	}
	
	public void mapConversionLeft(conversion c, PathwayElement line) {
		line.setConnectorType(ConnectorType.CURVED);
	}
	
	public void mapConversionRight(conversion c, PathwayElement line) {
		line.setEndLineType(LineType.ARROW);
		line.setConnectorType(ConnectorType.CURVED);
	}

	public void mapPhysicalEntity(physicalEntity e, PathwayElement datanode) {
		//Map type and apply color mapping
		if(e instanceof smallMolecule) {
			datanode.setDataNodeType(DataNodeType.METABOLITE);
			datanode.setColor(Color.BLUE);
		} else if (e instanceof protein) {
			datanode.setDataNodeType(DataNodeType.PROTEIN);
		} else if (e instanceof dna) {
			datanode.setDataNodeType(DataNodeType.UNKOWN);
			datanode.setColor(Color.GREEN);
		} else if (e instanceof rna) {
			datanode.setDataNodeType(DataNodeType.RNA);
			datanode.setColor(Color.ORANGE);
		}
	}
	
	public PathwayElement createUnknownParticipant() {
		PathwayElement pwe = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		pwe.setInitialSize();
		pwe.setColor(Color.lightGray);
		pwe.setTextLabel("?");
		return pwe;
	}
}
