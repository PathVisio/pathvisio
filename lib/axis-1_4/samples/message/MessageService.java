/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package samples.message ;

import org.w3c.dom.Element;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPElement;

/**
 * Simple message-style service sample.
 */
public class MessageService {
    /**
     * Service method, which simply echoes back any XML it receives.
     *
     * @param elems an array of DOM Elements, one for each SOAP body element
     * @return an array of DOM Elements to be sent in the response body
     */
    public Element[] echoElements(Element [] elems) {
        return elems;
    }

    public void process(SOAPEnvelope req, SOAPEnvelope resp) throws javax.xml.soap.SOAPException {
    	SOAPBody body = resp.getBody();
    	Name ns0 =  resp.createName("TestNS0", "ns0", "http://example.com");
    	Name ns1 =  resp.createName("TestNS1", "ns1", "http://example.com");
    	SOAPElement bodyElmnt = body.addBodyElement(ns0);
    	SOAPElement el = bodyElmnt.addChildElement(ns1);
    	el.addTextNode("TEST RESPONSE");
    }
}
