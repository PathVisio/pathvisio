package samples.encoding;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.net.URL;

public class TestElem {
    static String xml = "<x:hello xmlns:x=\"urn:foo\">a string</x:hello>" ;

    public static String doit(String[] args,String xml) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

        String  sURL = "http://" + args[0] + ":" + args[1] + "/axis/services/ElementService" ;
        QName   sqn  = new QName(sURL, "ElementService" );
        QName   pqn  = new QName(sURL, "ElementService" );

        //Service service=new Service(new URL("file:ElementService.wsdl"),sqn);
        Service service = new Service(new URL(sURL+"?wsdl"),sqn);
        Call    call    = (Call) service.createCall( pqn, "echoElement" );

        Options opts = new Options(args);
        opts.setDefaultURL( call.getTargetEndpointAddress() );
        call.setTargetEndpointAddress( new URL(opts.getURL()) );

        Element elem = XMLUtils.newDocument(bais).getDocumentElement();

        elem = (Element) call.invoke( new Object[] { "a string", elem } );
        return( XMLUtils.ElementToString( elem ) );
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Sent: " + xml );
        String res = doit(args, xml);
        System.out.println("Returned: " + res );
    }
}
