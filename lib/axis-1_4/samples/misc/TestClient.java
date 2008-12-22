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

package samples.misc ;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.utils.Options;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;


/**
 *
 * @author Doug Davis (dug@us.ibm.com)
 * @author Glen Daniels (gdaniels@allaire.com)
 */
public class TestClient {
    public static String msg = "<SOAP-ENV:Envelope " +
        "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" > " +
        "<SOAP-ENV:Body>\n" +
        "<echo:Echo xmlns:echo=\"EchoService\">\n" +
        "<symbol>IBM</symbol>\n" +
        "</echo:Echo>\n" +
        "</SOAP-ENV:Body></SOAP-ENV:Envelope>\n";

    /**
     * Send a hardcoded message to the server, and print the response.
     *
     * @param args the command line arguments (mainly for specifying URL)
     * @param op an optional service argument, which will be used for
     * specifying the transport-level service
     */
    public static String doTest (String args[], String op) throws Exception {
      Options      opts    = new Options( args );
      String       url     = opts.getURL();
      String       action  = "EchoService" ;
        
      if (op != null) action = op;

      args = opts.getRemainingArgs();
      if ( args != null ) action = args[0];

      InputStream   input   = new ByteArrayInputStream(msg.getBytes());
      Service       service = new Service();
      Call          call    = (Call) service.createCall();
      SOAPEnvelope  env     = new SOAPEnvelope(input);

      call.setTargetEndpointAddress( new URL(url) );
      if (action != null) {
          call.setUseSOAPAction( true );
          call.setSOAPActionURI( action );
      }

      System.out.println( "Request:\n" + msg );

      env = call.invoke( env );

      System.out.println( "Response:\n" + env.toString() );
      return( env.toString() );
    }
    
  public static void main(String args[]) throws Exception{
    doTest(args, null);
  }
  public static void mainWithService(String args[], String service) throws Exception{
    doTest(args, service);
  }
}
