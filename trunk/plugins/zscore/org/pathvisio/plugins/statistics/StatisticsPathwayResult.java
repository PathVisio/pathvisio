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
package org.pathvisio.plugins.statistics;

import java.io.File;

import org.pathvisio.desktop.util.RowWithProperties;

/**
 * Statistics calculation for a single pathway,
 * to be shown as a row in the statistics result table
 */
public class StatisticsPathwayResult implements RowWithProperties<Column>
{
	private int r = 0;
	private int n = 0;
	private int total = 0;
	private String name;
	private double z = 0;
	private File f;
	double permP = 0;
	double adjP = 0;

	/**
	 * Get the pathway file.
	 */
	public File getFile() { return f; }

	StatisticsPathwayResult (File f, String name, int n, int r, int total, double z)
	{
		this.f = f;
		this.r = r;
		this.n = n;
		this.total = total;
		this.name = name;
		this.z = z;
	}

	public String getProperty(Column prop)
	{
		switch (prop)
		{
		case N: return "" + n;
		case R: return "" + r;
		case TOTAL: return "" + total;
		case PATHWAY_NAME: return name;
		case PERMPVAL: return String.format ("%3.2f", (float)permP);
		case ADJPVAL: return String.format ("%3.2f", (float)adjP);
		case ZSCORE: return String.format ("%3.2f", (float)z);
		case PCT: return String.format("%3.2f%%", (n == 0 ? Float.NaN : 100.0 * (float)r / (float)n));
		case FILE_NAME: return f.getName();
		default : throw new IllegalArgumentException("Unknown property");
		}
	}

	public double getZScore()
	{
		return z;
	}
}