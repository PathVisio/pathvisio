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
package org.pathvisio.model;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.jdom.Document;
import org.pathvisio.biopax.BiopaxReferenceManager;
import org.pathvisio.model.GraphLink.GraphIdContainer;
import org.pathvisio.model.GraphLink.GraphRefContainer;
import org.pathvisio.preferences.GlobalPreference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.Utils;

/**
 * PathwayElement is responsible for maintaining the data for all the individual
 * objects that can appear on a pwy (Lines, GeneProducts, Shapes, etc.)
 * <p>
 * All PathwayElements have an ObjectType. This ObjectType is specified at creation
 * time and can't be modified. To create a PathwayElement,
 * use the createPathwayElement() function. This is a factory method
 * that returns a different implementation class depending on
 * the specified ObjectType.
 * <p>
 * PathwayElements have a number of properties which consist of a
 * key, value pair.
 * <p>
 * There are two types of properties: Static and Dynamic
 * Static properties are one of the properties
 * <p>
 * Dynamic properties can have any String as key. Their value is
 * always of type String. Dynamic properties are not essential for
 * the functioning of PathVisio and can be used to
 * store arbitrary data. In GPML, dynamic properties are
 * stored in an <Attribute key="" value=""/> tag.
 * Internally, dynamic properties are stored in a Map<String, String>
 * <p>
 * Static properties must have a key from the StaticProperty enum
 * Their value can be various types which can be
 * obtained from StaticProperty.type(). Static properties can
 * be queried with getStaticProperty (key) and
 * setStaticProperty(key, value), but also specific accessors
 * such as e.g. getTextLabel() and setTextLabel()
 * <p>
 * Internally, dynamic properties are stored in various
 * fields of the PathwayElement Object.
 * The static properties are a union of all possible fields
 * (e.g it has both start and endpoints for lines,
 * and label text for labels)
 * <p>
 * the setPropertyEx() and getPropertyEx() functions can be used
 * to access both dynamic and static properties
 * from the same function. If key instanceof String then it's
 * assumed the caller wants a dynamic
 * property, if key instanceof StaticProperty then the static property
 * is used.
 * <p>
 * most static properties cannot be set to null. Notable exceptions are graphId,
 * startGraphRef and endGraphRef.
 */
public class PathwayElement implements GraphIdContainer, Comparable<PathwayElement>
{
	//TreeMap has better performance than HashMap
	//in the (common) case where no attributes are present
	//This map should never contain non-null values, if a value
	//is set to null the key should be removed.
	private Map<String, String> attributes = new TreeMap<String, String>();

	/**
	 * get a set of all dynamic property keys
	 */
	public Set<String> getDynamicPropertyKeys()
	{
		return attributes.keySet();
	}

