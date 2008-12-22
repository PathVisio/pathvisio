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

package samples.proxy;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPEnvelope;
import org.w3c.dom.Document;
import samples.transport.tcp.TCPSender;
import samples.transport.tcp.TCPTransport;

/**
 * Proxy sample.  Relays message on to hardcoded URL.
 * Soon, URL becomes configurable (via deployment?!);
 * later, URL becomes specifiable in custom header.
 *
 * @author Rob Jellinghaus <robj@unrealities.com>
 */

public class ProxyService {
    /**
     * Process the given message, treating it as raw XML.
     */
    public void proxyService(SOAPEnvelope env1, SOAPEnvelope env2)
        throws AxisFault
    {
        try {
            // Get the current Message Context
            MessageContext msgContext = MessageContext.getCurrentContext();
            
            // Look in the message context for our service
            Handler self = msgContext.getService();
            
            // what is our target URL?
            String dest = (String)self.getOption("URL");
            
            // use the server's client engine in case anything has 
            // been deployed to it
            Service service = new Service();
            service.setEngine( msgContext.getAxisEngine().getClientEngine() );
            Call    call = (Call) service.createCall();

            SimpleTargetedChain c = new SimpleTargetedChain(new TCPSender());
            // !!! FIXME
            //service.getEngine().deployTransport("tcp", c);
    
            // add TCP for proxy testing
            call.addTransportPackage("samples.transport");
            call.setTransportForProtocol("tcp", TCPTransport.class);
            
            // NOW set the client's URL (since now the tcp handler exists)
            call.setTargetEndpointAddress(new java.net.URL(dest));
    
            call.setRequestMessage(msgContext.getRequestMessage());
            
            call.invoke();
            
            Message msg = call.getResponseMessage();

            msgContext.setResponseMessage(msg);
        }
        catch( Exception exp ) {
            throw AxisFault.makeFault( exp );
        }
    }
}

