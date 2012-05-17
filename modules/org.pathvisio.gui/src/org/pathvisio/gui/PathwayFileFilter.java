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
package org.pathvisio.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import org.pathvisio.core.model.PathwayIO;

/**
 * A filefilter that filters files for a given {@link PathwayImporter} or {@link PathwayExporter}.
 * Can be used to create a {@link FileDialog} for importers or exporters.
 * @author thomas
 */
public class PathwayFileFilter extends FileFilter {
	String[] exts;
	String name;

	public PathwayFileFilter(PathwayIO imp) {
		exts = imp.getExtensions();
		name = imp.getName();
	}

	public String getDefaultExtension() {
		return exts[0];
	}

	public boolean accept(File f) {
		if(f.isDirectory()) return true;

		String fn = f.toString();
		int i = fn.lastIndexOf('.');
		if(i > 0) {
			String ext = fn.substring(i + 1);
			for(String impExt : exts) {
				if(impExt.equalsIgnoreCase(ext)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getDescription() {
		StringBuilder extstr = new StringBuilder();
		for(String e : exts) {
			extstr.append(".");
			extstr.append(e);
			extstr.append(", ");
		}
		String str = extstr.substring(0, extstr.length() - 2);
		return name + " (" + str + ")";
	}
}
