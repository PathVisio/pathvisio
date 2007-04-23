// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.data;

import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.pathvisio.model.GmmlDataObject;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.gmmlVision.GmmlVision;

public class GpmlTransfer extends ByteArrayTransfer 
{
	private static final String TYPENAME = "gpml";
	private static final int TYPEID = registerType(TYPENAME);
	private static GpmlTransfer _instance = new GpmlTransfer();
	
	private GpmlTransfer() {} // prevent instantiation because it is private
	
	public static GpmlTransfer getInstance()
	{
		return _instance;
	}
	
	public void javaToNative (Object data, TransferData transferData)
	{
		
		if (!(data instanceof List)) { return; } // wrong type of data
		
		byte[] result = null;
		List<GmmlDataObject> clipboard = (List<GmmlDataObject>)data;
		Document doc = new Document();
		Namespace ns = Namespace.getNamespace("http://www.w3.org/2000/svg");

		for (GmmlDataObject o : clipboard)
		{
			try
			{
				Element e = GpmlFormat.createJdomElement(o, ns);
				doc.addContent(e);
			}
			catch (Exception e)
			{
				GmmlVision.log.error ("Converter exception", e);
			}

			XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());
			Format f = xmlcode.getFormat();
			f.setEncoding("ISO-8859-1");
			f.setTextMode(Format.TextMode.PRESERVE);
			xmlcode.setFormat(f);
			
			//Open a filewriter
			try
			{
				result = xmlcode.outputString(doc).getBytes();				
			}
			catch (Exception e)
			{
				GmmlVision.log.error ("Error?!?!", e);
			}
		}
		super.javaToNative(result, transferData);
	}
	
	public Object nativeToJava (TransferData transferData)
	{
		 if (isSupportedType(transferData)) 
		 {
			 byte[] buffer = (byte[]) super.nativeToJava(transferData);
			 if (buffer == null)
				 return null;

			 //TODO
			 String x = "" + buffer;
			 Object result = null;
			 return result;
		}
		 
		return null;
	}
	
	protected int[] getTypeIds() {
		return new int[]{TYPEID};
	}

	protected String[] getTypeNames() {
		return new String[]{TYPENAME};
	}

}
