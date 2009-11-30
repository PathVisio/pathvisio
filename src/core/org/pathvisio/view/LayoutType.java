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
package org.pathvisio.view;

/**
 * Identifiers for layout action types related to neatly laying out pathway elements, a. o.
 * aligning, scaling to the same size and stacking pathway elements.
 *
 * For each action, a short description, a long description and an icon name is stored.
 */
public enum LayoutType {
	ALIGN_CENTERX("Align Center X", "Align horizontal centers", "aligncenterx.gif"),
	ALIGN_CENTERY("Align Center Y", "Align vertical centers", "aligncentery.gif"),
	ALIGN_LEFT("Align Left", "Align left edges", "alignleft.gif"),
	ALIGN_RIGHT("Align Right", "Align right edges", "alignright.gif"),
	ALIGN_TOP("Align Top", "Align top edges", "aligntop.gif"),
	ALIGN_BOTTOM("Align Bottom", "Align bottom edges", "alignbottom.gif"),
	COMMON_WIDTH("Common Width", "Set common width", "sizewidth.gif"),
	COMMON_HEIGHT("Common Height", "Set common height", "sizeheight.gif"),
	STACK_CENTERX("Stack Center X", "Stack vertical center", "stackverticalcenter.gif"),
	STACK_CENTERY("Stack Center Y", "Stack horizontal center", "stackhorizontalcenter.gif"),
	STACK_LEFT("Stack Left", "Stack vertical left", "stackverticalleft.gif"),
	STACK_RIGHT("Stack Right", "Stack veritcal right", "stackverticalright.gif"),
	STACK_TOP("Stack Top", "Stack horizontal top", "stackhorizontaltop.gif"),
	STACK_BOTTOM("Stack Bottom", "Stack horizontal bottom", "stackhorizontalbottom.gif"),
	;

	private String label;
	private String description;
	private String icon;

	LayoutType(String label, String tooltip, String icon) {
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
