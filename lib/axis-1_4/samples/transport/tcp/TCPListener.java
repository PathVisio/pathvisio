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

package samples.transport.tcp;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.configuration.XMLStringProvider;
import org.apache.axis.deployment.wsdd.WSDDConstants;
import org.apache.axis.server.AxisServer;
import org.apache.axis.utils.Options;
import org.apache.commons.logging.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * Listen for incoming socket connections on the specified socket.  Take
 * incoming messages and dispatch them.
 *
 * @author Rob Jellinghaus (robj@unrealities.com)
 * @author Doug Davis (dug@us.ibm.com)
 */
public class TCPListener implements Runnable {
    static Log log =
            LogFactory.getLog(TCPSender.class.getName());

    // These have default values.
    private String transportName = "TCPTransport";

    private static final String AXIS_ENGINE = "AxisEngine" ;

    private int port;
    private ServerSocket srvSocket;

    private AxisEngine engine = null ;

    // becomes true when we want to quit
    private boolean done = false;

    static final String wsdd =
            "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" " +
                  "xmlns:java=\"" + WSDDConstants.URI_WSDD_JAVA + "\">\n" +
            " <transport name=\"tcp\" pivot=\"java:samples.transport.tcp.TCPSender\"/>\n" +
            " <service name=\"" + WSDDConstants.URI_WSDD + "\" provider=\"java:MSG\">\n" +
            "  <parameter name=\"allowedMethods\" value=\"AdminService\"/>\n" +
            "  <parameter name=\"className\" value=\"org.apache.axis.utils.Admin\"/>\n" +
            " </service>\n" +
            "</deployment>";

    public static void main (String args[]) {
        new TCPListener(args).run();
    }

    public TCPListener (String[] args) {
        // look for -p, -d arguments
        try {
            Options options = new Options(args);
            port = new URL(options.getURL()).getPort();
        } catch (MalformedURLException ex) {
            log.error("Hosed URL: "+ex);
            System.exit(1);
        }

        try {
            srvSocket = new ServerSocket(port);
        } catch (IOException ex) {
            log.error("Can't create server socket on port "+port);
            System.exit(1);
        }

        log.info("TCPListener is listening on port "+port+".");
    }

    public void run () {
        if (srvSocket == null) {
            return;
        }

        Socket sock;
        while (!done) {
            try {
                sock = srvSocket.accept();
                log.info("TCPListener received new connection: "+sock);
                new Thread(new SocketHandler(sock)).start();
            } catch (IOException ex) {
                /** stop complaining about this! it seems to happen on quit,
                    and is not worth mentioning.  unless I am confused. -- RobJ
                 */
                log.debug("Got IOException on srvSocket.accept: "+ex);
            }
        }
    }


    public class SocketHandler implements Runnable {
        private Socket socket;
        public SocketHandler (Socket socket) {
            this.socket = socket;
        }
        public void run () {
            // get the input stream
            if ( engine == null ) {
                XMLStringProvider provider = new XMLStringProvider(wsdd);
                engine = new AxisServer(provider);
                engine.init();
            }

            /* Place the Request message in the MessagContext object - notice */
            /* that we just leave it as a 'ServletRequest' object and let the  */
            /* Message processing routine convert it - we don't do it since we */
            /* don't know how it's going to be used - perhaps it might not     */
            /* even need to be parsed.                                         */
            /*******************************************************************/
            MessageContext    msgContext = new MessageContext(engine);

            InputStream inp;
            try {
                inp = socket.getInputStream();
            } catch (IOException ex) {
                log.error("Couldn't get input stream from "+socket);
                return;
            }

            // ROBJ 911
            // the plain ol' inputstream seems to hang in the SAX parse..... WHY?????
            // because there is no content length!
            //Message           msg        = new Message( nbbinp, "InputStream" );
            Message msg = null;
            try {
                StringBuffer line = new StringBuffer();
                int b = 0;
                while ((b = inp.read()) != '\r') {
                    line.append((char)b);
                }
                // got to '\r', skip it and '\n'
                if (inp.read() != '\n') {
                    log.error("Length line "+line+" was not terminated with \r\n");
                    return;
                }

                // TEST SUPPORT ONLY
                // If the line says "ping", then respond "\n".
                // If the line says "quit", then respond "\n" and exit.
                if (line.toString().equals("ping")) {
                    socket.getOutputStream().write(new String("\n").getBytes());
                    return;
                } else if (line.toString().equals("quit")) {
                    // peacefully die
                    socket.getOutputStream().write(new String("\n").getBytes());
                    socket.close();
                    // The following appears to deadlock.  It will get cleaned
                    // up on exit anyway...
                    // srvSocket.close();
                    log.error("AxisListener quitting.");
                    System.exit(0);
                }


                // OK, assume it is content length
                int len = Integer.parseInt(line.toString());
                // read that many bytes into ByteArrayInputStream...

                // experiment, doesn't work:
                //        NonBlockingBufferedInputStream nbbinp = new NonBlockingBufferedInputStream();
                //        nbbinp.setContentLength(len);
                //        nbbinp.setInputStream(inp);
                //        msg = new Message(nbbinp, "InputStream");

                byte[] mBytes = new byte[len];
                inp.read(mBytes);
                msg = new Message(new ByteArrayInputStream(mBytes));
            } catch (IOException ex) {
                log.error("Couldn't read from socket input stream: "+ex);
                return;
            }


            /* Set the request(incoming) message field in the context */
            /**********************************************************/
            msgContext.setRequestMessage( msg );

            /* Set the Transport Specific Request/Response chains IDs */
            /******************************************************/
            msgContext.setTransportName(transportName);

            try {
                /* Invoke the Axis engine... */
                /*****************************/
                engine.invoke( msgContext );
            }
            catch( Exception e ) {
                AxisFault fault = AxisFault.makeFault(e);
                msgContext.setResponseMessage( new Message(fault) );
            }

            /* Send it back along the wire...  */
            /***********************************/
            msg = msgContext.getResponseMessage();
            String response = null;
            if (msg == null) {
                response="No data";
            } else {
                try {
                    response = (String) msg.getSOAPPartAsString();
                } catch (AxisFault fault) {
                    msg = new Message(fault);
                    try {
                        response = (String)msg.getSOAPPartAsString();
                    } catch (AxisFault fault2) {
                        response = fault2.dumpToString();
                    }
                }
            }

            try {
                OutputStream buf = new BufferedOutputStream(socket.getOutputStream());
                // this should probably specify UTF-8, but for now, for Java interop,
                // use default encoding
                buf.write(response.getBytes());
                buf.close();
            } catch (IOException ex) {
                log.error("Can't write response to socket "+port+", response is: "+response);
            }
        }
    }
}


