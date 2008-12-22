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
import org.apache.axis.server.AxisServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Waits for the XML to appear in a file called xml#.req and writes
 * the response in a file called xml#.res
 *
 * @author Doug Davis (dug@us.ibm.com)
 */
public class FileReader extends Thread {
  static int      nextNum = 1 ;
  boolean  pleaseStop = false ;
  boolean  halted     = false ;

  public void run() {
    AxisServer  server = new AxisServer();
    server.init();

    while( !pleaseStop ) {
      try {
        Thread.sleep( 100 );
        File file = new File( "xml" + nextNum + ".req" );
        if ( !file.exists() ) continue ;
          
          // avoid race condition where file comes to exist but we were halted -- RobJ
          if (pleaseStop) continue;

        Thread.sleep( 100 );   // let the other side finish writing
        FileInputStream fis = new FileInputStream( file );

        int thisNum = nextNum++; // increment early to avoid infinite loops
        
        Message msg = new Message( fis );
        msg.getSOAPPartAsBytes();

        fis.close();
        file.delete();

        MessageContext  msgContext = new MessageContext(server);
        msgContext.setRequestMessage( msg );

        try {
            server.invoke( msgContext );
            msg = msgContext.getResponseMessage();
        } catch (AxisFault af) {
            msg = new Message(af);
            msg.setMessageContext(msgContext);
        } catch (Exception e) {
            msg = new Message(new AxisFault(e.toString()));
            msg.setMessageContext(msgContext);
        }
        
        byte[] buf = msg.getSOAPPartAsBytes();
        FileOutputStream fos = new FileOutputStream( "xml" + thisNum + ".res" );
        fos.write( buf );
        fos.close();
      }
      catch( Exception e ) {
        if ( !(e instanceof FileNotFoundException) )
          e.printStackTrace();
      }
    }

    halted = true;
    System.out.println("FileReader halted.");
  }

  public void halt() {
    pleaseStop = true ;
    while (!halted) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        break;
      }
    }
  }
}
