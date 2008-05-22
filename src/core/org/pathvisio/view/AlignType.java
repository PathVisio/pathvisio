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
package org.pathvisio.view;

public enum AlignType {
	CENTERX("Center X", "Align horizontal centers", "aligncenterx.gif"),
	CENTERY("Center Y", "Align vertical centers", "aligncentery.gif"),
	LEFT("Left", "Align left edges", "alignleft.gif"),
	RIGHT("Right", "Align right edges", "alignright.gif"),
	TOP("Top", "Align top edges", "aligntop.gif"),
	BOTTOM("Bottom", "Align bottom edges", "alignbottom.gif"),
	WIDTH("Width", "Set common width", "sizewidth.gif"),
	HEIGHT("Height", "Set common height", "sizeheight.gif"),
	;
	
	String label;
	String description;
	String icon;
	
	AlignType(String label, String tooltip, String icon) {
		this.label = label;
		this.description = tooltip;
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}
}
