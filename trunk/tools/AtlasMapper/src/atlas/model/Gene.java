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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Gene implements Serializable {
	private static final long serialVersionUID = 7495816130887438094L;

	private String id;
	private Map<Factor, Set<FactorData>> factors = new TreeMap<Factor, Set<FactorData>>();

	Gene(String id) {
		this.id = id;
	}

	public Collection<Set<FactorData>> getFactorData() {
		return factors.values();
	}

	public String getId() {
		return id;
	}

	void addFactorData(FactorData fv) {
		Set<FactorData> fds = factors.get(fv.getFactor());
		if(fds == null) {
			factors.put(fv.getFactor(), fds = new HashSet<FactorData>());
		}
		fds.add(fv);
	}

	public Set<Factor> getFactors() {
		return factors.keySet();
	}

	public Set<FactorData> getFactorData(Factor factor) {
		return factors.get(factor);
	}

	public String toString() {
		String str = id;
		for(Set<FactorData> fds : factors.values()) {
			for(FactorData fd : fds) str += "\n\t" + fd;
		}
		return str;
	}
}
