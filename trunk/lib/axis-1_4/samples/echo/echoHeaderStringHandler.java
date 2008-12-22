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

package samples.echo;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;

import javax.xml.namespace.QName;


/** This handler processes the SOAP header "echoMeString" defined in the 
 *  SOAPBuilder Round2C interop tests.
 *
 * <p>Essentially, you install it on both the request and response chains of
 * your service, on the server side.</p>
 *
 * @author Simon Fell (simon@zaks.demon.co.uk)
 */
public class echoHeaderStringHandler extends BasicHandler
{
    static Log log =
            LogFactory.getLog(echoHeaderStringHandler.class.getName());

    public static final String ECHOHEADER_STRING_ID = "echoHeaderStringHandler.id";
    public static final String HEADER_NS = "http://soapinterop.org/echoheader/";
    public static final String HEADER_REQNAME = "echoMeStringRequest";
    public static final String HEADER_RESNAME = "echoMeStringResponse";
    public static final String ACTOR_NEXT = "http://schemas.xmlsoap.org/soap/actor/next";

    public boolean canHandleBlock(QName qname) {
        if (HEADER_NS.equals(qname.getNamespaceURI()) &&
                HEADER_REQNAME.equals(qname.getLocalPart())) {
            return true;
        }
        
        return false;
    }

    /**
     * Process a MessageContext.
     */
    public void invoke(MessageContext context) throws AxisFault
    {    
        if (context.getPastPivot()) {
            // This is a response.  Add the response header, if we saw
            // the requestHeader
            String strVal = (String)context.getProperty(ECHOHEADER_STRING_ID);
            if (strVal == null)
                return;
            
            Message msg = context.getResponseMessage();
            if (msg == null)
                return;
            SOAPEnvelope env = msg.getSOAPEnvelope();
            SOAPHeaderElement header = new SOAPHeaderElement(HEADER_NS,
                                                             HEADER_RESNAME,
                                                             strVal);
            env.addHeader(header);
        } else {
            // Request. look for the header
            Message msg = context.getRequestMessage();
            if (msg == null)
                throw new AxisFault(Messages.getMessage("noRequest00"));
            
            SOAPEnvelope env = msg.getSOAPEnvelope();
            SOAPHeaderElement header = env.getHeaderByName(HEADER_NS,
                                                           HEADER_REQNAME);
            
            if (header != null) {
                // seems Axis has already ignored any headers not tageted
                // at us
                String strVal ;
                // header.getValue() doesn't seem to be connected to anything
                // we always get null.
                try {
                    strVal = (String)header.getValueAsType(Constants.XSD_STRING);
                } catch (Exception e) {
                    throw AxisFault.makeFault(e);
                }
                context.setProperty(ECHOHEADER_STRING_ID, strVal) ;
                header.setProcessed(true);
            }
        }
    }
}
