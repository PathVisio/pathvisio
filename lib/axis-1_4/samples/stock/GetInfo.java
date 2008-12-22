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

/**
 *
 * @author Doug Davis (dug@us.ibm.com.com)
 */
public class GetInfo {

  public static void main(String args[]) {
    try {
      Options opts = new Options( args );

      args = opts.getRemainingArgs();

      if ( args == null || args.length % 2 != 0 ) {
        System.err.println( "Usage: GetInfo <symbol> <datatype>" );
        System.exit(1);
      }

      String  symbol = args[0] ;
      Service  service = new Service();
      Call     call    = (Call) service.createCall();

      call.setTargetEndpointAddress( new java.net.URL(opts.getURL()) );
      call.setOperationName( new QName("urn:cominfo", "getInfo") );
      call.addParameter( "symbol", XMLType.XSD_STRING, ParameterMode.IN );
      call.addParameter( "info", XMLType.XSD_STRING, ParameterMode.IN );
      call.setReturnType( XMLType.XSD_STRING );
      call.setUsername( opts.getUser() );
      call.setPassword( opts.getPassword() );

      String res = (String) call.invoke( new Object[] { args[0], args[1] } );

      System.out.println( symbol + ": " + res );
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }

}

