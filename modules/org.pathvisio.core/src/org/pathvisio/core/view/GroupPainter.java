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
package org.pathvisio.core.view;

import java.awt.Graphics2D;

/**
 * Interface to implement group style specific
 * appearances.
 * @author thomas
 */
public interface GroupPainter {
	/**
	 * Draw the appearance of the given group.
	 * @param g The {@link Graphics2D} object to draw on
	 * @param group The group to draw
	 * @param flags Several flags that provide information about the
	 * group state. These include the {@link Group}.FLAG_* constants.
	 */
	public void drawGroup(Graphics2D g, Group group, int flags);
}
