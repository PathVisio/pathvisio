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
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

public class UddiPing {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: UddiPing business-name uddi-url");
            System.exit(1);
        }
        searchUDDI(args[0], args[1]);
    }

    public static void searchUDDI(String name, String url) throws Exception {
        // Create the connection and the message factory.
        SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = scf.createConnection();
        MessageFactory msgFactory = MessageFactory.newInstance();

        // Create a message
        SOAPMessage msg = msgFactory.createMessage();

        // Create an envelope in the message
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();

        // Get hold of the the body
        SOAPBody body = envelope.getBody();

        javax.xml.soap.SOAPBodyElement bodyElement = body.addBodyElement(envelope.createName("find_business", "",
                "urn:uddi-org:api"));

        bodyElement.addAttribute(envelope.createName("generic"), "1.0")
                .addAttribute(envelope.createName("maxRows"), "100")
                .addChildElement("name")
                .addTextNode(name);

        URLEndpoint endpoint = new URLEndpoint(url);
        msg.saveChanges();

        SOAPMessage reply = connection.call(msg, endpoint);
        //System.out.println("Received reply from: " + endpoint);
        //reply.writeTo(System.out);
        connection.close();
    }
}






