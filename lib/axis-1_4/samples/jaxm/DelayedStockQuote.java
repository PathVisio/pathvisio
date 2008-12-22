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
package samples.jaxm;

import javax.xml.messaging.URLEndpoint;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.util.Iterator;

public class DelayedStockQuote {
    public static void main(String[] args) throws Exception {
        DelayedStockQuote stockQuote = new DelayedStockQuote();
        System.out.print("The last price for SUNW is " + stockQuote.getStockQuote("SUNW"));
    }

    public String getStockQuote(String tickerSymbol) throws Exception {
        SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = scFactory.createConnection();

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();

        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();

        SOAPHeader header = envelope.getHeader();
        SOAPBody body = envelope.getBody();

        header.detachNode();

        Name bodyName = envelope.createName("getQuote", "n", "urn:xmethods-delayed-quotes");
        SOAPBodyElement gltp = body.addBodyElement(bodyName);

        Name name = envelope.createName("symbol");
        SOAPElement symbol = gltp.addChildElement(name);
        symbol.addTextNode(tickerSymbol);

        URLEndpoint endpoint = new URLEndpoint("http://64.124.140.30/soap");
        SOAPMessage response = con.call(message, endpoint);
        con.close();

        SOAPPart sp = response.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();
        Iterator it = sb.getChildElements();
        while (it.hasNext()) {
            SOAPBodyElement bodyElement = (SOAPBodyElement) it.next();
            Iterator it2 = bodyElement.getChildElements();
            while (it2.hasNext()) {
                SOAPElement element2 = (SOAPElement) it2.next();
                return element2.getValue();
            }
        }
        return null;
    }
}

