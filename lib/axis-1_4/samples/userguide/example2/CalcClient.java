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

package samples.userguide.example2 ;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;

import javax.xml.rpc.ParameterMode;

public class CalcClient
{
   public static void main(String [] args) throws Exception {
       Options options = new Options(args);
       
       String endpoint = "http://localhost:" + options.getPort() +
                         "/axis/Calculator.jws";
       
       args = options.getRemainingArgs();
       
       if (args == null || args.length != 3) {
           System.err.println("Usage: CalcClient <add|subtract> arg1 arg2");
           return;
       }
       
       String method = args[0];
       if (!(method.equals("add") || method.equals("subtract"))) {
           System.err.println("Usage: CalcClient <add|subtract> arg1 arg2");
           return;
       }
       
       Integer i1 = new Integer(args[1]);
       Integer i2 = new Integer(args[2]);

       Service  service = new Service();
       Call     call    = (Call) service.createCall();

       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.setOperationName( method );
       call.addParameter( "op1", XMLType.XSD_INT, ParameterMode.IN );
       call.addParameter( "op2", XMLType.XSD_INT, ParameterMode.IN );
       call.setReturnType( XMLType.XSD_INT );

       Integer ret = (Integer) call.invoke( new Object [] { i1, i2 });
       
       System.out.println("Got result : " + ret);
   }
}
