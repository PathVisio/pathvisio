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

package samples.userguide.example4;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;

public class LogHandler extends BasicHandler {
    public void invoke(MessageContext msgContext) throws AxisFault
    {
        /** Log an access each time we get invoked.
         */
        try {
            Handler serviceHandler = msgContext.getService();
            String filename = (String)getOption("filename");
            if ((filename == null) || (filename.equals("")))
                throw new AxisFault("Server.NoLogFile",
                                 "No log file configured for the LogHandler!",
                                    null, null);
            FileOutputStream fos = new FileOutputStream(filename, true);
            
            PrintWriter writer = new PrintWriter(fos);
            
            Integer numAccesses =
                             (Integer)serviceHandler.getOption("accesses");
            if (numAccesses == null)
                numAccesses = new Integer(0);
            
            numAccesses = new Integer(numAccesses.intValue() + 1);
            
            Date date = new Date();
            String result = date + ": service " +
                            msgContext.getTargetService() +
                            " accessed " + numAccesses + " time(s).";
            serviceHandler.setOption("accesses", numAccesses);
            
            writer.println(result);
            
            writer.close();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
