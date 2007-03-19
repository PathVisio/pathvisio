package data;

import org.eclipse.swt.graphics.RGB;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class SvgFormat 
{
	static final Namespace nsSVG = Namespace.getNamespace("http://www.w3.org/2000/svg");
	static Document createJdom (GmmlData data) throws ConverterException
	{
		Document doc = new Document();
	
		
		
		Element root = new Element("svg");
		root.setNamespace(nsSVG);
		doc.setRootElement(root);
//    	doc.setDocType(new DocType("SVG"));
		
		setDefs(root);
    	
		for (GmmlDataObject o : data.getDataObjects())
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
			{
				root.setAttribute("width", "" + toPixel(o.getMBoardWidth()));
				root.setAttribute("height", "" + toPixel(o.getMBoardHeight()));
			}
			else
			{
				addElement(root, o);
			}
		}		
		return doc;
	}

	static public void addElement (Element root, GmmlDataObject o) throws ConverterException 
	{		
		switch (o.getObjectType())
		{
			case ObjectType.SHAPE:
				mapShape(root, o);
				break;
			case ObjectType.DATANODE:
				mapDataNode(root, o);
				break;
			case ObjectType.LINE:
				mapLine(root, o);
				break;
			case ObjectType.LABEL:
				mapLabel(root, o);
				break;
		}
	}
	
	static void mapLine(Element parent, GmmlDataObject o) {
		Element e = new Element("line", nsSVG);
		e.setAttribute("x1", "" + toPixel(o.getMStartX()));
		e.setAttribute("y1", "" + toPixel(o.getMStartY()));
		e.setAttribute("x2", "" + toPixel(o.getMEndX()));
		e.setAttribute("y2", "" + toPixel(o.getMEndY()));
		e.setAttribute("stroke", rgb2String(o.getColor()));
		parent.addContent(e);
		LineType type = o.getLineType();
		if(type != LineType.LINE) {
			e.setAttribute("marker-end", "url(#" + type.getGpmlName() + ")");
		}
		
	}
	
	static void mapDataNode(Element parent, GmmlDataObject o) {
		Element e = new Element("rect", nsSVG);
		e.setAttribute("x", "" + toPixel(o.getMLeft()));
		e.setAttribute("y", "" + toPixel(o.getMTop()));
		e.setAttribute("width", "" + toPixel(o.getMWidth()));
		e.setAttribute("height", "" + toPixel(o.getMHeight()));
		mapColor(e, o);
		parent.addContent(e);
		e = new Element("text", nsSVG);
		e.setAttribute("x", "" + toPixel(o.getMCenterX()));
		e.setAttribute("y", "" + toPixel(o.getMCenterY()));
		e.setAttribute("text-anchor", "middle");
		e.addContent(o.getTextLabel());
		parent.addContent(e);
	}
	
	static void mapLabel(Element parent, GmmlDataObject o) {
		Element e = new Element("text", nsSVG);
		e.setAttribute("x", "" + toPixel(o.getMCenterX()));
		e.setAttribute("y", "" + toPixel(o.getMCenterY()));
		e.setAttribute("style", "font-family:" + o.getFontName() + 
				".ttf;font-size:" + toPixel(o.getMFontSize()));
		e.setAttribute("text-anchor", "middle");
		e.addContent(o.getTextLabel());
		parent.addContent(e);
	}
	
	static void mapShape(Element parent, GmmlDataObject o) {
		Element e = null;
		switch (o.getShapeType())
		{
			case OVAL:
				e = new Element("ellipse", nsSVG);
				e.setAttribute("cx", "" + toPixel(o.getMCenterX()));
				e.setAttribute("cy", "" + toPixel(o.getMCenterY()));
				e.setAttribute("rx", "" + toPixel(o.getMWidth()/2));
				e.setAttribute("ry", "" + toPixel(o.getMHeight()/2));
				break;
			default:
				e = new Element("rect", nsSVG);
				e.setAttribute("x", "" + toPixel(o.getMLeft()));
				e.setAttribute("y", "" + toPixel(o.getMTop()));
				e.setAttribute("width", "" + toPixel(o.getMWidth()));
				e.setAttribute("height", "" + toPixel(o.getMHeight()));
				break;
		}
		mapColor(e, o);
		parent.addContent(e);
	}
	
	static void mapColor(Element e, GmmlDataObject o) {
		e.setAttribute("stroke", rgb2String(o.getColor()));
		if(o.isTransparent()) {
			e.setAttribute("fill-opacity", "" + 0);
		} else {
			e.setAttribute("fill", rgb2String(o.getFillColor()));
		}
	}
	
	static String rgb2String(RGB rgb) {
		return "rgb(" + rgb.red + "," + rgb.green + "," + rgb.blue + ")";
	}
	
	static int toPixel(double coordinate) {
		return (int)(coordinate * 1/15);
	}
		
	static void setDefs(Element root) {
		Element defs = new Element("defs", nsSVG);
		
		//Arrow
		Element marker = new Element("marker", nsSVG);
		marker.setAttribute("id", LineType.ARROW.getGpmlName());
		marker.setAttribute("orient", "auto");
		marker.setAttribute("refX", "10");
		marker.setAttribute("refY", "5");
		marker.setAttribute("markerWidth", "10");
		marker.setAttribute("markerHeight", "15");
		Element e = new Element("path", nsSVG);
		e.setAttribute("d", "M 0 0 L 10 5 L 0 10 z");
		
		marker.addContent(e);
		defs.addContent(marker);
		
		//Arrow
		marker = new Element("marker", nsSVG);
		marker.setAttribute("id", LineType.TBAR.getGpmlName());
		marker.setAttribute("orient", "auto");
		marker.setAttribute("refX", "1");
		marker.setAttribute("refY", "5");
		marker.setAttribute("markerWidth", "1");
		marker.setAttribute("markerHeight", "15");
		e = new Element("line", nsSVG);
		e.setAttribute("x1", "0");
		e.setAttribute("y1", "0");
		e.setAttribute("x2", "0");
		e.setAttribute("y2", "15");
		
		marker.addContent(e);
		defs.addContent(marker);
		
			
		root.addContent(defs);
	}
}
