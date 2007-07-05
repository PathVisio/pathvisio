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
/* An extension of URLClassLoader that implements DelegatedClassLoader */

import java.net.URL;
import java.net.URLClassLoader;

public class DelegatedURLClassLoader extends URLClassLoader implements DelegatedClassLoader {
    public DelegatedURLClassLoader() {
	super(new URL[]{});
    }
    public DelegatedURLClassLoader(URL[] urls) {
	super(urls);
    }
    public String delegatedFindLibrary(String name) {
	return super.findLibrary(name);
    }
    public Class delegatedFindClass(String name) throws ClassNotFoundException {
	return super.findClass(name);
    }
    public URL delegatedFindResource(String name) {
	return super.findResource(name);
    }
}
