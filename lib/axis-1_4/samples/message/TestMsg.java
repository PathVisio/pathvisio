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

package samples.message;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Vector;

/**
 * Simple test driver for our message service.
 */
public class TestMsg {
    public String doit(String[] args) throws Exception {
        Options opts = new Options(args);
        opts.setDefaultURL("http://localhost:8080/axis/services/MessageService");

        Service  service = new Service();
        Call     call    = (Call) service.createCall();

        call.setTargetEndpointAddress( new URL(opts.getURL()) );
        SOAPBodyElement[] input = new SOAPBodyElement[3];

        input[0] = new SOAPBodyElement(XMLUtils.StringToElement("urn:foo",
                                                                "e1", "Hello"));
        input[1] = new SOAPBodyElement(XMLUtils.StringToElement("urn:foo",
                                                                "e1", "World"));

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc            = builder.newDocument();
        Element cdataElem       = doc.createElementNS("urn:foo", "e3");
        CDATASection cdata      = doc.createCDATASection("Text with\n\tImportant  <b>  whitespace </b> and tags! ");
        cdataElem.appendChild(cdata);

        input[2] = new SOAPBodyElement(cdataElem);

        Vector          elems = (Vector) call.invoke( input );
        SOAPBodyElement elem  = null ;
        Element         e     = null ;

        elem = (SOAPBodyElement) elems.get(0);
        e    = elem.getAsDOM();

        String str = "Res elem[0]=" + XMLUtils.ElementToString(e);

        elem = (SOAPBodyElement) elems.get(1);
        e    = elem.getAsDOM();
        str = str + "Res elem[1]=" + XMLUtils.ElementToString(e);

        elem = (SOAPBodyElement) elems.get(2);
        e    = elem.getAsDOM();
        str = str + "Res elem[2]=" + XMLUtils.ElementToString(e);

        return( str );
    }


    public void testEnvelope(String[] args) throws Exception {
        String xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            " <soapenv:Header>\n" +
            "  <shw:Hello xmlns:shw=\"http://localhost:8080/axis/services/MessageService\">\n" +
            "    <shw:Myname>Tony</shw:Myname>\n" +
            "  </shw:Hello>\n" +
            " </soapenv:Header>\n" +
            " <soapenv:Body>\n" +
            "  <shw:process xmlns:shw=\"http://message.samples\">\n" +
            "    <shw:City>GENT</shw:City>\n" +
            "  </shw:process>\n" +
            " </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage smsg =
                mf.createMessage(new MimeHeaders(), new ByteArrayInputStream(xmlString.getBytes()));
        SOAPPart sp = smsg.getSOAPPart();
        SOAPEnvelope se = (SOAPEnvelope)sp.getEnvelope();

        SOAPConnection conn = SOAPConnectionFactory.newInstance().createConnection();
        SOAPMessage response = conn.call(smsg, "http://localhost:8080/axis/services/MessageService2");
    }

    public static void main(String[] args) throws Exception {
        TestMsg testMsg = new TestMsg();
        testMsg.doit(args);
        testMsg.testEnvelope(args);
    }
}
