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
package org.pathvisio.view;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.AbstractShape;
import org.pathvisio.model.IShape;
import org.pathvisio.model.LineType;

/**
 * Collection of Shapes and ArrowHeads used in
 * the Molecular Interaction Map - style of pathways
 */
public class MIMShapes
{
	public static final LineType MIM_NECESSARY_STIMULATION = LineType.create ("mim-necessary-stimulation", "Arrow");
    public static final LineType MIM_BINDING = LineType.create ("mim-binding", "Arrow");
    public static final LineType MIM_CONVERSION = LineType.create ("mim-conversion", "Arrow");
    public static final LineType MIM_STIMULATION = LineType.create ("mim-stimulation", "Arrow");
    public static final LineType MIM_MODIFICATION = LineType.create ("mim-modification", "Arrow");
    public static final LineType MIM_CATALYSIS =LineType.create ("mim-catalysis", "Arrow");
    public static final LineType MIM_INHIBITION = LineType.create ("mim-inhibition", "Arrow");
    public static final LineType MIM_CLEAVAGE = LineType.create ("mim-cleavage", "Arrow");
    public static final LineType MIM_COVALENT_BOND = LineType.create ("mim-covalent-bond", "Arrow");
    public static final LineType MIM_BRANCHING_LEFT = LineType.create ("mim-branching-left", null);
    public static final LineType MIM_BRANCHING_RIGHT = LineType.create ("mim-branching-right", null);
    public static final LineType MIM_TRANSLATION = LineType.create ("mim-transcription-translation", "Arrow");
    public static final LineType MIM_GAP = LineType.create ("mim-gap", null);

    private static final int MIM_PHOSPHORYLATED = 0;
	private static final int MIM_DEGRADATION = 1;
	private static final int MIM_INTERACTION = 2;

	@Deprecated
    public static final IShape MIM_PHOSPHORYLATED_SHAPE = new AbstractShape (getPluggableShape (MIM_PHOSPHORYLATED), "mim-phosphorylated");
    @Deprecated
    public static final IShape MIM_DEGRADATION_SHAPE = new AbstractShape (getPluggableShape (MIM_DEGRADATION), "mim-degradation");
    @Deprecated
    public static final IShape MIM_INTERACTION_SHAPE = new AbstractShape (getPluggableShape (MIM_INTERACTION), "mim-interaction");
     
    public static void registerShapes()
	{
		ShapeRegistry.registerArrow (MIM_NECESSARY_STIMULATION.getName(), getMIMNecessary(), ArrowShape.FillType.OPEN, ARROWWIDTH);
		ShapeRegistry.registerArrow (MIM_BINDING.getName(), getMIMBinding(), ArrowShape.FillType.CLOSED);
		ShapeRegistry.registerArrow (MIM_CONVERSION.getName(), getMIMConversion(), ArrowShape.FillType.CLOSED, ARROWWIDTH);
		ShapeRegistry.registerArrow (MIM_STIMULATION.getName(), getMIMStimulation(), ArrowShape.FillType.OPEN, ARROWWIDTH);
        ShapeRegistry.registerArrow (MIM_MODIFICATION.getName(), getMIMBinding(), ArrowShape.FillType.CLOSED);
		ShapeRegistry.registerArrow (MIM_CATALYSIS.getName(), getMIMCatalysis(), ArrowShape.FillType.OPEN, CATALYSIS_DIAM + CATALYSIS_GAP);
        ShapeRegistry.registerArrow (MIM_CLEAVAGE.getName(), getMIMCleavage(), ArrowShape.FillType.WIRE,CLEAVAGE_FIRST);
        ShapeRegistry.registerArrow (MIM_BRANCHING_LEFT.getName(), getMIMBranching(LEFT), ArrowShape.FillType.OPEN, BRANCH_LOCATION);
        ShapeRegistry.registerArrow (MIM_BRANCHING_RIGHT.getName(), getMIMBranching(RIGHT), ArrowShape.FillType.OPEN, BRANCH_LOCATION);
		ShapeRegistry.registerArrow (MIM_INHIBITION.getName(), getMIMInhibition(),  ArrowShape.FillType.OPEN,TBARWIDTH + TBAR_GAP);
        ShapeRegistry.registerArrow (MIM_COVALENT_BOND.getName(), getMIMCovalentBond(), ArrowShape.FillType.OPEN);
        ShapeRegistry.registerArrow (MIM_TRANSLATION.getName(), getMIMTranslation(), ArrowShape.FillType.WIRE, ARROWWIDTH + ARROWHEIGHT);
        ShapeRegistry.registerArrow (MIM_GAP.getName(), getMIMGap(), ArrowShape.FillType.OPEN, 10);
	}

