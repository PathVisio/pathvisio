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


public class FactorData implements Comparable<FactorData>, Serializable {
	private static final long serialVersionUID = -6589555056693618552L;

	private Factor factor;
	private double pvalue;
	private int sign;
	private String experiment;

	public FactorData(Factor factor, double pvalue, int sign, String experiment) {
		this.factor = factor;
		this.pvalue = pvalue;
		this.sign = sign;
		this.experiment = experiment;
	}

	public String getExperiment() {
		return experiment;
	}

	public Factor getFactor() {
		return factor;
	}

	public double getPvalue() {
		return pvalue;
	}

	public int getSign() {
		return sign;
	}

	public int hashCode() {
		return ("" + factor + pvalue + sign).hashCode();
	}

	public boolean equals(Object obj) {
		return obj instanceof FactorData &&
			hashCode() == obj.hashCode();
	}

	public String toString() {
		return factor + ": pvalue = " + pvalue + "; sign = " + sign;
	}

	public int compareTo(FactorData o) {
		if(factor.equals(o.factor)) {
			return Double.compare(sign * pvalue, o.sign * o.pvalue);
		} else {
			return factor.compareTo(o.factor);
		}
	}
}
