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

package samples.stock ;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.utils.Options;

import javax.xml.namespace.QName;
import java.net.URL;

public class GetQuote2 {
    public  String   symbol ;
    
    /**
     * This will use the WSDL to prefill all of the info needed to make
     * the call.  All that's left is filling in the args to invoke().
     */
    public float getQuote(String args[]) throws Exception {
      Options  opts = new Options( args );

      args = opts.getRemainingArgs();

      if ( args == null ) {
        System.err.println( "Usage: GetQuote <symbol>" );
        System.exit(1);
      }

      /* Define the service QName and port QName */
      /*******************************************/
      QName servQN = new QName("urn:xmltoday-delayed-quotes","GetQuoteService");
      QName portQN = new QName("urn:xmltoday-delayed-quotes","GetQuoteJava");

      /* Now use those QNames as pointers into the WSDL doc */
      /******************************************************/
      Service service = new Service( new URL("file:GetQuote.wsdl"), servQN );
      Call    call    = (Call) service.createCall( portQN, "getQuote" );

      /* Strange - but allows the user to change just certain portions of */
      /* the URL we're gonna use to invoke the service.  Useful when you  */
      /* want to run it thru tcpmon (ie. put  -p81 on the cmd line).      */
      /********************************************************************/
      opts.setDefaultURL( call.getTargetEndpointAddress() );
      call.setTargetEndpointAddress( new URL(opts.getURL()) );

      /* Get symbol and invoke the service */
      /*************************************/
      Object result = call.invoke( new Object[] { symbol = args[0] } );

      return( ((Float) result).floatValue() );
    }

    public static void main(String args[]) {
      try {
          String    save_args[] = new String[args.length];
          float     val ;
          GetQuote2 gq  = new GetQuote2();

          /* Call the getQuote() that uses the WDSL */
          /******************************************/
          System.out.println("Using Java binding in WSDL");
          System.arraycopy( args, 0, save_args, 0, args.length );
          val = gq.getQuote( args );
          System.out.println( gq.symbol + ": " + val );
      }
      catch( Exception e ) {
          e.printStackTrace();
      }
    }
};