    private static final int BOND_SIZE = 8;

    static private java.awt.Shape getMIMCovalentBond ()
    {
        return new Rectangle2D.Double(
            -BOND_SIZE + 1, -BOND_SIZE/2,
            BOND_SIZE, BOND_SIZE
        );
    }

	//Cleavage line ending constants
	static final int CLEAVAGE_FIRST = 10;
	static final int CLEAVAGE_SECOND = 20;
	static final int CLEAVAGE_GAP = CLEAVAGE_SECOND - CLEAVAGE_FIRST;

	//Branch line ending constants
	 private static final int LEFT = 0;
	 private static final int RIGHT = 1;
	 private static final int BRANCH_LOCATION = 8;
	 private static final int BRANCHTHICKNESS = 1;

	 //method to create the MIM Branch RIGHT and LEFT line endings
	 // a 4 sided structure with small thickness works better tha
	 // a line.(Maybe the affine trasform has a issue with a line
	 //as opposed to a thin quadrilateral)
	static private java.awt.Shape getMIMBranching (int direction)
    {
		if (direction == RIGHT) {
			GeneralPath path = new GeneralPath();
			path.moveTo (0, 0);
			path.lineTo (BRANCH_LOCATION, -BRANCH_LOCATION);
			path.lineTo(BRANCH_LOCATION,-BRANCH_LOCATION  + BRANCHTHICKNESS);
			path.lineTo(BRANCHTHICKNESS, 0);
			path.closePath();
			return path;
		}
		else
		{
			GeneralPath path = new GeneralPath();
			path.moveTo (0, 0);
			path.lineTo (BRANCH_LOCATION, BRANCH_LOCATION);
			path.lineTo(BRANCH_LOCATION,BRANCH_LOCATION  - BRANCHTHICKNESS);
			path.lineTo(BRANCHTHICKNESS,0);
			path.closePath();
			return path;
		}
    }

	//method to create the MIM Cleavage lie ending
	static private java.awt.Shape getMIMCleavage ()
	{
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (0, -CLEAVAGE_FIRST);
		path.lineTo (CLEAVAGE_SECOND, CLEAVAGE_FIRST);
		return path;
	}

	static final int CATALYSIS_DIAM = 8;
	static final int CATALYSIS_GAP = CATALYSIS_DIAM/4;
	static final int CATALYSIS_GAP_HEIGHT = 6;
	//create the ellipse for catalysis line ending
	static private java.awt.Shape getMIMCatalysis ()
	{
		return new Ellipse2D.Double	(
			0, -CATALYSIS_DIAM/2,
			CATALYSIS_DIAM, CATALYSIS_DIAM);
	}

	private static final int ARROWHEIGHT = 4;
	private static final int ARROWWIDTH = 9;
	private static final int ARROW_NECESSARY_CROSSBAR = 6;

