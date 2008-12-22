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

import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;

public class Client {
    public static void main(String[] args) throws Exception {
        try {
            Options opts = new Options(args);

            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(opts.getURL()));

            SOAPEnvelope env = new SOAPEnvelope();
            SOAPBodyElement sbe = new SOAPBodyElement(XMLUtils.StringToElement("http://localhost:8080/LogTestService", "testMethod", ""));
            env.addBodyElement(sbe);

            env = new SignedSOAPEnvelope(env, "http://xml-security");

            System.out.println("\n============= Request ==============");
            XMLUtils.PrettyElementToStream(env.getAsDOM(), System.out);

            call.invoke(env);

            MessageContext mc = call.getMessageContext();
            System.out.println("\n============= Response ==============");
            XMLUtils.PrettyElementToStream(mc.getResponseMessage().getSOAPEnvelope().getAsDOM(), System.out);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
