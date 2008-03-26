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
import java.io.File;
import java.util.List;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;


public class HelloWorld {


	/**
	 * @param args
	 * @throws ConverterException 
	 */
	public static void main(String[] args) throws ConverterException {
		// TODO Auto-generated method stub
		String filename = ("C:\\Documents and Settings\\s040772\\PathVisio-Data\\pathways\\rat\\Rn_Apoptosis.gpml");
		File f = new File(filename);
		Pathway p = new Pathway();
		p.readFromXml(f, true);
		List<PathwayElement> pelts = p.getDataObjects();
		for (PathwayElement v:pelts){
			Xref reference;
			reference = v.getXref();
			String name = reference.getName();
			System.out.println(name);
			}
		}
	}
