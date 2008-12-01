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
package org.pathvisio.wikipathways.client;

import java.util.Collection;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Query implements IsSerializable {
	HashMap<String, String> fields = new HashMap<String, String>();
	
	public Query() {
	}
	
	public Query(String type, String text) {
		setField(FIELD_TYPE, type);
		setField(FIELD_TEXT, text);
	}

	public String getType() {
		return getField(FIELD_TYPE);
	}
	
	public String getText() {
		return getField(FIELD_TEXT);
	}
	
	public void setField(String field, String value) {
		fields.put(field, value);
	}
	
	public Collection<String> getFields() {
		return fields.keySet();
	}
	
	public String getField(String field) {
		return fields.get(field);
	}
	
	public static Query fromString(String s) {
		Query query = new Query();
		
		String[] pairs = s.split(SEP);
		
		for(String ps : pairs) {
			String[] pair = ps.split(IS);
			if(pair.length != 2) {
				throw new IllegalArgumentException("Invalid key/value pair");
			}
			query.setField(pair[0], pair[1]);
		}
		return query;
	}
	
	public String toString() {
		StringBuilder strb = new StringBuilder();
		for(String f : fields.keySet()) {
			strb.append(f);
			strb.append(IS);
			strb.append(fields.get(f));
			strb.append(SEP);
		}
		return strb.substring(0, strb.length() - SEP.length());
	}
	
	public static final String FIELD_TEXT = "text";
	public static final String FIELD_SYSTEM = "system";
	public static final String FIELD_TYPE = "type";
	
	public static final String TYPE_ID = "id";
	public static final String TYPE_TEXT = "text";
	
	private static final String SEP = "&";
	private static final String IS = "=";
}
