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

package samples.security;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileWriter;
import java.io.PrintWriter;

public class LogHandler extends BasicHandler {
    static Log log =
        LogFactory.getLog(LogHandler.class.getName());

    static {
        org.apache.xml.security.Init.init();
    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        try {
            System.out.println("Starting Server verification");

            Message inMsg = msgContext.getRequestMessage();
            Message outMsg = msgContext.getResponseMessage();

            // verify signed message

            Document doc = inMsg.getSOAPEnvelope().getAsDocument();
            String BaseURI = "http://xml-security";
            CachedXPathAPI xpathAPI = new CachedXPathAPI();

            Element nsctx = doc.createElement("nsctx");
            nsctx.setAttribute("xmlns:ds", Constants.SignatureSpecNS);

            Element signatureElem = (Element) xpathAPI.selectSingleNode(doc,
                    "//ds:Signature", nsctx);

            // check to make sure that the document claims to have been signed
            if (signatureElem == null) {
                System.out.println("The document is not signed");
                return;
            }

            XMLSignature sig = new XMLSignature(signatureElem, BaseURI);

            boolean verify = sig.checkSignatureValue(sig.getKeyInfo().getPublicKey());
            System.out.println("Server verification complete.");

            System.out.println("The signature is" + (verify
                    ? " "
                    : " not ") + "valid");
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

    }

    public void onFault(MessageContext msgContext) {
        try {
            Handler serviceHandler = msgContext.getService();
            String filename = (String) getOption("filename");
            if ((filename == null) || (filename.equals("")))
                throw new AxisFault("Server.NoLogFile",
                        "No log file configured for the LogHandler!",
                        null, null);
            FileWriter fw = new FileWriter(filename, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println("=====================");
            pw.println("= " + Messages.getMessage("fault00"));
            pw.println("=====================");
            pw.close();
        } catch (Exception e) {
            log.error(e);
        }
    }
}
