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
package org.pathvisio.core.gpmldiff;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility collection: values are indexed by two keys.
 * Can be used to implement 2D tables of unknown size.
 *
 * Note: the set of keys has to be known at creation, and can't be changed later!
 *
 * @param <S> Object type of row keys
 * @param <T> Object type of column keys
 * @param <U> Object type of values
 */
public class Map2D <S, T, U>
{
	Map<S, Integer> rows;
	Map<T, Integer> cols;
	U[][] cell;

	void set (S row, T col, U value)
	{
		int rowInt = rows.get (row);
		int colInt = cols.get (col);
		cell[rowInt][colInt] = value;
	}

	U get (S row, T col)
	{
		int rowInt = rows.get (row);
		int colInt = cols.get (col);
		return cell[rowInt][colInt];
	}

	/**
	   Note: doubles in the collection are discarded!
	 */
	@SuppressWarnings("unchecked")
	Map2D (Collection<S> aRows, Collection<T> aCols)
	{
		rows = new HashMap <S, Integer>();
		cols = new HashMap <T, Integer>();
		int i = 0;
		for (S s : aRows)
		{
			rows.put (s, i);
			i++;
		}
		i = 0;
		for (T t : aCols)
		{
			cols.put (t, i);
			i++;
		}
		// Generate cell array as Object[][], not possible to do U[][] directly.
		// this will give an unavoidable compiler warning.
		cell = (U[][]) new Object[rows.size()][cols.size()];
	}

}