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

package samples.transport.tcp ;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Call;
import org.apache.axis.configuration.DefaultEngineConfigurationFactory;
import org.apache.axis.configuration.SimpleProvider;

/**
 * An admin client object, which will work with the TCP transport.
 *
 * @author Rob Jellinghaus (robj@unrealities.com)
 * @author Doug Davis (dug@us.ibm.com)
 * @author Glen Daniels (gdaniels@apache.org)
 */

public class AdminClient extends org.apache.axis.client.AdminClient {
    public static void main(String args[]) {

        Call.addTransportPackage("samples.transport");
        Call.setTransportForProtocol("tcp", TCPTransport.class);

        // Deploy the transport on top of the default client configuration.
        EngineConfiguration defaultConfig = 
            (new DefaultEngineConfigurationFactory()).
            getClientEngineConfig();
        SimpleProvider config = new SimpleProvider(defaultConfig);
        SimpleTargetedChain c = new SimpleTargetedChain(new TCPSender());
        config.deployTransport("tcp", c);

        AdminClient.setDefaultConfiguration(config);

        try {
            org.apache.axis.client.AdminClient client =
                new org.apache.axis.client.AdminClient(true);

            System.out.println(client.process(args));
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace( System.err );
        }
    }
}

