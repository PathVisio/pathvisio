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
package org.pathvisio.core.preferences;

/**
 * A preference must have at least two things:
 * <ol>
 * <li>a key in the hash maintained by PreferenceMgr
 * <li>a default value
 * </ol>
 * Anything that implements this interface can be stored as a preference.
 * We recommend defining mulitple Preferences in an enum, but that is not absolutely
 * required.
 */
public interface Preference
{
	/** name will be the "key" in the properties file.
	 * If you use an enum that implements this interface, name will be automatically defined  */
	public String name();

	/**
	 * The default value for this property. Used to initialize when the key is not
	 * present in the properties file, or if the user wants to reset to default values.
	 */
	public String getDefault();
}
