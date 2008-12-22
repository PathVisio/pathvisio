package samples.transport ;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.XMLStringProvider;
import org.apache.axis.deployment.wsdd.WSDDConstants;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/** Tests the simple File transport.  To run:
 *      java org.apache.axis.utils.Admin client client_deploy.xml
 *      java org.apache.axis.utils.Admin server deploy.xml
 *      java samples.transport.FileTest IBM
 *      java samples.transport.FileTest XXX
 */

public class FileTest {
    static final String wsdd =
            "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" " +
                  "xmlns:java=\"" + WSDDConstants.URI_WSDD_JAVA + "\">\n" +
            " <transport name=\"FileTransport\" pivot=\"java:samples.transport.FileSender\"/>\n" +
            " <service name=\"" + WSDDConstants.URI_WSDD + "\" provider=\"java:MSG\">\n" +
            "  <parameter name=\"allowedMethods\" value=\"AdminService\"/>\n" +
            "  <parameter name=\"className\" value=\"org.apache.axis.utils.Admin\"/>\n" +
            " </service>\n" +
            "</deployment>";

    public static void main(String args[]) throws Exception {
        FileReader  reader = new FileReader();
        reader.setDaemon(true);
        reader.start();

        Options opts = new Options( args );

        args = opts.getRemainingArgs();

        if ( args == null ) {
            System.err.println( "Usage: GetQuote <symbol>" );
            System.exit(1);
        }

        String   symbol = args[0] ;
        Service  service = new Service(new XMLStringProvider(wsdd));
        Call     call    = (Call) service.createCall();

        call.setOperationName( new QName("urn:xmltoday-delayed-quotes", "getQuote") );
        call.addParameter( "symbol", XMLType.XSD_STRING, ParameterMode.IN );
        call.setReturnType( XMLType.XSD_FLOAT );
        call.setTransport( new FileTransport() );
        call.setUsername(opts.getUser() );
        call.setPassword(opts.getPassword() );
        call.setTimeout(new Integer(10000));

        Float res = new Float(0.0F);
        res = (Float) call.invoke( new Object[] {symbol} );

        System.out.println( symbol + ": " + res );

        reader.halt();
    }
}
