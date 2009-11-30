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
package org.pathvisio.wikipathways.client;

import java.util.Collection;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A search query that contains the necessary information
 * to perform a search.
 * @author thomas
 */
public class Query implements IsSerializable {
	HashMap<String, String> fields = new HashMap<String, String>();

	public Query() {
	}

	/**
	 * Create a query.
	 * @param type The query type. Should be one of the TYPE_* constants.
	 * @param text The query string.
	 */
	public Query(String type, String text) {
		setField(FIELD_TYPE, type);
		setField(FIELD_TEXT, text);
	}

	/**
	 * Get the query type.
	 * @return One of the TYPE_* constants.
	 */
	public String getType() {
		return getField(FIELD_TYPE);
	}

	/**
	 * Get the query string.
	 */
	public String getText() {
		return getField(FIELD_TEXT);
	}

	/**
	 * Set a custom query field.
	 * @param field The field name
	 * @param value The field value
	 */
	public void setField(String field, String value) {
		fields.put(field, value);
	}

	/**
	 * Get all query field names.
	 */
	public Collection<String> getFields() {
		return fields.keySet();
	}

	/**
	 * Get the value of a query field.
	 * @param field A field name.
	 * @return The field value, or null if no value exists.
	 */
	public String getField(String field) {
		return fields.get(field);
	}

	/**
	 * Build a query from a string of key/value pairs.
	 */
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

	/**
	 * Export a query to a string of key/value pairs.
	 */
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

	/**
	 * The name of the field that stores the query string
	 */
	public static final String FIELD_TEXT = "text";
	/**
	 * The name of the field that stores the database system for identifier search
	 */
	public static final String FIELD_SYSTEM = "system";
	/**
	 * The name of the field that stores the query type
	 */
	public static final String FIELD_TYPE = "type";

	/**
	 * The query type for identifier searches
	 */
	public static final String TYPE_ID = "id";
	/**
	 * The query type for text searches
	 */
	public static final String TYPE_TEXT = "text";

	private static final String SEP = "&";
	private static final String IS = "=";
}
