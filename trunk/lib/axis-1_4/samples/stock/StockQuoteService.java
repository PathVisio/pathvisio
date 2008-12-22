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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;

/**
 * See \samples\stock\readme for info.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 * @author Doug Davis (dug@us.ibm.com)
 */
public class StockQuoteService {
  public String test() {
    return( "Just a test" );
  }

  public float getQuote (String symbol) throws Exception {
    // get a real (delayed by 20min) stockquote from 
    // http://services.xmethods.net/axis/. The IP addr 
    // below came from the host that the above form posts to ..

    if ( symbol.equals("XXX") ) return( (float) 55.25 );

    URL          url = new URL( "http://services.xmethods.net/axis/getQuote?s="
                                + symbol );

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder        db  = dbf.newDocumentBuilder();

    Document doc  = db.parse( url.toExternalForm() );
    Element  elem = doc.getDocumentElement();
    NodeList list = elem.getElementsByTagName( "stock_quote" );

    if ( list != null && list.getLength() != 0 ) {
      elem = (Element) list.item(0);
      list = elem.getElementsByTagName( "price" );
      elem = (Element) list.item(0);
      String quoteStr = elem.getAttribute("value");
      try {
        return Float.valueOf(quoteStr).floatValue();
      } catch (NumberFormatException e1) {
        // maybe its an int?
        try {
          return Integer.valueOf(quoteStr).intValue() * 1.0F;
        } catch (NumberFormatException e2) {
          return -1.0F;
        }
      }
    }
    return( 0 );
  }
}
