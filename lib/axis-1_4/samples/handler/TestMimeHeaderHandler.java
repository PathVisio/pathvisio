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

package samples.handler;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

public class TestMimeHeaderHandler extends BasicHandler {

	public void invoke(MessageContext msgContext) throws AxisFault {
		Message requestMessage = msgContext.getRequestMessage();
		Message responseMessage = new Message(requestMessage.getSOAPEnvelope());
        String[] fooHeader = requestMessage.getMimeHeaders().getHeader("foo");
        if (fooHeader != null) {
            responseMessage.getMimeHeaders().addHeader("foo", fooHeader[0]);
        }
		msgContext.setResponseMessage(responseMessage);
	}
}
