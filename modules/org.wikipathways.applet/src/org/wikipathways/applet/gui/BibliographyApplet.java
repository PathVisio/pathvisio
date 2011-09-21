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
package org.wikipathways.applet.gui;

import java.awt.BorderLayout;
import java.awt.Container;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.gui.panels.LitReferencePanel;

public class BibliographyApplet extends PathwayPageApplet
{

	@Override
	protected void createGui() {
		Container content = getContentPane();

		Pathway pathway = wiki.getPathway();
		LitReferencePanel refPanel = new LitReferencePanel(wiki.getSwingEngine());
		refPanel.setInput(pathway.getMappInfo());
		content.add(refPanel, BorderLayout.CENTER);
	}

	@Override
	protected String getDefaultDescription() {
		return "Modified bibliography";
	}
}
