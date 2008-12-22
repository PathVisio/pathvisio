package samples.transport ;

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

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;


/**
 * Just write the XML to a file called xml#.req and wait for
 * the result in a file called xml#.res
 *
 * Not thread-safe - just a dummy sample to show that we can indeed use
 * something other than HTTP as the transport.
 *
 * @author Doug Davis (dug@us.ibm.com)
 */

public class FileSender extends BasicHandler {
  static int nextNum = 1 ;

  public void invoke(MessageContext msgContext) throws AxisFault {
    Message  msg = msgContext.getRequestMessage();
    byte[]   buf = (byte[]) msg.getSOAPPartAsBytes();
    boolean timedOut = false;
    try {
      FileOutputStream fos = new FileOutputStream( "xml" + nextNum + ".req" );

      fos.write( buf );
      fos.close();
    }
    catch( Exception e ) {
      e.printStackTrace();
    }

    long timeout = Long.MAX_VALUE;
    if (msgContext.getTimeout()!=0)
      timeout=(new Date()).getTime()+msgContext.getTimeout();

    for (; timedOut == false;) {
      try {
        Thread.sleep( 100 );
        File file = new File( "xml" + nextNum + ".res" );

        if ((new Date().getTime())>=timeout)
            timedOut = true;

        if ( !file.exists() ) continue ;
        Thread.sleep( 100 );   // let the other side finish writing
        FileInputStream fis = new FileInputStream( "xml" + nextNum + ".res" );
        msg = new Message( fis );
        msg.getSOAPPartAsBytes();  // just flush the buffer
        fis.close();
         Thread.sleep( 100 );
        (new File("xml" + nextNum + ".res")).delete();
        msgContext.setResponseMessage( msg );
        break ;
      }
      catch( Exception e ) {
        // File not there - just loop
      }
    }
    nextNum++ ;
    if (timedOut)
        throw new AxisFault("timeout");

  }
}
