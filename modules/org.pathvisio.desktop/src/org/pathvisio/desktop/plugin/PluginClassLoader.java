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
package org.pathvisio.desktop.plugin;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A class loader for loading classes from a plugin jar files.
 *
 * inspired by CDK PluginClassLoader
 */
public class PluginClassLoader extends URLClassLoader
{
    public PluginClassLoader(URL url)
    {
        super (new URL[] { url });
    }

    /**
     * This class loading method overwrites the default behaviour and tries
     * to look in the plugin jar first. This allows that users put
     * plugins in their local plugin dir that come with the program by default
     * too. The newest plugin is then loaded.
     *
     * @param name the name of the main class
     * @exception ClassNotFoundException if the specified class could not
     *            be found
     */
//    @Override public Class<?> loadClass(String name) throws ClassNotFoundException
//    {
//        Logger.log.debug("Loading from plugin jar: " + name);
//        Class<?> aClass = null;
//        try
//        {
//            aClass = super.findClass(name);
//            Logger.log.debug("  found: " + aClass);
//            return aClass;
//        }
//        catch (ClassNotFoundException ex)
//        {
//            Logger.log.debug("  not found in plugin jar");
//        }
//
//        try
//        {
//            aClass = super.loadClass(name);
//            Logger.log.debug("  found: " + aClass);
//        }
//        catch (ClassNotFoundException ex)
//        {
//            Logger.log.error("  not found in elsewhere", ex);
//            throw ex;
//        }
//        return aClass;
//    }

}
