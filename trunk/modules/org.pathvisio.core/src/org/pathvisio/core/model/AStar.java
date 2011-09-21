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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConnectorShape.Segment;

/**
 * Package private.
 *
 * Implementation of the AStar algorithm,
 * that can make connectors that avoid overlap with other shapes.
 * See http://theory.stanford.edu/~amitp/GameProgramming/index.html for a good explanation.
 *
 * TODO: improvement where, instead of using a fixed grid, we calculate a set of magic horizontal
 * and vertical lines and use that as basis for our grid.
 */
class AStar
{
	// distance between nodes
	private static final double ASTAR_STEP_SIZE = 15.0;

	// maximum number of simultaneously open nodes
	private static final double OPEN_NODE_CUTOFF = 5000;

	// extra distance we are willing to travel to avoid an extra elbow:
	private static final double ELBOW_PENALTY = 20.0;

	// extra distance we are willing to travel to avoid going through an object
	private static final double OVERLAP_PENALTY = 300.0;

	// queue of open nodes to examine
	private PriorityQueue<AStarNode> open = new PriorityQueue<AStarNode>();

	// positions already covered, with their associated g value
	private Map<Integer, Double> opened = new HashMap<Integer, Double>();

	private ConnectorRestrictions restrictions;

	// some stats
	private int nodesOpened = 0;
	private int maxQueueSize = 0;

	// add a node to the queue, but only if it's not in the opened set,
	// or if it has a smaller g than the one in the opened set
	private void openNode(AStarNode n)
	{
		int i = n.getPosAsInt();
		if (!opened.containsKey(i) || opened.get(i) > n.g)
		{
			open.add (n);
			opened.put (i, n.g);
			nodesOpened++;
		}
	}

	AStar (ConnectorRestrictions restrictions)
	{
		this.restrictions = restrictions;
	}

	// calculate the segments
	Segment[] getSegmentsAStar()
	{
		Logger.log.info ("ASTAR; calculation start");

		// put start node on the queue
		Point2D target = restrictions.getEndPoint();
		open.add(new AStarNode (restrictions.getStartPoint(), AStarNode.DIR_N, target));

		AStarNode curr;
		AStarNode end = null;

		// get next open node from the queue
		while ((curr = open.poll()) != null)
		{
			if (curr.pos.distance(target) < 2 * ASTAR_STEP_SIZE)
			{
				// finished!
				end = curr;
				break;
			}
			else
			{
				// open node in current direction
				openNode (new AStarNode (curr, curr.dir, ASTAR_STEP_SIZE, restrictions));

				// open nodes in orthogonal directions
				if (curr.dir == AStarNode.DIR_N ||
					curr.dir == AStarNode.DIR_S)
				{
					// open east and west nodes
					openNode (new AStarNode (curr, AStarNode.DIR_W, ASTAR_STEP_SIZE, restrictions));
					openNode (new AStarNode (curr, AStarNode.DIR_E, ASTAR_STEP_SIZE, restrictions));
				}
				else
				{
					// open north and south nodes
					openNode (new AStarNode (curr, AStarNode.DIR_N, ASTAR_STEP_SIZE, restrictions));
					openNode (new AStarNode (curr, AStarNode.DIR_S, ASTAR_STEP_SIZE, restrictions));
				}
			}

			if (open.size() > maxQueueSize)
			{
				maxQueueSize = open.size();
			}

			// make sure we don't go on too long...
			if (open.size() > OPEN_NODE_CUTOFF) break;
		}

		Segment[] result;
		// now we start backtracking the node tree
		if (end == null)
		{
			// could not find a valid route within reasonable limits.
			result = new Segment[] {
					new Segment (
							restrictions.getStartPoint(),
							restrictions.getEndPoint()
							)
					};
		}
		else
		{
			List<Segment> resultList = new ArrayList<Segment>();

			//calculate segments
			curr = end;
			Point2D pEnd = curr.pos;
			while (curr.parent != null)
			{
				if (curr.parent.dir != curr.dir)
				{
					Point2D pStart = curr.parent.pos;
					// since we're moving backwards, insert at the beginning
					resultList.add(0, new Segment (pStart, pEnd));
					pEnd = pStart;
				}
				curr = curr.parent;
			}
			// final segment to last node
			Point2D pStart = curr.pos;
			resultList.add(0, new Segment (pStart, pEnd));

			result = resultList.toArray(new Segment[0]);
		}

		Logger.log.info ("ASTAR; calculation ended; max queue size: "
				+ maxQueueSize + "; nodes opened " + nodesOpened);
		return result;
	}

	private static class AStarNode implements Comparable<AStarNode>
	{
		static final int DIR_N = 0;
		static final int DIR_E = 1;
		static final int DIR_S = 2;
		static final int DIR_W = 3;

		private final double[] tblDx = {0, 1, 0, -1};
		private final double[] tblDy = {-1, 0, 1, 0};

		AStarNode parent;
		int dir;
		Point2D pos;
		double g;
		double h;

		// constructor for the head node
		AStarNode(Point2D pos, int dir, Point2D target)
		{
			parent = null;
			this.dir = dir;
			this.pos = pos;
			g = 0;
			h = calculateH(target);
		}

		// heuristic: estimate remaining travel cost
		private double calculateH (Point2D target)
		{
			// Manhattan distance
			double hdx = target.getX() - pos.getX();
			double hdy = target.getY() - pos.getY();
			return Math.abs (hdx) + Math.abs (hdy);
		}

		AStarNode(AStarNode parent, int dir, double dist, ConnectorRestrictions restrictions)
		{
			if (parent == null) throw new NullPointerException();

			this.parent = parent;
			this.dir = dir;

			double dx = dist * tblDx[dir];
			double dy = dist * tblDy[dir];
			pos = new Point2D.Double (parent.pos.getX() + dx, parent.pos.getY() + dy);

			// calculate g, cost to this node
			g = parent.g + dist;

			// penalty on change of direction
			if (parent.dir != dir)
			{
				g += ELBOW_PENALTY;
			}

			// penalty on crossing something
			if (restrictions.mayCross (pos) != null)
			{
				g += OVERLAP_PENALTY;
			}

			// calculate h
			h = calculateH(restrictions.getEndPoint());
		}

		// calculate a more or less unique (one-dimensional) integer for this node position
		int getPosAsInt ()
		{
			int x = (int)(pos.getX() / ASTAR_STEP_SIZE);
			int y = (int)(pos.getY() / ASTAR_STEP_SIZE);

			return (x & 0xFFFF) + ((y & 0xFFFF) << 16);
		}

		// natural ordering for priority queue:
		// f = g + h, where
		// f = total cost
		// g = cost to get to this node
		// h = estimated cost to get to target
		public int compareTo (AStarNode o)
		{
			return (int)((g+h) - (o.g + o.h));
		}
	}

}
