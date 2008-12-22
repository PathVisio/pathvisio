/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.stock ;



/**
 *
 * @author Doug Davis (dug@us.ibm.com)
 */
public class ComInfoService {
  public String getInfo(String symbol, String dataType) throws Exception {
    int        i, j ;
    String[]   types = { "symbol", "name", "address" };
    String[][] data = { {"IBM", 
                         "International Business Machines",
                          "Armonk, NY" },

                         {"MACR",
                          "Macromedia",
                          "Newton, MA" },

                         {"CSCO", 
                          "Cisco Systems", 
                          "San Jose, CA" } };

    for ( i = 0 ; i < types.length ; i++ )
      if ( types[i].equals( dataType ) ) break ;

    if ( i == types.length )
      return( "Unknown dataType: " + dataType );

    for ( j = 0 ; j < data.length ; j++ )
      if ( data[j][0].equals( symbol ) ) break ;

    if ( j == data.length )
      return( "Unknown symbol: " + symbol );
    
    return( data[j][i] );
  }
}
