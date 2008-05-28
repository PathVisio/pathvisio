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
package org.pathvisio.visualization.plugins;

import java.awt.Component;
import java.awt.Graphics2D;

import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationMethod;

public class ColorByExpression extends VisualizationMethod {
	
	public ColorByExpression(Visualization v, String registeredName) {
		super(v, registeredName);
		setIsConfigurable(false); //TODO: make configurable
	}

	public String getDescription() {
		return "Color DataNodes by their expression value";
	}

	public String getName() {
		return "Expression as color";
	}

	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		// TODO Auto-generated method stub
		
	}

	public Component visualizeOnToolTip(Graphics g) {
		// TODO Auto-generated method stub
		return null;
	}
}
