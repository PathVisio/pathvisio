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

package samples.jaxrpc;

import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;

/**
 * This version of GetInfo is a near-duplicate of the GetInfo class in
 * samples/stock.  This version is strictly JAX-RPC compliant.  It uses
 * no AXIS enhancements.
 *
 * @author Russell Butek (butek@us.ibm.com)
 */
public class GetInfo {

    public static void main(String args[]) throws Exception {
        Options opts = new Options(args);

        args = opts.getRemainingArgs();

        if (args == null || args.length % 2 != 0) {
            System.err.println("Usage: GetInfo <symbol> <datatype>");
            System.exit(1);
        }

        String  symbol  = args[0];
        Service service = ServiceFactory.newInstance().createService(null);
        Call    call    = service.createCall();

        call.setTargetEndpointAddress(opts.getURL());
        call.setOperationName(new QName("urn:cominfo", "getInfo"));
        call.addParameter("symbol", XMLType.XSD_STRING, ParameterMode.IN);
        call.addParameter("info", XMLType.XSD_STRING, ParameterMode.IN);
        call.setReturnType(XMLType.XSD_STRING);
        if(opts.getUser()!=null)
            call.setProperty(Call.USERNAME_PROPERTY, opts.getUser());
        if(opts.getPassword()!=null)
            call.setProperty(Call.PASSWORD_PROPERTY, opts.getPassword());

        String res = (String) call.invoke(new Object[] {args[0], args[1]});

        System.out.println(symbol + ": " + res);
    } // main
}

