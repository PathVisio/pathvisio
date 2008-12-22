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

package samples.jaxrpc;

import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import java.net.URL;

/**
 * This version of the ever so popular GetQuote is a near-duplicate of
 * the GetQuote1 method in samples/stock which shows how to use the AXIS
 * client APIs with and without WSDL.  This version is strictly JAX-RPC
 * compliant.  It uses no AXIS enhancements.
 *
 * This sample supports the use of the standard options too (-p ...)
 *
 * @author Russell Butek (butek@us.ibm.com)
 */
public class GetQuote1 {
    public  String symbol;

    /**
     * This will use the WSDL to prefill all of the info needed to make
     * the call.  All that's left is filling in the args to invoke().
     */
    public float getQuote1(String args[]) throws Exception {
        Options opts = new Options(args);

        args = opts.getRemainingArgs();

        if (args == null) {
            System.err.println("Usage: GetQuote <symbol>");
            System.exit(1);
        }

        /* Define the service QName and port QName */
        /*******************************************/
        QName servQN = new QName("urn:xmltoday-delayed-quotes",
                "GetQuoteService");
        QName portQN = new QName("urn:xmltoday-delayed-quotes", "GetQuote");

        /* Now use those QNames as pointers into the WSDL doc */
        /******************************************************/
        Service service = ServiceFactory.newInstance().createService(
                new URL("file:samples/stock/GetQuote.wsdl"), servQN);
        Call call = service.createCall(portQN, "getQuote");

        /* Strange - but allows the user to change just certain portions of */
        /* the URL we're gonna use to invoke the service.  Useful when you  */
        /* want to run it thru tcpmon (ie. put  -p81 on the cmd line).      */
        /********************************************************************/
        opts.setDefaultURL(call.getTargetEndpointAddress());
        call.setTargetEndpointAddress(opts.getURL());

        /* Define some service specific properties */
        /*******************************************/
        call.setProperty(Call.USERNAME_PROPERTY, opts.getUser());
        call.setProperty(Call.PASSWORD_PROPERTY, opts.getPassword());

        /* Get symbol and invoke the service */
        /*************************************/
        Object result = call.invoke(new Object[] {symbol = args[0]});

        return ((Float) result).floatValue();
    } // getQuote1

    /**
     * This will do everything manually (ie. no WSDL).
     */
    public float getQuote2(String args[]) throws Exception {
        Options opts = new Options(args);

        args = opts.getRemainingArgs();

        if (args == null) {
            System.err.println("Usage: GetQuote <symbol>");
            System.exit(1);
        }

        /* Create default/empty Service and Call object */
        /************************************************/
        Service service = ServiceFactory.newInstance().createService(null);
        Call call = service.createCall();

        /* Strange - but allows the user to change just certain portions of */
        /* the URL we're gonna use to invoke the service.  Useful when you  */
        /* want to run it thru tcpmon (ie. put  -p81 on the cmd line).      */
        /********************************************************************/
        opts.setDefaultURL("http://localhost:8080/axis/servlet/AxisServlet");

        /* Set all of the stuff that would normally come from WSDL */
        /***********************************************************/
        call.setTargetEndpointAddress(opts.getURL());
        call.setProperty(Call.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        call.setProperty(Call.SOAPACTION_URI_PROPERTY, "getQuote");
        call.setProperty(Call.ENCODINGSTYLE_URI_PROPERTY,
                "http://schemas.xmlsoap.org/soap/encoding/");
        call.setOperationName(new QName("urn:xmltoday-delayed-quotes", "getQuote"));
        call.addParameter("symbol", XMLType.XSD_STRING, ParameterMode.IN);
        call.setReturnType(XMLType.XSD_FLOAT);

        /* Define some service specific properties */
        /*******************************************/
        call.setProperty(Call.USERNAME_PROPERTY, opts.getUser());
        call.setProperty(Call.PASSWORD_PROPERTY, opts.getPassword());

        /* Get symbol and invoke the service */
        /*************************************/
        Object result = call.invoke(new Object[] {symbol = args[0]});

        return ((Float) result).floatValue();
    } // getQuote2

    /**
     * This method does the same thing that getQuote1 does, but in
     * addition it reuses the Call object to make another call.
     */
    public float getQuote3(String args[]) throws Exception {
        Options opts = new Options(args);

        args = opts.getRemainingArgs();

        if (args == null) {
            System.err.println("Usage: GetQuote <symbol>");
            System.exit(1);
        }

        /* Define the service QName and port QName */
        /*******************************************/
        QName servQN = new QName("urn:xmltoday-delayed-quotes",
                "GetQuoteService");
        QName portQN = new QName("urn:xmltoday-delayed-quotes", "GetQuote");

        /* Now use those QNames as pointers into the WSDL doc */
        /******************************************************/
        Service service = ServiceFactory.newInstance().createService(
                new URL("file:samples/stock/GetQuote.wsdl"), servQN);
        Call call = service.createCall(portQN, "getQuote");

        /* Strange - but allows the user to change just certain portions of */
        /* the URL we're gonna use to invoke the service.  Useful when you  */
        /* want to run it thru tcpmon (ie. put  -p81 on the cmd line).      */
        /********************************************************************/
        opts.setDefaultURL(call.getTargetEndpointAddress());
        call.setTargetEndpointAddress(opts.getURL());

        /* Define some service specific properties */
        /*******************************************/
        call.setProperty(Call.USERNAME_PROPERTY, opts.getUser());
        call.setProperty(Call.PASSWORD_PROPERTY, opts.getPassword());

        /* Get symbol and invoke the service */
        /*************************************/
        Object result = call.invoke(new Object[] {symbol = args[0]});

        /* Reuse the Call object for a different call */
        /**********************************************/
        call.setOperationName(new QName("urn:xmltoday-delayed-quotes", "test"));
        call.removeAllParameters();
        call.setReturnType(XMLType.XSD_STRING);

        System.out.println(call.invoke(new Object[]{}));
        return ((Float) result).floatValue();
    } // getQuote3

    public static void main(String args[]) throws Exception {
        String    save_args[] = new String[args.length];
        float     val;
        GetQuote1 gq = new GetQuote1();

        /* Call the getQuote() that uses the WDSL */
        /******************************************/
        System.out.println("Using WSDL");
        System.arraycopy(args, 0, save_args, 0, args.length);
        val = gq.getQuote1(args);
        System.out.println(gq.symbol + ": " + val);

        /* Call the getQuote() that does it all manually */
        /*************************************************/
        System.out.println("Manually");
        System.arraycopy(save_args, 0, args, 0, args.length);
        val = gq.getQuote2(args);
        System.out.println(gq.symbol + ": " + val);

        /* Call the getQuote() that uses Axis's generated WSDL */
        /*******************************************************/
        System.out.println("WSDL + Reuse Call");
        System.arraycopy(save_args, 0, args, 0, args.length);
        val = gq.getQuote3(args);
        System.out.println(gq.symbol + ": " + val);
    } // main
}
