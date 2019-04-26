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
package org.pathvisio.core.view;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.pathvisio.core.model.AbstractShape;
import org.pathvisio.core.model.IShape;
import org.pathvisio.core.model.LineType;

/**
 * Collection of Shapes and ArrowHeads used in
 * the WikiPathways Relationships (aka interactions)
 * @author DeniseSl22
 */
public class WPShapes
{
	public static final LineType WP_CONVERSION = LineType.create ("mim-conversion", "Arrow");
	public static final LineType WP_CATALYSIS =LineType.create ("mim-catalysis", "Arrow");
	public static final LineType WP_INHIBITION = LineType.create ("mim-inhibition", "Arrow");
	public static final LineType WP_MODIFICATION = LineType.create ("mim-modification", "Arrow"); 
	//Basic Directed relationship can be reused from Basic interaction panel
	public static final LineType WP_STIMULATION = LineType.create ("mim-stimulation", "Arrow");
	public static final LineType WP_BINDING = LineType.create ("mim-binding", "Arrow");
	public static final LineType WP_TRANSLATION = LineType.create ("mim-transcription-translation", "Arrow");
	public static final LineType WP_TRANSLOCATION = LineType.create ("mim-translocation", "Arrow");
	//Basic UNDirected relationship can be reused from Basic interaction panel
	
 
    public static void registerShapes()
	{
    	ShapeRegistry.registerArrow (WP_CONVERSION.getName(), getWPConversion(), ArrowShape.FillType.CLOSED, ARROWWIDTH);
    	ShapeRegistry.registerArrow (WP_CATALYSIS.getName(), getWPCatalysis(), ArrowShape.FillType.OPEN, CATALYSIS_DIAM + CATALYSIS_GAP);
    	ShapeRegistry.registerArrow (WP_INHIBITION.getName(), getWPInhibition(),  ArrowShape.FillType.OPEN,TBARWIDTH + TBAR_GAP);
    	ShapeRegistry.registerArrow (WP_MODIFICATION.getName(), getWPBinding(), ArrowShape.FillType.CLOSED);
    	
    	//Basic Directed relationship can be reused from Basic interaction panel
    	
    	ShapeRegistry.registerArrow (WP_STIMULATION.getName(), getWPStimulation(), ArrowShape.FillType.OPEN, ARROWWIDTH);
    	ShapeRegistry.registerArrow (WP_BINDING.getName(), getWPBinding(), ArrowShape.FillType.CLOSED);
     	ShapeRegistry.registerArrow (WP_TRANSLATION.getName(), getWPTranslation(), ArrowShape.FillType.WIRE, ARROWWIDTH + ARROWHEIGHT);		
		ShapeRegistry.registerArrow (WP_TRANSLOCATION.getName(), getWPTranslocation(), ArrowShape.FillType.CLOSED, ARROWWIDTH);
		
		//Basic UNDirected relationship can be reused from Basic interaction panel

	}


	//Cleavage line ending constants
	static final int CLEAVAGE_FIRST = 10;
	static final int CLEAVAGE_SECOND = 20;
	static final int CLEAVAGE_GAP = CLEAVAGE_SECOND - CLEAVAGE_FIRST;
	static final int CATALYSIS_DIAM = 8;
	static final int CATALYSIS_GAP = CATALYSIS_DIAM/4;
	static final int CATALYSIS_GAP_HEIGHT = 6;
	
	//create the ellipse for catalysis line ending
	static private java.awt.Shape getWPCatalysis ()
	{
		return new Ellipse2D.Double	(
			0, -CATALYSIS_DIAM/2,
			CATALYSIS_DIAM, CATALYSIS_DIAM);
	}

	private static final int ARROWHEIGHT = 4;
	private static final int ARROWWIDTH = 9;
//	private static final int ARROW_NECESSARY_CROSSBAR = 6;

	private static GeneralPath getArrowShapedPath() {
		GeneralPath path = new GeneralPath();
		path.moveTo (0, -ARROWHEIGHT);
		path.lineTo (ARROWWIDTH, 0);
		path.lineTo (0, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getWPStimulation () {
		return getArrowShapedPath();
	}

	static private java.awt.Shape getWPBinding () {
		GeneralPath path = new GeneralPath();
		path.moveTo (0, 0);
		path.lineTo (-ARROWWIDTH, -ARROWHEIGHT);
		path.lineTo (-ARROWWIDTH / 2, 0);
		path.lineTo (-ARROWWIDTH, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getWPConversion () {
		return getArrowShapedPath();
	}
	
	static private java.awt.Shape getWPTranslocation () {
		return getArrowShapedPath();
	}

    final static int TAIL = ARROWWIDTH / 2;

    static private Shape getWPTranslation() {
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

 

	// copied from BasicShapes for T-Bar
	private static final int TBARHEIGHT = 15;
	private static final int TBARWIDTH = 1;
	private static final int TBAR_GAP = 6;

	// copied from BasicShapes.getTBar()
	private static Shape getWPInhibition()
	{
		return new Rectangle2D.Double(
			0, -TBARHEIGHT / 2,
			TBARWIDTH, TBARHEIGHT
			);
	}

}