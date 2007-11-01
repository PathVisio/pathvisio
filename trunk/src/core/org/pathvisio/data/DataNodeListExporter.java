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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.pathvisio.data.Gdb.IdCodePair;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayExporter;

public class DataNodeListExporter implements PathwayExporter {
	public static final String DB_ORIGINAL = "original"; //Use the id/code as in database
	private String resultCode = DB_ORIGINAL;
	
	public void setResultCode(String code) {
		resultCode = code;
	}
	
	public String getResultCode() {
		return resultCode;
	}
	
	public void doExport(File file, Pathway pathway) throws ConverterException {
		if(!DB_ORIGINAL.equals(resultCode)) {
			//Check gene database connection
			if(!Gdb.isConnected()) {
				throw new ConverterException("No gene database loaded");
			}
		}
		PrintStream out = null;
		try {
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
		} catch (FileNotFoundException e) {
			throw new ConverterException(e);
		}
		//print headers
		out.println("ID\tDatabase");
		for(PathwayElement elm : pathway.getDataObjects()) {
			if(elm.getObjectType() == ObjectType.DATANODE) {
				String line = "";
				String id = elm.getGeneID();
				String code = elm.getSystemCode();
				if(!checkString(id) || !checkString(code)) {
					continue; //Skip empty id/codes
				}
				//Use the original id, if code is already the one asked for
				if(DB_ORIGINAL.equals(resultCode) || code.equals(resultCode)) {
					line = id + "\t" + DataSources.sysCode2Name.get(code);
				} else { //Lookup the cross-references for the wanted database code
					ArrayList<IdCodePair> refs = Gdb.getCrossRefs(new IdCodePair(id, code), resultCode);
					for(IdCodePair ref : refs) {
						line += ref.getId() + ", ";
					}
					if(line.length() > 2) { //Remove the last ', '
						line = line.substring(0, line.length() - 2);
						line += "\t" + DataSources.sysCode2Name.get(resultCode);
					}
				}
				out.println(line);
			}
		}
		out.close();
	}

	private boolean checkString(String string) {
		return string != null && string.length() > 0;
	}
	public String[] getExtensions() {
		return new String[] { "txt" };
	}

	public String getName() {
		return "DataNode list";
	}

}