	private static GeneralPath getArrowShapedPath() {
		GeneralPath path = new GeneralPath();
		path.moveTo (0, -ARROWHEIGHT);
		path.lineTo (ARROWWIDTH, 0);
		path.lineTo (0, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getMIMStimulation () {
		return getArrowShapedPath();
	}

	static private java.awt.Shape getMIMBinding () {
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (-ARROWWIDTH, -ARROWHEIGHT);
		path.lineTo (-ARROWWIDTH / 2, 0);
		path.lineTo (-ARROWWIDTH, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getMIMConversion () {
		return getArrowShapedPath();
	}

	static private java.awt.Shape getMIMNecessary () {
		GeneralPath path = getArrowShapedPath();
		path.moveTo (-ARROW_NECESSARY_CROSSBAR, -ARROWHEIGHT);
		path.lineTo (-ARROW_NECESSARY_CROSSBAR, ARROWHEIGHT);
		return path;
	}

    final static int TAIL = ARROWWIDTH / 2;

    static private Shape getMIMTranslation() {
        GeneralPath path = new GeneralPath();
		path.moveTo (-TAIL, 0);
		path.lineTo (-TAIL, ARROWHEIGHT *2);
        path.lineTo (TAIL, ARROWHEIGHT * 2);
        path.lineTo (TAIL, ARROWHEIGHT * 3);
		path.lineTo (TAIL + ARROWWIDTH, ARROWHEIGHT * 2);
        path.lineTo (TAIL, ARROWHEIGHT);
        path.lineTo (TAIL, ARROWHEIGHT * 2);
        return path;
    }

    static private java.awt.Shape getMIMGap () {
        GeneralPath path = new GeneralPath();
        path.moveTo (0, 0);
        path.moveTo (0, 5);
        return path;
    }


    /**
	   Internal,
	   Only for general shape types that can be described as a path.
	   The shapes are constructed as a general path with arbitrary size
	   and then resized to fit w and h parameters.
	 */
	static private java.awt.Shape getPluggableShape (int st)
	{
		GeneralPath path = new GeneralPath();
		switch (st)
		{
		case MIM_DEGRADATION:
			path.moveTo (31.59f, 18.46f);
			path.curveTo (31.59f, 25.44f, 25.72f, 31.10f, 18.50f, 31.10f);
			path.curveTo (11.27f, 31.10f, 5.41f, 25.44f, 5.41f, 18.46f);
			path.curveTo (5.41f, 11.48f, 11.27f, 5.82f, 18.50f, 5.82f);
			path.curveTo (25.72f, 5.82f, 31.59f, 11.48f, 31.59f, 18.46f);
			path.closePath();
			path.moveTo (0.39f, 0.80f);
			path.curveTo (34.84f, 36.07f, 35.25f, 35.67f, 35.25f, 35.67f);
			break;
		case MIM_PHOSPHORYLATED:
			path.moveTo (5.79f, 4.72f);
			path.lineTo (5.79f, 18.18f);
			path.lineTo (13.05f, 18.18f);
			path.curveTo (15.74f, 18.18f, 17.81f, 17.60f, 19.28f, 16.43f);
			path.curveTo (20.75f, 15.26f, 21.48f, 13.60f, 21.48f, 11.44f);
			path.curveTo (21.48f, 9.29f, 20.75f, 7.64f, 19.28f, 6.47f);
			path.curveTo (17.81f, 5.30f, 15.74f, 4.72f, 13.05f, 4.72f);
			path.lineTo (5.79f, 4.72f);
			path.moveTo (0.02f, 0.73f);
			path.lineTo (13.05f, 0.73f);
			path.curveTo (17.83f, 0.73f, 21.44f, 1.65f, 23.88f, 3.47f);
			path.curveTo (26.34f, 5.28f, 27.57f, 7.93f, 27.57f, 11.44f);
			path.curveTo (27.57f, 14.98f, 26.34f, 17.65f, 23.88f, 19.46f);
			path.curveTo (21.44f, 21.26f, 17.83f, 22.17f, 13.05f, 22.17f);
			path.lineTo (5.79f, 22.17f);
			path.lineTo (5.79f, 36.57f);
			path.lineTo (0.02f, 36.57f);
			path.lineTo (0.02f, 0.73f);
			break;
		case MIM_INTERACTION:
			path.moveTo (30.90f, 15.20f);
			path.curveTo (30.90f, 23.18f, 24.02f, 29.65f, 15.55f, 29.65f);
			path.curveTo (7.08f, 29.65f, 0.20f, 23.18f, 0.20f, 15.20f);
			path.curveTo (0.20f, 7.23f, 7.08f, 0.76f, 15.55f, 0.76f);
			path.curveTo (24.02f, 0.76f, 30.90f, 7.23f, 30.90f, 15.20f);
			path.closePath();
			break;
		default:
			assert (false);
		}
		return path;
	}

	// copied from BasicShapes for T-Bar
	private static final int TBARHEIGHT = 15;
	private static final int TBARWIDTH = 1;
	private static final int TBAR_GAP = 6;

	// copied from BasicShapes.getTBar()
	private static Shape getMIMInhibition()
	{
		return new Rectangle2D.Double(
			0, -TBARHEIGHT / 2,
			TBARWIDTH, TBARHEIGHT
			);
	}

}