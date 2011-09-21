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
package atlas.model;

import java.io.Serializable;

public class Factor implements Comparable<Factor>, Serializable {
	private static final long serialVersionUID = -2122238431100451429L;

	String value;
	String name;

	public Factor(String name, String value) {
		this.value = value;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public int hashCode() {
		return (value + name).hashCode();
	}

	public boolean equals(Object obj) {
		return obj instanceof Factor &&
			hashCode() == obj.hashCode();
	}

	public String toString() {
		return name + " = " + value;
	}

	public int compareTo(Factor o) {
		if(name.equals(o.name)) {
			return value.compareTo(o.value);
		} else {
			return name.compareTo(o.name);
		}
	}
}
