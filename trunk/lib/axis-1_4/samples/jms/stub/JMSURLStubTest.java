/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
package samples.jms.stub;

import samples.jms.stub.xmltoday_delayed_quotes.*;
import org.apache.axis.AxisFault;
import org.apache.axis.utils.Options;
import org.apache.axis.transport.jms.JMSTransport;
import org.apache.axis.transport.jms.SimpleJMSListener;
import java.util.HashMap;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

/**
 * Demonstrates use of wsdl2java-generated static stubs to invoke JMS endpoints.
 *
 * The JMS listener is an intermediary that receives the JMS service request and
 * invokes the actual stock quote service over HTTP.
 *
 * @author Ray Chun (rchun@sonicsoftware.com)
*/

public class JMSURLStubTest extends TestCase {
    public JMSURLStubTest(String name) {
        super(name);
    }

    public static Float getQuote(String ticker) throws AxisFault {
        float quote = -1.0F;

        GetQuoteServiceLocator locator = new GetQuoteServiceLocator();
        GetQuote getQuote;

        try {
            getQuote = locator.getGetQuote();
        }
        catch (ServiceException e) {
            throw new AxisFault("JAX-RPC ServiceException caught: ", e);
        }
        assertTrue("getQuote is null", getQuote != null);

        try {
            quote = getQuote.getQuote(ticker);
            System.out.println("quote: " + quote);

            // close matching connectors
            // note: this is optional, as all connectors will be closed upon exit
            String endptAddr = locator.getGetQuoteAddress();
            JMSTransport.closeMatchingJMSConnectors(endptAddr, null, null);
        }
        catch (RemoteException e) {
            throw new AxisFault("Remote Exception caught: ", e);
        }
        return new Float(quote);
    }

    public static void printUsage()
    {
        System.out.println("JMSURLStubTest: Tests JMS transport by obtaining stock quote using wsdl2java-generated stub classes");
        System.out.println("  Usage: JMSURLStubTest <symbol 1> <symbol 2> <symbol 3> ...");
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

    /**
     * Conn args are still required to set up the JMS listener
     */
    public static void main(String[] args) throws Exception
    {
        Options opts = new Options( args );

        // first check if we should print usage
        if ((opts.isFlagSet('?') > 0) || (opts.isFlagSet('h') > 0))
            printUsage();

        String username = opts.getUser();
        String password = opts.getPassword();

        HashMap connectorMap = SimpleJMSListener.createConnectorMap(opts);
        HashMap cfMap = SimpleJMSListener.createCFMap(opts);
        String destination = opts.isValueSet('d');

        args = opts.getRemainingArgs();
        if ( args == null || args.length == 0)
            printUsage();

        // create the jms listener
        SimpleJMSListener listener = new SimpleJMSListener(connectorMap,
                                                           cfMap,
                                                           destination,
                                                           username,
                                                           password,
                                                           false);
        listener.start();

        JMSURLStubTest stubTest = new JMSURLStubTest("JMS URL static stub test");

        for (int i = 0; i < args.length; i++)
        {
            try
            {
                Float quote = stubTest.getQuote(args[i]);
                System.out.println(args[i] + ": " + quote);
            }
            catch(AxisFault af)
            {
                System.out.println(af.dumpToString());
            }
        }

        listener.shutdown();

        System.exit(1);
    }
}