	/**
	 * set a dynamic property.
	 * Setting to null means removing this dynamic property altogether
	 */
	public void setDynamicProperty (String key, String value)
	{
		if (value == null)
			attributes.remove(key);
		else
			attributes.put (key, value);
		fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, key));
	}

	/**
	 * get a dynamic property
	 */
	public String getDynamicProperty (String key)
	{
		return attributes.get (key);
	}

	/**
	 * A comment in a pathway element: each
	 * element can have zero or more comments with it, and
	 * each comment has a source and a text.
	 */
	public class Comment implements Cloneable
	{
		public Comment(String aComment, String aSource)
		{
			source = aSource;
			comment = aComment;
		}

		public Object clone() throws CloneNotSupportedException
		{
			return super.clone();
		}

		private String source;
		private String comment;

		public String getSource() { return source; }
		public String getComment() { return comment; }

		public void setSource(String s) {
			if(s != null && !source.equals(s)) {
				source = s;
				changed();
			}
		}

		public void setComment(String c) {
			if(c != null && !comment.equals(c)) {
				comment = c;
				changed();
			}
		}

		private void changed() {
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(PathwayElement.this, StaticProperty.COMMENTS));
		}

		public String toString() {
			String src = "";
			if(source != null && !"".equals(source)) {
				src = " (" + source + ")";
			}
			return comment + src;
		}
	}

	/**
	 * Represents a generic point in an coordinates.length dimensional space.
	 * The point is automatically a {@link GraphIdContainer} and therefore lines
	 * can link to the point.
	 * @see MPoint
	 * @see MAnchor
	 * @author thomas
	 *
	 */
	private abstract class GenericPoint implements Cloneable, GraphIdContainer
	{
		private double[] coordinates;

		private String graphId;

		GenericPoint(double[] coordinates)
		{
			this.coordinates = coordinates;
		}

		GenericPoint(GenericPoint p)
		{
			coordinates = new double[p.coordinates.length];
			System.arraycopy(p.coordinates, 0, coordinates, 0, coordinates.length);
			if (p.graphId != null)
				graphId = p.graphId;
		}

		protected void moveBy(double[] delta)
		{
			for(int i = 0; i < coordinates.length; i++) {
				coordinates[i] += delta[i];
			}
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));
		}

		protected void moveTo(double[] coordinates)
		{
			this.coordinates = coordinates;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));
		}

		protected void moveTo(GenericPoint p)
		{
			coordinates = p.coordinates;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));;
		}

		protected double getCoordinate(int i) {
			return coordinates[i];
		}

		public String getGraphId()
		{
			return graphId;
		}

		public String setGeneratedGraphId()
		{
			setGraphId(parent.getUniqueGraphId());
			return graphId;
		}

		public void setGraphId(String v)
		{
			GraphLink.setGraphId(v, this, PathwayElement.this.parent);
			graphId = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(PathwayElement.this, StaticProperty.GRAPHID));
		}

		public Object clone() throws CloneNotSupportedException
		{
			GenericPoint p = (GenericPoint) super.clone();
			if (graphId != null)
				p.graphId = graphId;
			return p;
		}

		public Set<GraphRefContainer> getReferences()
		{
			return GraphLink.getReferences(this, parent);
		}

		public Pathway getPathway() {
			return parent;
		}

		public PathwayElement getParent()
		{
			return PathwayElement.this;
		}
	}

	/**
	 * This class represents the Line.Graphics.Point element in GPML.
	 * @author thomas
	 *
	 */
	public class MPoint extends GenericPoint implements GraphRefContainer
	{
		private String graphRef;
		private boolean relativeSet;

		public MPoint(double x, double y)
		{
			super(new double[] { x, y, 0, 0 });
		}

		MPoint(MPoint p)
		{
			super(p);
			if (p.graphRef != null)
				graphRef = p.graphRef;
		}

		public void moveBy(double dx, double dy)
		{
			super.moveBy(new double[] { dx, dy, 0, 0 });
		}

		public void moveTo(double x, double y)
		{
			super.moveTo(new double[] { x, y, 0, 0 });
		}

		public void setX(double nx)
		{
			if (nx != getX())
				moveBy(nx - getX(), 0);
		}

		public void setY(double ny)
		{
			if (ny != getY())
				moveBy(0, ny - getY());
		}

		public double getX()
		{
			if(isRelative()) {
				return getAbsolute().getX();
			} else {
				return getCoordinate(0);
			}
		}

		public double getY()
		{
			if(isRelative()) {
				return getAbsolute().getY();
			} else {
				return getCoordinate(1);
			}
		}

		protected double getRawX() {
			return getCoordinate(0);
		}

		protected double getRawY() {
			return getCoordinate(1);
		}

		public double getRelX() {
			return getCoordinate(2);
		}

		public double getRelY() {
			return getCoordinate(3);
		}

		private Point2D getAbsolute() {
			return getGraphIdContainer().toAbsoluteCoordinate(
					new Point2D.Double(getRelX(), getRelY())
			);
		}


		public void setRelativePosition(double rx, double ry) {
			moveTo(new double[] { getX(), getY(), rx, ry });
			relativeSet = true;
		}

		/**
		 * Checks if the position of this point should be stored
		 * as relative or absolute coordinates
		 * @return true if the coordinates are relative, false if not
		 */
		public boolean isRelative() {
			Pathway p = getPathway();
			if(p != null) {
				GraphIdContainer gc = getPathway().getGraphIdContainer(graphRef);
				return gc != null;
			}
			return false;
		}

		/**
		 * Helper method for converting older GPML files without
		 * relative coordinates.
		 * @return true if {@link #setRelativePosition(double, double)} was called to
		 * set the relative coordinates, false if not.
		 */
		protected boolean relativeSet() {
			return relativeSet;
		}

		private GraphIdContainer getGraphIdContainer() {
			return getPathway().getGraphIdContainer(graphRef);
		}

		public String getGraphRef()
		{
			return graphRef;
		}

		/**
		 * Set a reference to another object with a graphId. If a parent is set,
		 * this will automatically deregister the previously held reference and
		 * register the new reference as necessary
		 *
		 * @param v
		 *            reference to set.
		 */
		public void setGraphRef(String v)
		{
			if (!Utils.stringEquals(graphRef, v))
			{
				if (parent != null)
				{
					if (graphRef != null)
					{
						parent.removeGraphRef(graphRef, this);
					}
					if (v != null)
					{
						parent.addGraphRef(v, this);
					}
				}
				graphRef = v;
			}
		}

		public Object clone() throws CloneNotSupportedException
		{
			MPoint p = (MPoint) super.clone();
			if (graphRef != null)
				p.graphRef = graphRef;
			return p;
		}

		public Point2D toPoint2D() {
			return new Point2D.Double(getX(), getY());
		}

		/**
		 * Link to an object. Current absolute coordinates
		 * will be converted to relative coordinates based on the
		 * object to link to.
		 */
		public void linkTo(GraphIdContainer idc) {
			Point2D rel = idc.toRelativeCoordinate(toPoint2D());
			linkTo(idc, rel.getX(), rel.getY());
		}

		/**
		 * Link to an object using the given relative coordinates
		 */
		public void linkTo(GraphIdContainer idc, double relX, double relY) {
			String id = idc.getGraphId();
			if(id == null) id = idc.setGeneratedGraphId();
			setGraphRef(idc.getGraphId());
			setRelativePosition(relX, relY);
		}

		/** note that this may be called any number of times when this point is already unlinked */
		public void unlink()
		{
			if (graphRef != null)
			{
				if (getPathway() != null)
				{
					Point2D abs = getAbsolute();
					moveTo(abs.getX(), abs.getY());
				}
				relativeSet = false;
				setGraphRef(null);
				fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));
			}
		}

		public Point2D toAbsoluteCoordinate(Point2D p) {
			return new Point2D.Double(p.getX() + getX(), p.getY() + getY());
		}

		public Point2D toRelativeCoordinate(Point2D p) {
			return new Point2D.Double(p.getX() - getX(), p.getY() - getY());
		}

		/**
		 * Find out if this point is linked to an object.
		 * Returns true if a graphRef exists and is not an empty string
		 */
		public boolean isLinked() {
			String ref = getGraphRef();
			return ref != null && !"".equals(ref);
		}

		public void refeeChanged()
		{
			// called whenever the object being referred to has changed.
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));
		}
	}

	/**
	 * This class represents the Line.Graphics.Anchor element in GPML
	 * @author thomas
	 *
	 */
	public class MAnchor extends GenericPoint {
		AnchorType shape = AnchorType.NONE;

		public MAnchor(double position) {
			super(new double[] { position });
		}

		public MAnchor(MAnchor a) {
			super(a);
			shape = a.shape;
		}

		public void setShape(AnchorType type) {
			if(!this.shape.equals(type) && type != null) {
				this.shape = type;
				fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(PathwayElement.this, StaticProperty.LINESTYLE));
			}
		}

		public AnchorType getShape() {
			return shape;
		}

		public double getPosition() {
			return getCoordinate(0);
		}

		public void setPosition(double position) {
			if(position != getPosition()) {
				moveBy(position - getPosition());
			}
		}

		public void moveBy(double delta) {
			super.moveBy(new double[] { delta });
		}

		public Point2D toAbsoluteCoordinate(Point2D p) {
			Point2D l = ((MLine)getParent()).getConnectorShape().fromLineCoordinate(getPosition());
			return new Point2D.Double(p.getX() + l.getX(), p.getY() + l.getY());
		}

		public Point2D toRelativeCoordinate(Point2D p) {
			Point2D l = ((MLine)getParent()).getConnectorShape().fromLineCoordinate(getPosition());
			return new Point2D.Double(p.getX() - l.getX(), p.getY() - l.getY());
		}
	}

	private static final int M_INITIAL_LINE_LENGTH = 30; 

	private static final int M_INITIAL_STATE_SIZE = 15;
	
	private static final int M_INITIAL_SHAPE_SIZE = 30;
	
	private static final int M_INITIAL_CELLCOMP_HEIGHT = 100;

	private static final int M_INITIAL_CELLCOMP_WIDTH = 200;

	private static final int M_INITIAL_BRACE_HEIGHT = 15;

	private static final int M_INITIAL_BRACE_WIDTH = 60;

	private static final int M_INITIAL_GENEPRODUCT_WIDTH = 80;

	private static final int M_INITIAL_GENEPRODUCT_HEIGHT = 20;

	// groups should be behind other graphics
	// to allow background colors
	private static final int Z_ORDER_GROUP = 0x1000;
	// default order of geneproduct, label, shape and line determined
	// by GenMAPP legacy
	private static final int Z_ORDER_GENEPRODUCT = 0x8000;
	private static final int Z_ORDER_LABEL = 0x7000;
	private static final int Z_ORDER_SHAPE = 0x4000;
	private static final int Z_ORDER_LINE = 0x3000;
	// default order of uninteresting elements.
	private static final int Z_ORDER_DEFAULT = 0x0000;

	/**
	 * default z order for newly created objects
	 */
	private static int getDefaultZOrder(ObjectType value)
	{
		switch (value)
		{
		case SHAPE:
			return Z_ORDER_SHAPE;
		case STATE:
			return Z_ORDER_GENEPRODUCT + 10;
		case DATANODE:
			return Z_ORDER_GENEPRODUCT;
		case LABEL:
			return Z_ORDER_LABEL;
		case LINE:
			return Z_ORDER_LINE;
		case LEGEND:
		case INFOBOX:
		case MAPPINFO:
		case BIOPAX:
			return Z_ORDER_DEFAULT;
		case GROUP:
			return Z_ORDER_GROUP;
		default:
			throw new IllegalArgumentException("Invalid object type " + value);
		}
	}

	/**
	 * Instantiate a pathway element.
	 * The required parameter objectType ensures only objects with a valid type
	 * can be created.
	 *
	 * @param ot
	 *            Type of object, one of the ObjectType.* fields
	 */
	public static PathwayElement createPathwayElement(ObjectType ot) {
		PathwayElement e;
		switch (ot) {
		case GROUP:
			e = new MGroup();
			break;
		case LINE:
			e = new MLine();
			break;
		case STATE:
			e = new MState();
			break;
		default:
			e = new PathwayElement(ot);
			break;
		}
		return e;
	}

	protected PathwayElement(ObjectType ot)
	{
		/* set default value for transparency */
		if (ot == ObjectType.LINE || ot == ObjectType.LABEL || ot == ObjectType.DATANODE || ot == ObjectType.STATE)
		{
			fillColor = Color.WHITE;
		}
		else
		{
			fillColor = null;
		}
		/* set default value for shapeType */
		if (ot == ObjectType.LABEL)
		{
			shapeType = ShapeType.NONE;
		}
		else
		{
			shapeType = ShapeType.RECTANGLE;
		}
		
		objectType = ot;
		zOrder = getDefaultZOrder (ot);
	}

	int zOrder;

	public int getZOrder()
	{
		return zOrder;
	}

	public void setZOrder(int z) {
		if(z != zOrder) {
			zOrder = z;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.ZORDER));
		}
	}

	/**
	 * Parent of this object: may be null (for example, when object is in
	 * clipboard)
	 */
	protected Pathway parent = null;

	public Pathway getParent()
	{
		return parent;
	}

	/**
	 * Get the parent pathway. Same as {@link #getParent()}, but necessary to
	 * comply to the {@link GraphIdContainer} interface.
	 */
	public Pathway getPathway() {
		return parent;
	}

	/**
	 * Set parent. Do not use this method directly! parent is set automatically
	 * when using Pathway.add/remove
	 * @param v the parent
	 */
	void setParent(Pathway v)
	{
		parent = v;
	}

	/**
	 * Returns keys of available static properties and dynamic properties as an object list
	 */
	public Set<Object> getPropertyKeys()
	{
		Set<Object> keys = new HashSet<Object>();
		keys.addAll(getStaticPropertyKeys());
		keys.addAll(getDynamicPropertyKeys());
		return keys;
	}

	private static final Map<ObjectType, Set<StaticProperty>> ALLOWED_PROPS;

	static {
		Set<StaticProperty> propsCommon = EnumSet.of(
				StaticProperty.COMMENTS,
				StaticProperty.GRAPHID,
				StaticProperty.GROUPREF,
				StaticProperty.BIOPAXREF,
				StaticProperty.ZORDER
			);
		Set<StaticProperty> propsCommonShape = EnumSet.of(
				StaticProperty.CENTERX,
				StaticProperty.CENTERY,
				StaticProperty.WIDTH,
				StaticProperty.HEIGHT,
				StaticProperty.COLOR
			);
		Set<StaticProperty> propsCommonStyle = EnumSet.of(
				StaticProperty.TEXTLABEL,
				StaticProperty.FONTNAME,
				StaticProperty.FONTWEIGHT,
				StaticProperty.FONTSTYLE,
				StaticProperty.FONTSIZE,
				StaticProperty.ALIGN,
				StaticProperty.VALIGN,
				StaticProperty.COLOR,
				StaticProperty.FILLCOLOR,
				StaticProperty.TRANSPARENT,
				StaticProperty.SHAPETYPE,
				StaticProperty.LINETHICKNESS,
				StaticProperty.LINESTYLE
			);
		ALLOWED_PROPS = new EnumMap<ObjectType, Set<StaticProperty>>(ObjectType.class);
		{
			Set<StaticProperty> propsMappinfo = EnumSet.of (
					StaticProperty.COMMENTS,
					StaticProperty.MAPINFONAME,
					StaticProperty.ORGANISM,
					StaticProperty.MAPINFO_DATASOURCE,
					StaticProperty.VERSION,
					StaticProperty.AUTHOR,
					StaticProperty.MAINTAINED_BY,
					StaticProperty.EMAIL,
					StaticProperty.LAST_MODIFIED,
					StaticProperty.LICENSE,
					StaticProperty.BOARDWIDTH,
					StaticProperty.BOARDHEIGHT
				);
			ALLOWED_PROPS.put (ObjectType.MAPPINFO, propsMappinfo);
		}
		{
			Set<StaticProperty> propsState = EnumSet.of(
					StaticProperty.RELX,
					StaticProperty.RELY,
					StaticProperty.WIDTH,
					StaticProperty.HEIGHT,
					StaticProperty.MODIFICATIONTYPE,
					StaticProperty.GRAPHREF
				);
			propsState.addAll (propsCommon);
			propsState.addAll (propsCommonStyle);
			ALLOWED_PROPS.put (ObjectType.STATE, propsState);
		}
		{
			Set<StaticProperty> propsShape = EnumSet.of(
					StaticProperty.FILLCOLOR,
					StaticProperty.SHAPETYPE,
					StaticProperty.ROTATION,
					StaticProperty.TRANSPARENT,
					StaticProperty.LINESTYLE
				);
			propsShape.addAll (propsCommon);
			propsShape.addAll (propsCommonStyle);
			propsShape.addAll (propsCommonShape);
			ALLOWED_PROPS.put (ObjectType.SHAPE, propsShape);
		}
		{
			Set<StaticProperty> propsDatanode = EnumSet.of (
					StaticProperty.GENEID,
					StaticProperty.DATASOURCE,
					StaticProperty.TEXTLABEL,
					StaticProperty.TYPE
				);
			propsDatanode.addAll (propsCommon);
			propsDatanode.addAll (propsCommonStyle);
			propsDatanode.addAll (propsCommonShape);
			ALLOWED_PROPS.put (ObjectType.DATANODE, propsDatanode);
		}
		{
			Set<StaticProperty> propsLine = EnumSet.of(
					StaticProperty.COLOR,
					StaticProperty.STARTX,
					StaticProperty.STARTY,
					StaticProperty.ENDX,
					StaticProperty.ENDY,
					StaticProperty.STARTLINETYPE,
					StaticProperty.ENDLINETYPE,
					StaticProperty.LINESTYLE,
					StaticProperty.LINETHICKNESS,
					StaticProperty.STARTGRAPHREF,
					StaticProperty.ENDGRAPHREF
				);
			propsLine.addAll (propsCommon);
			ALLOWED_PROPS.put (ObjectType.LINE, propsLine);
		}
		{
			Set<StaticProperty> propsLabel = EnumSet.of (
					StaticProperty.HREF
				);
			propsLabel.addAll (propsCommon);
			propsLabel.addAll (propsCommonStyle);
			propsLabel.addAll (propsCommonShape);
			ALLOWED_PROPS.put (ObjectType.LABEL, propsLabel);
		}
		{
			Set<StaticProperty> propsGroup = EnumSet.of(
					StaticProperty.GROUPID,
					StaticProperty.GROUPREF,
					StaticProperty.BIOPAXREF,
					StaticProperty.GROUPSTYLE,
					StaticProperty.TEXTLABEL,
					StaticProperty.COMMENTS,
					StaticProperty.ZORDER
				);
			ALLOWED_PROPS.put (ObjectType.GROUP, propsGroup);
		}
		{
			Set<StaticProperty> propsInfobox = EnumSet.of(
					StaticProperty.CENTERX,
					StaticProperty.CENTERY,
					StaticProperty.ZORDER
				);
			ALLOWED_PROPS.put (ObjectType.INFOBOX, propsInfobox);
		}
		{
			Set<StaticProperty> propsLegend = EnumSet.of(
					StaticProperty.CENTERX,
					StaticProperty.CENTERY,
					StaticProperty.ZORDER
				);
			ALLOWED_PROPS.put (ObjectType.LEGEND, propsLegend);
		}
		{
			Set<StaticProperty> propsBiopax = EnumSet.noneOf(StaticProperty.class);
			ALLOWED_PROPS.put(ObjectType.BIOPAX, propsBiopax);
		}
	};



	/**
	 * get all attributes that are stored as static members.
	 */
	public Set<StaticProperty> getStaticPropertyKeys()
	{
		return ALLOWED_PROPS.get (getObjectType());
	}

	/**
	 * Set dynamic or static properties at the same time
	 * Will be replaced with setProperty in the future.
	 */
	public void setPropertyEx (Object key, Object value)
	{
		if (key instanceof StaticProperty)
		{
			setStaticProperty((StaticProperty)key, value);
		}
		else if (key instanceof String)
		{
			setDynamicProperty((String)key, value.toString());
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	public Object getPropertyEx (Object key)
	{
		if (key instanceof StaticProperty)
		{
			return getStaticProperty((StaticProperty)key);
		}
		else if (key instanceof String)
		{
			return getDynamicProperty ((String)key);
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	/**
	 * This works so that o.setNotes(x) is the equivalent of
	 * o.setProperty("Notes", x);
	 *
	 * Value may be null in some cases, e.g. graphRef
	 *
	 * @param key
	 * @param value
	 */
	public void setStaticProperty(StaticProperty key, Object value)
	{
		if (!getStaticPropertyKeys().contains(key))
			throw new IllegalArgumentException("Property " + key.name() + " is not allowed for objects of type " + getObjectType());
		switch (key)
		{
		case COMMENTS:
			setComments((List<Comment>) value);
			break;
		case COLOR:
			setColor((Color) value);
			break;

		case CENTERX:
			setMCenterX((Double) value);
			break;
		case CENTERY:
			setMCenterY((Double) value);
			break;
		case WIDTH:
			setMWidth((Double) value);
			break;
		case HEIGHT:
			setMHeight((Double) value);
			break;

		case FILLCOLOR:
			setFillColor((Color) value);
			break;
		case SHAPETYPE:
			setShapeType((ShapeType)value);
			break;
		case ROTATION:
			setRotation((Double) value);
			break;
		case RELX:
			setRelX((Double) value);
			break;
		case RELY:
			setRelY((Double) value);
			break;
		case STARTX:
			setMStartX((Double) value);
			break;
		case STARTY:
			setMStartY((Double) value);
			break;
		case ENDX:
			setMEndX((Double) value);
			break;
		case ENDY:
			setMEndY((Double) value);
			break;
		case ENDLINETYPE:
			if(value instanceof LineType)
				setEndLineType((LineType)value);
			else
				setEndLineType(LineType.fromOrdinal ((Integer) value));
			break;
		case STARTLINETYPE:
			if(value instanceof LineType)
				setStartLineType((LineType)value);
			else
				setStartLineType(LineType.fromOrdinal ((Integer) value));
			break;
		case LINESTYLE:
			setLineStyle((Integer) value);
			break;

		case ORIENTATION:
			setOrientation((Integer) value);
			break;

		case GENEID:
			setGeneID((String) value);
			break;
		case DATASOURCE:
			if (value instanceof DataSource)
			{
				setDataSource((DataSource) value);
			}
			else
			{
				setDataSource(DataSource.getByFullName((String)value));
			}
			break;
		case TYPE:
			setDataNodeType((String) value);
			break;

		case TEXTLABEL:
			setTextLabel((String) value);
			break;
		case HREF:
			setHref ((String) value);
			break;
		case FONTNAME:
			setFontName((String) value);
			break;
		case FONTWEIGHT:
			setBold((Boolean) value);
			break;
		case FONTSTYLE:
			setItalic((Boolean) value);
			break;
		case FONTSIZE:
			setMFontSize((Double) value);
			break;
		case MAPINFONAME:
			setMapInfoName((String) value);
			break;
		case ORGANISM:
			setOrganism((String) value);
			break;
		case MAPINFO_DATASOURCE:
			setMapInfoDataSource((String)value);
			break;
		case VERSION:
			setVersion((String) value);
			break;
		case AUTHOR:
			setAuthor((String) value);
			break;
		case MAINTAINED_BY:
			setMaintainer((String) value);
			break;
		case EMAIL:
			setEmail((String) value);
			break;
		case LAST_MODIFIED:
			setLastModified((String) value);
			break;
		case LICENSE:
			setCopyright((String) value);
			break;
		case BOARDWIDTH:
			//ignore, board width is calculated automatically
			break;
		case BOARDHEIGHT:
			//ignore, board width is calculated automatically
			break;
		case GRAPHID:
			setGraphId((String) value);
			break;
		case STARTGRAPHREF:
			setStartGraphRef((String) value);
			break;
		case ENDGRAPHREF:
			setEndGraphRef((String) value);
			break;
		case GROUPID:
			setGroupId((String) value);
			break;
		case GROUPREF:
			setGroupRef((String) value);
			break;
		case TRANSPARENT:
			setTransparent((Boolean) value);
			break;

		case BIOPAXREF:
			setBiopaxRefs((List<String>) value);
			break;
		case ZORDER:
			setZOrder((Integer)value);
			break;
		case GROUPSTYLE:
			if(value instanceof GroupStyle) {
				setGroupStyle((GroupStyle)value);
			} else {
				setGroupStyle(GroupStyle.fromName((String)value));
			}
			break;
		case ALIGN:
			setAlign ((AlignType) value);
			break;
		case VALIGN:
			setValign ((ValignType) value);
			break;
		case LINETHICKNESS:
			setLineThickness((Double) value);
			break;
		}
	}

	public Object getStaticProperty(StaticProperty key)
	{
		if (!getStaticPropertyKeys().contains(key))
			throw new IllegalArgumentException("Property " + key.name() + " is not allowed for objects of type " + getObjectType());
		Object result = null;
		switch (key)
		{
		case COMMENTS:
			result = getComments();
			break;
		case COLOR:
			result = getColor();
			break;

		case CENTERX:
			result = getMCenterX();
			break;
		case CENTERY:
			result = getMCenterY();
			break;
		case WIDTH:
			result = getMWidth();
			break;
		case HEIGHT:
			result = getMHeight();
			break;

		case FILLCOLOR:
			result = getFillColor();
			break;
		case SHAPETYPE:
			result = getShapeType();
			break;
		case ROTATION:
			result = getRotation();
			break;
		case RELX:
			result = getRelX();
			break;
		case RELY:
			result = getRelY();
			break;
		case STARTX:
			result = getMStartX();
			break;
		case STARTY:
			result = getMStartY();
			break;
		case ENDX:
			result = getMEndX();
			break;
		case ENDY:
			result = getMEndY();
			break;
		case ENDLINETYPE:
			result = getEndLineType();
			break;
		case STARTLINETYPE:
			result = getStartLineType();
			break;
		case LINESTYLE:
			result = getLineStyle();
			break;

		case ORIENTATION:
			result = getOrientation();
			break;

		case GENEID:
			result = getGeneID();
			break;
		case DATASOURCE:
			result = getDataSource();
			break;
		case TYPE:
			result = getDataNodeType();
			break;

		case TEXTLABEL:
			result = getTextLabel();
			break;
		case HREF:
			result = getHref();
			break;
		case FONTNAME:
			result = getFontName();
			break;
		case FONTWEIGHT:
			result = isBold();
			break;
		case FONTSTYLE:
			result = isItalic();
			break;
		case FONTSIZE:
			result = getMFontSize();
			break;

		case MAPINFONAME:
			result = getMapInfoName();
			break;
		case ORGANISM:
			result = getOrganism();
			break;
		case MAPINFO_DATASOURCE:
			result = getMapInfoDataSource();
			break;
		case VERSION:
			result = getVersion();
			break;
		case AUTHOR:
			result = getAuthor();
			break;
		case MAINTAINED_BY:
			result = getMaintainer();
			break;
		case EMAIL:
			result = getEmail();
			break;
		case LAST_MODIFIED:
			result = getLastModified();
			break;
		case LICENSE:
			result = getCopyright();
			break;
		case BOARDWIDTH:
			result = getMBoardSize()[0];
			break;
		case BOARDHEIGHT:
			result = getMBoardSize()[1];
			break;
		case GRAPHID:
			result = getGraphId();
			break;
		case STARTGRAPHREF:
			result = getStartGraphRef();
			break;
		case ENDGRAPHREF:
			result = getEndGraphRef();
			break;
		case GROUPID:
			result = createGroupId();
			break;
		case GROUPREF:
			result = getGroupRef();
			break;
		case TRANSPARENT:
			result = isTransparent();
			break;
		case BIOPAXREF:
			result = getBiopaxRefs();
			break;
		case ZORDER:
			result = getZOrder();
			break;
		case GROUPSTYLE:
			result = getGroupStyle().toString();
			break;
		case ALIGN:
			result = getAlign();
			break;
		case VALIGN:
			result = getValign();
			break;
		case LINETHICKNESS:
			result = getLineThickness();
			break;		
		}

		return result;
	}

	/**
	 * Note: doesn't change parent, only fields
	 *
	 * Used by UndoAction.
	 *
	 * @param src
	 */
	public void copyValuesFrom(PathwayElement src)
	{
		attributes = new TreeMap<String, String>(src.attributes); // create copy
		author = src.author;
		copyright = src.copyright;
		mCenterx = src.mCenterx;
		mCentery = src.mCentery;
		relX = src.relX;
		relY = src.relY;
		zOrder =  src.zOrder;
		color = src.color;
		fillColor = src.fillColor;
		dataSource = src.dataSource;
		email = src.email;
		fontName = src.fontName;
		mFontSize = src.mFontSize;
		fBold = src.fBold;
		fItalic = src.fItalic;
		fStrikethru = src.fStrikethru;
		fUnderline = src.fUnderline;
		setGeneID = src.setGeneID;
		dataNodeType = src.dataNodeType;
		mHeight = src.mHeight;
		textLabel = src.textLabel;
		href = src.href;
		lastModified = src.lastModified;
		lineStyle = src.lineStyle;
		startLineType = src.startLineType;
		endLineType = src.endLineType;
		maintainer = src.maintainer;
		mapInfoDataSource = src.mapInfoDataSource;
		mapInfoName = src.mapInfoName;
		organism = src.organism;
		rotation = src.rotation;
		shapeType = src.shapeType;
		lineThickness = src.lineThickness;
		align = src.align;
		valign = src.valign;
		mPoints = new ArrayList<MPoint>();
		for (MPoint p : src.mPoints)
		{
			mPoints.add(new MPoint(p));
		}
		for (MAnchor a : src.anchors) {
			anchors.add(new MAnchor(a));
		}
		comments = new ArrayList<Comment>();
		for (Comment c : src.comments)
		{
			try
			{
				comments.add((Comment) c.clone());
			}
			catch (CloneNotSupportedException e)
			{
				assert (false);
				/* not going to happen */
			}
		}
		version = src.version;
		mWidth = src.mWidth;
		graphId = src.graphId;
		graphRef = src.graphRef;
		groupId = src.groupId;
		groupRef = src.groupRef;
		groupStyle = src.groupStyle;
		connectorType = src.connectorType;
		biopaxRefs = (List<String>)((ArrayList<String>)src.biopaxRefs).clone();
		if(src.biopax != null) {
			biopax = (Document)src.biopax.clone();
		}
		fireObjectModifiedEvent(PathwayElementEvent.createAllPropertiesEvent(this));
	}

	/**
	 * Copy Object. The object will not be part of the same Pathway object, it's
	 * parent will be set to null.
	 *
	 * No events will be sent to the parent of the original.
	 */
	public PathwayElement copy()
	{
		PathwayElement result = PathwayElement.createPathwayElement(objectType);
		result.copyValuesFrom(this);
		result.parent = null;
		return result;
	}

	protected ObjectType objectType = ObjectType.DATANODE;

	public ObjectType getObjectType()
	{
		return objectType;
	}

	// only for lines
	private List<MPoint> mPoints = Arrays.asList(new MPoint(0, 0), new MPoint(0, 0));

	public void setMPoints(List<MPoint> points) {
		if(points != null) {
			if(points.size() < 2) {
				throw new IllegalArgumentException("Points array should at least have two elements");
			}
			mPoints = points;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
	}

	public MPoint getMStart()
	{
		return mPoints.get(0);
	}

	public void setMStart(MPoint p)
	{
		getMStart().moveTo(p);
	}

	public MPoint getMEnd()
	{
		return mPoints.get(mPoints.size() - 1);
	}

	public void setMEnd(MPoint p)
	{
		getMEnd().moveTo(p);
	}

	public List<MPoint> getMPoints()
	{
		return mPoints;
	}

	public double getMStartX()
	{
		return getMStart().getX();
	}

	public void setMStartX(double v)
	{
		getMStart().setX(v);
	}

	public double getMStartY()
	{
		return getMStart().getY();
	}

	public void setMStartY(double v)
	{
		getMStart().setY(v);
	}

	public double getMEndX()
	{
		return mPoints.get(mPoints.size() - 1).getX();
	}

	public void setMEndX(double v)
	{
		getMEnd().setX(v);
	}

	public double getMEndY()
	{
		return getMEnd().getY();
	}

	public void setMEndY(double v)
	{
		getMEnd().setY(v);
	}

	protected int lineStyle = LineStyle.SOLID;

	public int getLineStyle()
	{
		return lineStyle;
	}

	public void setLineStyle(int value)
	{
		if (lineStyle != value)
		{
			lineStyle = value;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
			
			//handle LineStyle.DOUBLE until GPML is updated
			//TODO: remove after next GPML update
			if (lineStyle == LineStyle.DOUBLE)
				setDynamicProperty(CellularComponentType.DOUBLE_LINE_KEY, "Double");
		}
	}

	protected LineType endLineType = LineType.LINE;
	protected LineType startLineType = LineType.LINE;

	public LineType getStartLineType()
	{
		return startLineType == null ? LineType.LINE : startLineType;
	}

	public LineType getEndLineType()
	{
		return endLineType == null ? LineType.LINE : endLineType;
	}

	public void setStartLineType(LineType value)
	{
		if (startLineType != value)
		{
			startLineType = value;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.STARTLINETYPE));
		}
	}

	public void setEndLineType(LineType value)
	{
		if (endLineType != value)
		{
			endLineType = value;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.ENDLINETYPE));
		}
	}

	private ConnectorType connectorType = ConnectorType.STRAIGHT;

	public void setConnectorType(ConnectorType type) {
		if(connectorType == null) {
			throw new IllegalArgumentException();
		}
		if (!connectorType.equals(type))
		{
			connectorType = type;
			//TODO: create a static property for connector type, linestyle is not the correct mapping
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		}
	}

	public ConnectorType getConnectorType() {
		return connectorType;
	}

//TODO: end of new elements
	protected List<MAnchor> anchors = new ArrayList<MAnchor>();

	/**
	 * Get the anchors for this line.
	 * @return A list with the anchors, or an empty list, if no anchors are defined
	 */
	public List<MAnchor> getMAnchors() {
		return anchors;
	}

	/**
	 * Add a new anchor to this line at the given position.
	 * @param position The relative position on the line, between 0 (start) to 1 (end).
	 */
	public MAnchor addMAnchor(double position) {
		if(position < 0 || position > 1) {
			throw new IllegalArgumentException(
					"Invalid position value '" + position +
					"' must be between 0 and 1");
		}
		MAnchor anchor = new MAnchor(position);
		anchors.add(anchor);
		//No property for anchor, use LINESTYLE as dummy property to force redraw on line
		fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		return anchor;
	}

	/**
	 * Remove the given anchor
	 */
	public void removeMAnchor(MAnchor anchor) {
		if (anchors.remove(anchor)) {
			//No property for anchor, use LINESTYLE as dummy property to force redraw on line
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		}
	}

	protected Color color = new Color(0, 0, 0);

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color v)
	{
		if (v == null)
			throw new IllegalArgumentException();
		if (color != v)
		{
			color = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.COLOR));
		}
	}

	/**
	   a fillcolor of null is equivalent to transparent.
	 */
	protected Color fillColor = null;

	public Color getFillColor()
	{
		return fillColor;
	}

	public void setFillColor(Color v)
	{
		if (fillColor != v)
		{
			fillColor = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.FILLCOLOR));
		}
	}

	/**
	   checks if fill color is equal to null or the alpha value is equal to 0.
	 */
	public boolean isTransparent()
	{
		return fillColor == null || fillColor.getAlpha() == 0;
	}

	/**
	   sets the alpha component of fillColor to 0 if true
	   sets the alpha component of fillColor to 255 if true
	 */
	public void setTransparent(boolean v)
	{
		if (isTransparent() != v)
		{
			if(fillColor == null) {
				fillColor = Color.WHITE;
			}
			int alpha = v ? 0 : 255;
			fillColor = new Color(
					fillColor.getRed(),
					fillColor.getGreen(),
					fillColor.getBlue(),
					alpha);

			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.TRANSPARENT));
		}
	}

	// general
	List<Comment> comments = new ArrayList<Comment>();

	public List<Comment> getComments()
	{
		return comments;
	}

	public void setComments(List<Comment> value)
	{
		if (comments != value)
		{
			comments = value;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.COMMENTS));
		}
	}

	public void addComment(String comment, String source)
	{
		addComment(new Comment(comment, source));
	}

	public void addComment(Comment comment) {
		comments.add(comment);
		fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.COMMENTS));
	}

	public void removeComment(Comment comment) {
		comments.remove(comment);
		fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.COMMENTS));
	}

	/**
	 * Finds the first comment with a specific source
	 */
	public String findComment(String source)
	{
		for (Comment c : comments)
		{
			if (source.equals(c.source))
			{
				return c.comment;
			}
		}
		return null;
	}

	protected String setGeneID = "";

	public String getGeneID()
	{
		return setGeneID;
	}

	public void setGeneID(String v)
	{
		if (v == null)
			throw new IllegalArgumentException();
		if (!Utils.stringEquals(setGeneID, v))
		{
			setGeneID = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GENEID));
		}
	}

	protected String dataNodeType = "Unknown";

	public String getDataNodeType()
	{
		return dataNodeType;
	}

	public void setDataNodeType(DataNodeType type) {
		setDataNodeType(type.getName());
	}

	public void setDataNodeType(String v)
	{
		if (v == null)
			throw new IllegalArgumentException();
		if (!Utils.stringEquals(dataNodeType, v))
		{
			dataNodeType = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.TYPE));
		}
	}

	/**
	 * The pathway datasource
	 */
	protected DataSource dataSource = null;

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource v)
	{
		if (dataSource != v)
		{
			dataSource = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.DATASOURCE));
		}
	}

	/**
	 * returns GeneID and datasource combined in an Xref.
	 * Only meaningful for datanodes.
	 *
	 * Same as
	 * new Xref (
	 * 		pathwayElement.getGeneID(),
	 * 		pathwayElement.getDataSource()
	 * );
	 */
	public Xref getXref()
	{
		//TODO: Store Xref by default, derive setGeneID and dataSource from it.
		return new Xref (setGeneID, dataSource);
	}

	protected double mCenterx = 0;

	public double getMCenterX()
	{
		return mCenterx;
	}

	public void setMCenterX(double v)
	{
		if (mCenterx != v)
		{
			mCenterx = v;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
	}

	protected double mCentery = 0;

	public double getMCenterY()
	{
		return mCentery;
	}

	public void setMCenterY(double v)
	{
		if (mCentery != v)
		{
			mCentery = v;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
	}

	protected double mWidth = 0;

	public double getMWidth()
	{
		return mWidth;
	}

	public void setMWidth(double v)
	{
		if(mWidth < 0) {
			throw new IllegalArgumentException("Tried to set dimension < 0: " + v);
		}
		if (mWidth != v)
		{
			mWidth = v;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
	}

	protected double mHeight = 0;

	public double getMHeight()
	{
		return mHeight;
	}

	public void setMHeight(double v)
	{
		if(mWidth < 0) {
			throw new IllegalArgumentException("Tried to set dimension < 0: " + v);
		}
		if (mHeight != v)
		{
			mHeight = v;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
	}

	// starty for shapes
	public double getMTop()
	{
		return mCentery - mHeight / 2;
	}

	public void setMTop(double v)
	{
		mCentery = v + mHeight / 2;
		fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
	}

	// startx for shapes
	public double getMLeft()
	{
		return mCenterx - mWidth / 2;
	}

	public void setMLeft(double v)
	{
		mCenterx = v + mWidth / 2;
		fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
	}

	protected ShapeType shapeType = ShapeType.RECTANGLE;

	public ShapeType getShapeType()
	{
		return shapeType;
	}

	public void setShapeType(ShapeType v)
	{
		if (shapeType != v)
		{
			shapeType = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.SHAPETYPE));
		}
	}

	public void setOrientation(int orientation)
	{
		switch (orientation)
		{
		case OrientationType.TOP:
			setRotation(0);
			break;
		case OrientationType.LEFT:
			setRotation(Math.PI * (3.0 / 2));
			break;
		case OrientationType.BOTTOM:
			setRotation(Math.PI);
			break;
		case OrientationType.RIGHT:
			setRotation(Math.PI / 2);
			break;
		}
	}

	public int getOrientation()
	{
		double r = rotation / Math.PI;
		if (r < 1.0 / 4 || r >= 7.0 / 4)
			return OrientationType.TOP;
		if (r > 5.0 / 4 && r <= 7.0 / 4)
			return OrientationType.LEFT;
		if (r > 3.0 / 4 && r <= 5.0 / 4)
			return OrientationType.BOTTOM;
		if (r > 1.0 / 4 && r <= 3.0 / 4)
			return OrientationType.RIGHT;
		return 0;
	}

	protected double rotation = 0; // in radians

	public double getRotation()
	{
		return rotation;
	}

	public void setRotation(double v)
	{
		if (rotation != v)
		{
			rotation = v;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}

	}

	/**
	 * Get the rectangular bounds of the object
	 * after rotation is applied
	 */
	public Rectangle2D getRBounds() {
		Rectangle2D bounds = getMBounds();
		AffineTransform t = new AffineTransform();
		t.rotate(getRotation(), getMCenterX(), getMCenterY());
		bounds = t.createTransformedShape(bounds).getBounds2D();
		return bounds;
	}

	/**
	 * Get the rectangular bounds of the object
	 * without rotation taken into accound
	 */
	public Rectangle2D getMBounds() {
		return new Rectangle2D.Double(getMLeft(), getMTop(), getMWidth(), getMHeight());
	}

	// for labels
	protected boolean fBold = false;

	public boolean isBold()
	{
		return fBold;
	}

	public void setBold(boolean v)
	{
		if (fBold != v)
		{
			fBold = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.FONTWEIGHT));
		}
	}

	protected boolean fStrikethru = false;

	public boolean isStrikethru()
	{
		return fStrikethru;
	}

	public void setStrikethru(boolean v)
	{
		if (fStrikethru != v)
		{
			fStrikethru = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.FONTSTYLE));
		}
	}

	protected boolean fUnderline = false;

	public boolean isUnderline()
	{
		return fUnderline;
	}

	public void setUnderline(boolean v)
	{
		if (fUnderline != v)
		{
			fUnderline = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.FONTSTYLE));
		}
	}

	protected boolean fItalic = false;

	public boolean isItalic()
	{
		return fItalic;
	}

	public void setItalic(boolean v)
	{
		if (fItalic != v)
		{
			fItalic = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.FONTSTYLE));
		}
	}

	protected String fontName = "Arial";

	public String getFontName()
	{
		return fontName;
	}

	public void setFontName(String v)
	{
		if (v == null)
			throw new IllegalArgumentException();
		if (!Utils.stringEquals(fontName, v))
		{
			fontName = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.FONTNAME));
		}
	}

	protected String textLabel = "";

	public String getTextLabel()
	{
		return textLabel;
	}

	public void setTextLabel(String v)
	{
		String input = (v == null) ? "" : v;
		if (!Utils.stringEquals(textLabel, input))
		{
			textLabel = input;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.TEXTLABEL));
		}
	}

	protected String href = "";

	public String getHref()
	{
		return href;
	}

	public void setHref(String v)
	{
		String input = (v == null) ? "" : v;
		if (!Utils.stringEquals(href, input))
		{
			href = input;
			setColor(PreferenceManager.getCurrent().getColor(GlobalPreference.COLOR_LINK)); 
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.HREF));
		}
	}

	private double lineThickness = 1.0;

	public double getLineThickness()
	{
		return lineThickness;
	}

	public void setLineThickness(double v)
	{
		if (lineThickness != v)
		{
			lineThickness = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LINETHICKNESS));
		}
	}

	protected double mFontSize = 10;

	public double getMFontSize()
	{
		return mFontSize;
	}

	public void setMFontSize(double v)
	{
		if (mFontSize != v)
		{
			mFontSize = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.FONTSIZE));
		}
	}

	protected String mapInfoName = "untitled";

	public String getMapInfoName()
	{
		return mapInfoName;
	}

	public void setMapInfoName(String v)
	{
		if (v == null)
			throw new IllegalArgumentException();

		if (!Utils.stringEquals(mapInfoName, v))
		{
			mapInfoName = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.MAPINFONAME));
		}
	}

	protected String organism = null;

	public String getOrganism()
	{
		return organism;
	}

	public void setOrganism(String v)
	{
		if (!Utils.stringEquals(organism, v))
		{
			organism = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.ORGANISM));
		}
	}

	protected String mapInfoDataSource = null;

	public String getMapInfoDataSource()
	{
		return mapInfoDataSource;
	}

	public void setMapInfoDataSource(String v)
	{
		if (!Utils.stringEquals(mapInfoDataSource, v))
		{
			mapInfoDataSource = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.MAPINFO_DATASOURCE));
		}
	}

	protected ValignType valign = ValignType.MIDDLE;

	public void setValign (ValignType v)
	{
		if (valign != v)
		{
			valign = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.VALIGN));
		}
	}
	
	public ValignType getValign()
	{
		return valign;
	}

	protected AlignType align = AlignType.CENTER;

	public void setAlign (AlignType v)
	{
		if (align != v)
		{
			align = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.ALIGN));
		}
	}
	
	public AlignType getAlign()
	{
		return align;
	}

	protected String version = null;

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String v)
	{
		if (!Utils.stringEquals(version, v))
		{
			version = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.VERSION));
		}
	}

	protected String author = null;

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String v)
	{
		if (!Utils.stringEquals(author, v))
		{
			author = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.AUTHOR));
		}
	}

	protected String maintainer = null;

	public String getMaintainer()
	{
		return maintainer;
	}

	public void setMaintainer(String v)
	{
		if (!Utils.stringEquals(maintainer, v))
		{
			maintainer = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.MAINTAINED_BY));
		}
	}

	protected String email = null;

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String v)
	{
		if (!Utils.stringEquals(email, v))
		{
			email = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.EMAIL));
		}
	}

	protected String copyright = null;

	public String getCopyright()
	{
		return copyright;
	}

	public void setCopyright(String v)
	{
		if (!Utils.stringEquals(copyright, v))
		{
			copyright = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LICENSE));
		}
	}

	protected String lastModified = null;

	public String getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(String v)
	{
		if (!Utils.stringEquals(lastModified, v))
		{
			lastModified = v;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LAST_MODIFIED));
		}
	}

	/**
	 * Calculates the drawing size on basis of the location and size of the
	 * containing pathway elements
	 * @return The drawing size
	 */
	public double[] getMBoardSize() {
		return parent.calculateMBoardSize();
	}

	public double getMBoardWidth()
	{
		return getMBoardSize()[0];
	}

	public double getMBoardHeight()
	{
		return getMBoardSize()[1];
	}

	/* AP20070508 */
	protected String groupId;

	protected String graphId;

	protected String groupRef;

	protected GroupStyle groupStyle;

	public String doGetGraphId()
	{
		return graphId;
	}

	public String getGroupRef()
	{
		return groupRef;
	}

	public void setGroupRef(String s)
	{
		if (groupRef == null || !groupRef.equals(s))
		{
			if (parent != null)
			{
				if (groupRef != null)
				{
					parent.removeGroupRef(groupRef, this);
				}
				// Check: move add before remove??
				if (s != null)
				{
					parent.addGroupRef(s, this);
				}
			}
			groupRef = s;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GROUPREF));
		}
	}

	public String getGroupId()
	{
		return groupId;
	}

	public String createGroupId()
	{
		if (groupId == null)
		{
			setGroupId(parent.getUniqueGroupId());
		}
		return groupId;
	}

	public void setGroupStyle(GroupStyle gs)
	{
		if(groupStyle != gs) {
			groupStyle = gs;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GROUPSTYLE));
		}
	}

	public GroupStyle getGroupStyle()
	{
		if(groupStyle == null) {
			groupStyle = GroupStyle.NONE;
		}
		return groupStyle;
	}

	/**
	 * Set groupId. This id must be any string unique within the Pathway object
	 *
	 * @see Pathway#getUniqueId(java.util.Set)
	 */
	public void setGroupId(String w)
	{
		if (groupId == null || !groupId.equals(w))
		{
			if (parent != null)
			{
				if (groupId != null)
				{
					parent.removeGroupId(groupId);
				}
				// Check: move add before remove??
				if (w != null)
				{
					parent.addGroupId(w, this);
				}
			}
			groupId = w;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GROUPID));
		}

	}

	protected String graphRef = null;

	/** graphRef property, used by Modification */
	public String getGraphRef()
	{
		return graphRef;
	}

	/**
	 * set graphRef property, used by State
	 * The new graphRef should exist and point to an existing DataNode
	 */
	public void setGraphRef (String value)
	{
		// TODO: check that new graphRef exists and that it points to a DataNode
		if (!(graphRef == null ? value == null : graphRef.equals(value)))
		{
			graphRef = value;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GRAPHREF));
		}
	}

	private double relX;

	/**
	 * relX property, used by State.
	 * Should normally be between -1.0 and 1.0, where 1.0
	 * corresponds to the edge of the parent object
	 */
	public double getRelX()
	{
		return relX;
	}

	/**
	 * See getRelX
	 */
	public void setRelX(double value)
	{
		if (relX != value)
		{
			relX = value;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
	}

	private double relY;

	/**
	 * relX property, used by State.
	 * Should normally be between -1.0 and 1.0, where 1.0
	 * corresponds to the edge of the parent object
	 */
	public double getRelY()
	{
		return relY;
	}

	/**
	 * See getRelX
	 */
	public void setRelY(double value)
	{
		if (relY != value)
		{
			relY = value;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
	}


	public String getGraphId()
	{
		return graphId;
	}

	/**
	 * Set graphId. This id must be any string unique within the Pathway object
	 *
	 * @see Pathway#getUniqueId(java.util.Set)
	 */
	public void setGraphId(String v)
	{
		GraphLink.setGraphId(v, this, parent);
		graphId = v;
		fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.GRAPHID));
	}

	public String setGeneratedGraphId()
	{
		setGraphId(parent.getUniqueGraphId());
		return graphId;
	}

	public String getStartGraphRef()
	{
		return mPoints.get(0).getGraphRef();
	}

	public void setStartGraphRef(String ref)
	{
		MPoint start = mPoints.get(0);
		start.setGraphRef(ref);
	}

	public String getEndGraphRef()
	{
		return mPoints.get(mPoints.size() - 1).getGraphRef();
	}

	public void setEndGraphRef(String ref)
	{
		MPoint end = mPoints.get(mPoints.size() - 1);
		end.setGraphRef(ref);
	}

	protected Document biopax;

	public Document getBiopax()
	{
		return biopax;
	}

	BiopaxReferenceManager bpRefMgr;

	public BiopaxReferenceManager getBiopaxReferenceManager() {
		if(bpRefMgr == null) {
			bpRefMgr = new BiopaxReferenceManager(this);
		}
		return bpRefMgr;
	}

	public void setBiopax(Document bp)
	{
		biopax = bp;
		if(parent != null) parent.getBiopaxElementManager().refresh();
	}

	protected List<String> biopaxRefs = new ArrayList<String>();

	public List<String> getBiopaxRefs()
	{
		return biopaxRefs;
	}

	public void setBiopaxRefs(List<String> refs) {
		if(refs != null && !biopaxRefs.equals(refs)) {
			biopaxRefs = refs;
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.BIOPAXREF));
		}
	}

	public void addBiopaxRef(String ref)
	{
		if (ref != null && !biopaxRefs.contains(ref))
		{
			biopaxRefs.add(ref);
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.BIOPAXREF));
		}
	}

	public void removeBiopaxRef(String ref)
	{
		if(ref != null) {
			boolean changed = biopaxRefs.remove(ref);
			if(changed) {
				fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.BIOPAXREF));
			}
		}
	}

	public PathwayElement[] splitLine()
	{
		double centerX = (getMStartX() + getMEndX()) / 2;
		double centerY = (getMStartY() + getMEndY()) / 2;
		PathwayElement l1 = new PathwayElement(ObjectType.LINE);
		l1.copyValuesFrom(this);
		l1.setMStartX(getMStartX());
		l1.setMStartY(getMStartY());
		l1.setMEndX(centerX);
		l1.setMEndY(centerY);
		PathwayElement l2 = new PathwayElement(ObjectType.LINE);
		l2.copyValuesFrom(this);
		l2.setMStartX(centerX);
		l2.setMStartY(centerY);
		l2.setMEndX(getMEndX());
		l2.setMEndY(getMEndY());
		return new PathwayElement[] { l1, l2 };
	}

	int noFire = 0;

	public void dontFireEvents(int times)
	{
		noFire = times;
	}

	private Set<PathwayElementListener> listeners = new HashSet<PathwayElementListener>();

	public void addListener(PathwayElementListener v)
	{
		if(!listeners.contains(v)) listeners.add(v);
	}

	public void removeListener(PathwayElementListener v)
	{
		listeners.remove(v);
	}

	public void fireObjectModifiedEvent(PathwayElementEvent e)
	{
		if (noFire > 0)
		{
			noFire -= 1;
			return;
		}
		if (parent != null) parent.childModified(e);
		for (PathwayElementListener g : listeners)
		{
			g.gmmlObjectModified(e);
		}
	}

	/**
	 * This sets the object to a suitable default size.
	 *
	 * This method is intended to be called right after the object is placed on
	 * the drawing with a click.
	 */
	public void setInitialSize()
	{	
		switch (objectType)
		{
		case SHAPE:
			if (shapeType == ShapeType.BRACE)
			{
				setMWidth(M_INITIAL_BRACE_WIDTH);
				setMHeight(M_INITIAL_BRACE_HEIGHT);
			} 
			else if (shapeType == ShapeType.MITOCHONDRIA || lineStyle == LineStyle.DOUBLE)
			{
				setMWidth(M_INITIAL_CELLCOMP_WIDTH);
				setMHeight(M_INITIAL_CELLCOMP_HEIGHT);				
			} 
			else 
			{
				setMWidth(M_INITIAL_SHAPE_SIZE);
				setMHeight(M_INITIAL_SHAPE_SIZE);
			}
			break;
		case DATANODE:
			setMWidth(M_INITIAL_GENEPRODUCT_WIDTH);
			setMHeight(M_INITIAL_GENEPRODUCT_HEIGHT);
			break;
		case LINE:
			setMEndX(getMStartX() + M_INITIAL_LINE_LENGTH);
			setMEndY(getMStartY() + M_INITIAL_LINE_LENGTH);
			break;
		case STATE:
			setMWidth(M_INITIAL_STATE_SIZE);
			setMHeight(M_INITIAL_STATE_SIZE);
			break;
		}
	}

	public Set<GraphRefContainer> getReferences()
	{
		return GraphLink.getReferences(this, parent);
	}

	public int compareTo(PathwayElement o) {
		return getZOrder() - o.getZOrder();
	}

	public Point2D toAbsoluteCoordinate(Point2D p) {
		double x = p.getX();
		double y = p.getY();
		Rectangle2D bounds = getRBounds();
		//Scale
		if(bounds.getWidth() != 0) x *= bounds.getWidth() / 2;
		if(bounds.getHeight() != 0) y *= bounds.getHeight() / 2;
		//Translate
		x += bounds.getCenterX();
		y += bounds.getCenterY();
		return new Point2D.Double(x, y);
	}

	public Point2D toRelativeCoordinate(Point2D p) {
		double relX = p.getX();
		double relY = p.getY();
		Rectangle2D bounds = getRBounds();
		//Translate
		relX -= bounds.getCenterX();
		relY -= bounds.getCenterY();
		//Scalebounds.getCenterX();
		if(relX != 0 && bounds.getWidth() != 0) relX /= bounds.getWidth() / 2;
		if(relY != 0 && bounds.getHeight() != 0) relY /= bounds.getHeight() / 2;
		return new Point2D.Double(relX, relY);
	}

	public void printRefsDebugInfo()
	{
		System.err.println (objectType + " " + getGraphId());
		if (this instanceof MLine)
		{
			for (MPoint p : getMPoints())
			{
				System.err.println("  p: " + p.getGraphId());
			}
			for (MAnchor a : getMAnchors())
			{
				System.err.println("  a: " + a.getGraphId());
			}
		}
		if (this instanceof MState)
		{
			System.err.println ("  " + getGraphRef());
		}
	}
}
