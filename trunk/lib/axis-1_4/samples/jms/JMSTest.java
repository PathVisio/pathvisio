/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

package samples.jms;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.XMLStringProvider;
import org.apache.axis.deployment.wsdd.WSDDConstants;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.transport.jms.JMSConstants;
import org.apache.axis.transport.jms.JMSTransport;
import org.apache.axis.transport.jms.SimpleJMSListener;
import org.apache.axis.utils.Options;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.util.HashMap;

/** Tests the JMS transport.  To run:
 *      java org.apache.axis.utils.Admin client client_deploy.xml
 *      java org.apache.axis.utils.Admin server deploy.xml
 *      java samples.transport.FileTest IBM
 *      java samples.transport.FileTest XXX
 *
 * JMSTest is a simple test driver for the JMS transport. It sets up a
 *   JMS listener, then calls a delayed quote service for each of the symbols
 *   specified on the command line.
 *
 * @author Jaime Meritt  (jmeritt@sonicsoftware.com)
 * @author Richard Chung (rchung@sonicsoftware.com)
 * @author Dave Chappell (chappell@sonicsoftware.com)
 */

public class JMSTest {
    static final String wsdd =
            "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" " +
                  "xmlns:java=\"" + WSDDConstants.URI_WSDD_JAVA + "\">\n" +
            " <transport name=\"JMSTransport\" pivot=\"java:org.apache.axis.transport.jms.JMSSender\"/>\n" +
            " <service name=\"" + WSDDConstants.URI_WSDD + "\" provider=\"java:MSG\">\n" +
            "  <parameter name=\"allowedMethods\" value=\"AdminService\"/>\n" +
            "  <parameter name=\"className\" value=\"org.apache.axis.utils.Admin\"/>\n" +
            " </service>\n" +
            "</deployment>";

    public static void main(String args[]) throws Exception {
        Options opts = new Options( args );

        // first check if we should print usage
        if ((opts.isFlagSet('?') > 0) || (opts.isFlagSet('h') > 0))
            printUsage();

        HashMap connectorMap = SimpleJMSListener.createConnectorMap(opts);
        HashMap cfMap = SimpleJMSListener.createCFMap(opts);
        String destination = opts.isValueSet('d');
        String username = opts.getUser();
        String password = opts.getPassword();
        // create the jms listener
        SimpleJMSListener listener = new SimpleJMSListener(connectorMap,
                                                           cfMap,
                                                           destination,
                                                           username,
                                                           password,
                                                           false);
        listener.start();

        args = opts.getRemainingArgs();
        if ( args == null || args.length == 0)
            printUsage();

        Service  service = new Service(new XMLStringProvider(wsdd));

        // create the transport
        JMSTransport transport = new JMSTransport(connectorMap, cfMap);

        // create a new Call object
        Call     call    = (Call) service.createCall();

        call.setOperationName( new QName("urn:xmltoday-delayed-quotes", "getQuote") );
        call.addParameter( "symbol", XMLType.XSD_STRING, ParameterMode.IN );
        call.setReturnType( XMLType.XSD_FLOAT );
        call.setTransport(transport);

        // set additional params on the call if desired
        //call.setUsername(username );
        //call.setPassword(password );
        //call.setProperty(JMSConstants.WAIT_FOR_RESPONSE, Boolean.FALSE);
        //call.setProperty(JMSConstants.PRIORITY, new Integer(5));
        //call.setProperty(JMSConstants.DELIVERY_MODE,
        //    new Integer(javax.jms.DeliveryMode.PERSISTENT));
        //call.setProperty(JMSConstants.TIME_TO_LIVE, new Long(20000));

        call.setProperty(JMSConstants.DESTINATION, destination);
        call.setTimeout(new Integer(10000));

        Float res = new Float(0.0F);

        // invoke a call for each of the symbols and print out
        for (int i = 0; i < args.length; i++)
        {
            try
            {
            res = (Float) call.invoke(new Object[] {args[i]});
            System.out.println(args[i] + ": " + res);
            }
            catch(AxisFault af)
            {
                System.out.println(af.dumpToString());
            }
        }

        // shutdown
        listener.shutdown();
        transport.shutdown();
    }

    public static void printUsage()
    {
        System.out.println("JMSTest: Tests JMS transport by obtaining stock quote");
        System.out.println("  Usage: JMSTest <symbol 1> <symbol 2> <symbol 3> ...");
        System.out.println("   Opts: -? this message");
        System.out.println();
        System.out.println("       -c connection factory properties filename");
        System.out.println("       -d destination");
        System.out.println("       -t topic [absence of -t indicates queue]");
        System.out.println();
        System.out.println("       -u username");
        System.out.println("       -w password");
        System.out.println();
        System.out.println("       -s single-threaded listener");
        System.out.println("          [absence of option => multithreaded]");

        System.exit(1);
    }
}
