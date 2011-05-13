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
package org.pathvisio.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.LayoutType;
import org.pathvisio.core.view.MIMShapes;
import org.pathvisio.core.view.VPathway;

public class ResizeForGenMAPP {
	static final double MAX_WIDTH = 35000;
	static final double MAX_HEIGHT = MAX_WIDTH;

	public static void main(String[] args) {
		try {
			File in = new File(args[0]);
			List<File> files = new ArrayList<File>();
			if(in.isDirectory()) {
				files.addAll(Arrays.asList(in.listFiles()));
			} else {
				files.add(in);
			}

			PreferenceManager.init();
			MIMShapes.registerShapes();

			for(File f : files) {
				if(!f.getName().endsWith(".gpml") || f.getName().endsWith("-resized.gpml")) {
					Logger.log.info("Skipping " + f.getName() + ", not a gpml file");
					continue;
				}
				Pathway pathway = new Pathway();
				pathway.readFromXml(f, true);

				resizePathway(pathway);

				String newName = f.getAbsolutePath();
				newName = newName.substring(0, newName.lastIndexOf('.')) + "-resized.gpml";
				File out = new File(newName);
				pathway.writeToXml(out, true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void resizePathway(Pathway pathway) {
		double[] size = pathway.getMappInfo().getMBoardSize();

		if(size[0] > MAX_WIDTH || size[1] > MAX_HEIGHT) {
			double zoom = Math.min(
					MAX_WIDTH / size[0],
					MAX_HEIGHT / size[1]
			);
			Logger.log.info("Pathway size " + size[0] + ", " + size[1] +
					" is larger than GenMAPP limit");
			Logger.log.info("Decreasing size with factor " + zoom);

			for(PathwayElement pwe : pathway.getDataObjects()) {
				//Skip groups
				if(pwe.getObjectType() == ObjectType.GROUP) continue;

				//Adjust position
				pwe.setMCenterX(pwe.getMCenterX() * zoom);
				pwe.setMCenterY(pwe.getMCenterY() * zoom);
			}

			VPathway vp = new VPathway(null);
			vp.fromModel(pathway);

			//Restack complexes
			for(PathwayElement pwe : pathway.getDataObjects()) {
				if(pwe.getObjectType() == ObjectType.GROUP) {
					for(PathwayElement ge : pathway.getGroupElements(pwe.getGroupId())) {
						Graphics g = vp.getPathwayElementView(ge);
						if(g != null) {
							g.select();
						}
					}
					vp.layoutSelected(LayoutType.STACK_CENTERX);
					vp.clearSelection();
				}
			}
		} else {
			Logger.log.info("Nothing to do: pathway size " +
					size[0] + ", " + size[1] + " is below GenMAPP limit");
		}
	}
}
