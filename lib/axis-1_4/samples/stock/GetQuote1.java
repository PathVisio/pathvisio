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
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.net.URL;

/**
 * This version of the ever so popular GetQuote shows how to use the
 * Axis client APIs with and without WSDL.  The first flavor (getQuote1)
 * will use WSDL to prefill all of the data about the remote service.
 * The second one (getQuote2) will do it all manually.  Either way the
 * service is invoked it should produce the exact same request XML and
 * of course same results.
 *
 * This sample supports the use of the standard options too (-p ...)
 *
 * @author Doug Davis (dug@us.ibm.com.com)
 */
public class GetQuote1 {
    public  String   symbol ;
    
    /**
     * This will use the WSDL to prefill all of the info needed to make
     * the call.  All that's left is filling in the args to invoke().
     */
    public float getQuote1(String args[]) throws Exception {
      Options  opts = new Options( args );

      args = opts.getRemainingArgs();

      if ( args == null ) {
        System.err.println( "Usage: GetQuote <symbol>" );
        System.exit(1);
      }

      /* Define the service QName and port QName */
      /*******************************************/
      QName servQN = new QName("urn:xmltoday-delayed-quotes","GetQuoteService");
      QName portQN = new QName("urn:xmltoday-delayed-quotes","GetQuote");

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

      /* Define some service specific properties */
      /*******************************************/
      call.setUsername( opts.getUser() );
      call.setPassword( opts.getPassword() );

      /* Get symbol and invoke the service */
      /*************************************/
        Object result = call.invoke( new Object[] { symbol = args[0] } );

      return( ((Float) result).floatValue() );
    }

    /**
     * This will do everything manually (ie. no WSDL).
     */
    public float getQuote2(String args[]) throws Exception {
      Options  opts    = new Options( args );

      args = opts.getRemainingArgs();

      if ( args == null ) {
        System.err.println( "Usage: GetQuote <symbol>" );
        System.exit(1);
      }

      /* Create default/empty Service and Call object */
      /************************************************/
      Service  service = new Service();
      Call     call    = (Call) service.createCall();

      /* Strange - but allows the user to change just certain portions of */
      /* the URL we're gonna use to invoke the service.  Useful when you  */
      /* want to run it thru tcpmon (ie. put  -p81 on the cmd line).      */
      /********************************************************************/
      opts.setDefaultURL( "http://localhost:8080/axis/servlet/AxisServlet" );

      /* Set all of the stuff that would normally come from WSDL */
      /***********************************************************/
      call.setTargetEndpointAddress( new URL(opts.getURL()) );
      call.setUseSOAPAction( true );
      call.setSOAPActionURI( "getQuote" );
      call.setEncodingStyle( "http://schemas.xmlsoap.org/soap/encoding/" );
      call.setOperationName( new QName("urn:xmltoday-delayed-quotes", "getQuote") );
      call.addParameter( "symbol", XMLType.XSD_STRING, ParameterMode.IN );
      call.setReturnType( XMLType.XSD_FLOAT );

      /* Define some service specific properties */
      /*******************************************/
      call.setUsername( opts.getUser() );
      call.setPassword( opts.getPassword() );

      /* Get symbol and invoke the service */
      /*************************************/
      Object result = call.invoke( new Object[] { symbol = args[0] } );

      return( ((Float) result).floatValue() );
    }

    /**
     * This will use the WSDL to prefill all of the info needed to make
     * the call.  All that's left is filling in the args to invoke().
     */
    public float getQuote3(String args[]) throws Exception {
      Options  opts = new Options( args );

      args = opts.getRemainingArgs();

      if ( args == null ) {
        System.err.println( "Usage: GetQuote <symbol>" );
        System.exit(1);
      }

      /* Define the service QName and port QName */
      /*******************************************/
      QName servQN = new QName("urn:xmltoday-delayed-quotes","GetQuoteService");
      QName portQN = new QName("urn:xmltoday-delayed-quotes","GetQuote");

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

      /* Define some service specific properties */
      /*******************************************/
      call.setUsername( opts.getUser() );
      call.setPassword( opts.getPassword() );

      /* Get symbol and invoke the service */
      /*************************************/
      Object result = call.invoke( new Object[] { symbol = args[0] } );
      result = call.invoke( new Object[] { symbol = args[0] } );

      /* Reuse the call object to call the test method */
      /*************************************************/
      call.setOperation( portQN, "test" );
      call.setReturnType( XMLType.XSD_STRING );

      System.out.println( call.invoke(new Object[]{}) );

      return( ((Float) result).floatValue() );
    }

    public static void main(String args[]) {
      try {
          String    save_args[] = new String[args.length];
          float     val ;
          GetQuote1 gq  = new GetQuote1();

          /* Call the getQuote() that uses the WDSL */
          /******************************************/
          System.out.println("Using WSDL");
          System.arraycopy( args, 0, save_args, 0, args.length );
          val = gq.getQuote1( args );
          System.out.println( gq.symbol + ": " + val );

          /* Call the getQuote() that does it all manually */
          /*************************************************/
          System.out.println("Manually");
          System.arraycopy( save_args, 0, args, 0, args.length );
          val = gq.getQuote2( args );
          System.out.println( gq.symbol + ": " + val );

          /* Call the getQuote() that uses Axis's generated WSDL */
          /*******************************************************/
          System.out.println("WSDL + Reuse Call");
          System.arraycopy( save_args, 0, args, 0, args.length );
          val = gq.getQuote3( args );
          System.out.println( gq.symbol + ": " + val );
      }
      catch( Exception e ) {
          e.printStackTrace();
      }
    }
};
