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
package org.pathvisio.pluginmanager.impl.util;

import java.net.URL;

/**
 * Utility function related to getting Resources from the class path.
 * TODO: merge with org.pathvisio.util.Utils.
 */
public class Resources
{
	/**
	 * Get the {@link URL} for the resource stored in a jar file in the classpath
	 * @param name  the filename of the resource
	 * @return the URL pointing to the resource
	 */
	public static URL getResourceURL(String name)
	{
		URL url = Resources.class.getClassLoader().getResource(name);
		return url;
	}

}
