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

package samples.userguide.example5;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.utils.Options;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
                                           
public class Client
{
    public static void main(String [] args) throws Exception
    {
        Options options = new Options(args);
        
        Order order = new Order();
        order.setCustomerName("Glen Daniels");
        order.setShippingAddress("275 Grove Street, Newton, MA");
        
        String [] items = new String[] { "mp3jukebox", "1600mahBattery" };
        int [] quantities = new int [] { 1, 4 };
        
        order.setItemCodes(items);
        order.setQuantities(quantities);
        
        Service  service = new Service();
        Call     call    = (Call) service.createCall();
        QName    qn      = new QName( "urn:BeanService", "Order" );

        call.registerTypeMapping(Order.class, qn,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(Order.class, qn),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(Order.class, qn));        
        String result;
        try {
            call.setTargetEndpointAddress( new java.net.URL(options.getURL()) );
            call.setOperationName( new QName("OrderProcessor", "processOrder") );
            call.addParameter( "arg1", qn, ParameterMode.IN );
            call.setReturnType( org.apache.axis.encoding.XMLType.XSD_STRING );

            result = (String) call.invoke( new Object[] { order } );
        } catch (AxisFault fault) {
            result = "Error : " + fault.toString();
        }
        
        System.out.println(result);
    }
}
