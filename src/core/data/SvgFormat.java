package data;

import org.jdom.*;

public class SvgFormat 
{
	static Document createJdom (GmmlData data) throws ConverterException
	{
		Document doc = new Document();
	
		Namespace ns = Namespace.getNamespace("http://www.w3.org/2000/svg");
		
		Element root = new Element("svg", ns);
		doc.setRootElement(root);
//    	doc.setDocType(new DocType("SVG"));
    	
		for (GmmlDataObject o : data.getDataObjects())
		{
			if (o.getObjectType() == ObjectType.MAPPINFO)
			{
				root.setAttribute("width", "" + o.getMBoardWidth());
				root.setAttribute("height", "" + o.getMBoardHeight());
			}
			else
			{
				addElement(root, o, ns);
			}
		}		
		return doc;
	}

	static public void addElement (Element root, GmmlDataObject o, Namespace ns) throws ConverterException 
	{		
		Element e = null;
		
		switch (o.getObjectType())
		{
			case ObjectType.FIXEDSHAPE:
			case ObjectType.COMPLEXSHAPE:
			case ObjectType.BRACE:
				e = new Element("rect", ns);
				e.setAttribute("x", "" + o.getMLeft());
				e.setAttribute("y", "" + o.getMTop());
				e.setAttribute("width", "" + o.getMWidth());
				e.setAttribute("height", "" + o.getMHeight());
				e.setAttribute("style", "stroke:black;fill:none");
				root.addContent(e);
				break;
			case ObjectType.SHAPE:
				switch (o.getShapeType())
				{
					case ShapeType.OVAL:
						e = new Element("ellipse", ns);
						e.setAttribute("cx", "" + o.getMCenterX());
						e.setAttribute("cy", "" + o.getMCenterY());
						e.setAttribute("rx", "" + o.getMWidth()/2);
						e.setAttribute("ry", "" + o.getMHeight()/2);
						e.setAttribute("style", "stroke:black;fill:none");
						root.addContent(e);
						break;
					case ShapeType.RECTANGLE:
					case ShapeType.ARC:				
						e = new Element("rect", ns);
						e.setAttribute("x", "" + o.getMLeft());
						e.setAttribute("y", "" + o.getMTop());
						e.setAttribute("width", "" + o.getMWidth());
						e.setAttribute("height", "" + o.getMHeight());
						e.setAttribute("style", "stroke:black;fill:none");
						root.addContent(e);
					break;
				}
				break;
			case ObjectType.GENEPRODUCT:
				e = new Element("rect", ns);
				e.setAttribute("x", "" + o.getMLeft());
				e.setAttribute("y", "" + o.getMTop());
				e.setAttribute("width", "" + o.getMWidth());
				e.setAttribute("height", "" + o.getMHeight());
				e.setAttribute("style", "stroke:black;fill:none");
				root.addContent(e);
				e = new Element("text", ns);
				e.setAttribute("x", "" + o.getMCenterX());
				e.setAttribute("y", "" + o.getMCenterY());
				e.addContent(o.getGeneID());
				root.addContent(e);
				break;
			case ObjectType.LINE:
				e = new Element("line", ns);
				e.setAttribute("x1", "" + o.getMStartX());
				e.setAttribute("y1", "" + o.getMStartY());
				e.setAttribute("x2", "" + o.getMEndX());
				e.setAttribute("y2", "" + o.getMEndY());
				e.setAttribute("style", "stroke:black");
				root.addContent(e);
				break;
			case ObjectType.LABEL:
				e = new Element("text", ns);
				e.setAttribute("x", "" + o.getMCenterX());
				e.setAttribute("y", "" + o.getMCenterY());
				e.setAttribute("style", "font-family:" + o.getFontName() + 
						".ttf;font-size:" + o.getMFontSize());
				e.addContent(o.getLabelText());
				root.addContent(e);
				break;
		}
	}


}
