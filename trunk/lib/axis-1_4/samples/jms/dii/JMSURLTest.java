package samples.jms.dii;

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

/**
 * Demonstrates use of a JMS URL endpoint address to drive the JMS transport.
 *
 * The JMS listener is an intermediary that receives the JMS service request and
 * invokes the actual stock quote service over HTTP.
 *
 * @author Ray Chun (rchun@sonicsoftware.com)
 */

public class JMSURLTest {
    static final String wsdd =
            "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" " +
                  "xmlns:java=\"" + WSDDConstants.URI_WSDD_JAVA + "\">\n" +
            " <transport name=\"JMSTransport\" pivot=\"java:org.apache.axis.transport.jms.JMSSender\"/>\n" +
            " <service name=\"" + WSDDConstants.URI_WSDD + "\" provider=\"java:MSG\">\n" +
            "  <parameter name=\"allowedMethods\" value=\"AdminService\"/>\n" +
            "  <parameter name=\"className\" value=\"org.apache.axis.utils.Admin\"/>\n" +
            " </service>\n" +
            "</deployment>";

    // the JMS URL target endpoint address
    static String sampleJmsUrl = "jms:/MyQ?" +
                                 "vendor=JNDI" +
                                 "&java.naming.factory.initial=com.sun.jndi.fscontext.RefFSContextFactory" +
                                 "&java.naming.provider.url=file:///c:/JNDIStore" +
                                 "&ConnectionFactoryJNDIName=MyCF" +
                                 "&deliveryMode=persistent" +
                                 "&priority=5" +
                                 "&ttl=10000" +
                                 "&debug=true";
    /*
    // example using Sonic
    static String sampleJmsUrl = "jms:/SampleQ1?" +
                                 "vendor=SonicMQ" +
                                 "&brokerURL=localhost:2506" +
                                 "&deliveryMode=persistent" +
                                 "&priority=5" +
                                 "&ttl=10000";
    */

    public static void main(String args[]) throws Exception {
        Options opts = new Options( args );

        // first check if we should print usage
        if ((opts.isFlagSet('?') > 0) || (opts.isFlagSet('h') > 0))
            printUsage();

        String username = opts.getUser();
        String password = opts.getPassword();

        HashMap connectorMap = SimpleJMSListener.createConnectorMap(opts);
        HashMap cfMap = SimpleJMSListener.createCFMap(opts);
        String destination = opts.isValueSet('d');

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

        for (int i = 0; i < args.length; i++)
        {
            try
            {
                Float res = getQuote(args[i], username, password);
                System.out.println(args[i] + ": " + res);
            }
            catch(AxisFault af)
            {
                System.out.println(af.dumpToString());
            }
        }

        // shutdown
        listener.shutdown();

        // close all JMSConnectors whose configuration matches that of the JMS URL
        // note: this is optional, as all connectors will be closed upon exit
        JMSTransport.closeMatchingJMSConnectors(sampleJmsUrl, username, password);

        System.exit(1);
    }

    public static Float getQuote(String ticker, String username, String password)
        throws javax.xml.rpc.ServiceException, AxisFault
    {
        Float res = new Float(-1.0);

        Service  service = new Service(new XMLStringProvider(wsdd));

        // create a new Call object
        Call     call    = (Call) service.createCall();
        call.setOperationName( new QName("urn:xmltoday-delayed-quotes", "getQuote") );
        call.addParameter( "symbol", XMLType.XSD_STRING, ParameterMode.IN );
        call.setReturnType( XMLType.XSD_FLOAT );

        try
        {
            java.net.URL jmsurl = new java.net.URL(sampleJmsUrl);
            call.setTargetEndpointAddress(jmsurl);

            // set additional params on the call if desired
            call.setUsername(username);
            call.setPassword(password);
            call.setTimeout(new Integer(30000));

            res = (Float) call.invoke(new Object[] {ticker});
        }
        catch (java.net.MalformedURLException e)
        {
            throw new AxisFault("Invalid JMS URL", e);
        }
        catch (java.rmi.RemoteException e)
        {
            throw new AxisFault("Failed in getQuote()", e);
        }

        return res;
    }

    public static void printUsage()
    {
        System.out.println("JMSURLTest: Tests JMS transport by obtaining stock quote");
        System.out.println("  Usage: JMSURLTest <symbol 1> <symbol 2> <symbol 3> ...");
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
