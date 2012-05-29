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
package org.pathvisio.core.model;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Set;

import org.pathvisio.core.util.Utils;

/**
 * This class only contains static methods and should not be instantiated.
 */
public abstract class GraphLink
{

	/**
	 * All classes that have a graphId must implement this interface.
	 * Those are PathwayElement.MPoint (i.e. points)
	 * and PathwayElement (i.e. DataNodes, Shapes, etc).
	 * They are needed for being refered to.
	 *
	 * This interface exists so we can easily iterate through all
	 * objects containing a graphId.
	 */
	public interface GraphIdContainer
	{
		String getGraphId();
		void setGraphId(String id);
		/** generate a unique graph Id and use that. */
		String setGeneratedGraphId();
		Set<GraphRefContainer> getReferences();
		/**
		 * return the parent Gmmldata Object,
		 * needed for maintaining a consistent list of graphId's
		 */
		Pathway getPathway();

		/**
		 * Convert a point to shape coordinates (relative
		 * to the bounds of the GraphIdContainer)
		 */
		Point2D toRelativeCoordinate(Point2D p);

		/**
		 * Convert a point to pathway coordinates (relative
		 * to the pathway)
		 */
		Point2D toAbsoluteCoordinate(Point2D p);
	}

	/**
	 * All classes that want to refer *to* a GraphIdContainer must
	 * implement this interface. At this time that only goes for
	 * PathwayElement.MPoint.
	 */
	public interface GraphRefContainer
	{
		String getGraphRef();
		void linkTo(GraphIdContainer idc, double relX, double relY);
		void unlink();
		
		double getRelX();
		double getRelY();

		/**
		 * return the parent Pathway object,
		 * needed for maintaining a consistent list of graphId's
		 */
		Pathway getPathway();
		
		/**
		 * Called whenever the object being referred to changes coordinates.
		 */
		void refeeChanged();
	}

	/**
	 * Give an object that implements the graphId interface
	 * a graphId, thereby possibly linking it to new objects.
	 *
	 * This is a helper for classes that need to implement the GraphIdContainer interface,
	 * to avoid duplication.
	 *
	 * @param v the graphId
	 * @param c the object to is going to get the new graphId
	 * @param gd the pathway model, which is maintaining a complete list of all graphId's in this pathway
	 */
	protected static void setGraphId(String v, GraphIdContainer c, Pathway data)
	{
		String graphId = c.getGraphId();
		if (graphId == null || !graphId.equals(v))
		{
			if (data != null)
			{
				if (graphId != null)
				{
					data.removeGraphId(graphId);
				}
				if (v != null)
				{
					data.addGraphId(v, c);
				}
			}
		}
	}

	/**
	 * Return a list of GraphRefContainers (i.e. points)
	 * referring to a certain GraphId.
	 *
	 * @param gid
	 * @param gd
	 * @return
	 */
	public static Set<GraphRefContainer> getReferences(GraphIdContainer gid, Pathway gd)
	{
		if (gd == null || Utils.isEmpty(gid.getGraphId())) 
			return Collections.emptySet();
		else
			return gd.getReferringObjects(gid.getGraphId());
	}
}

