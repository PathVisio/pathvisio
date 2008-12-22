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

/**
 * WidgetPriceServiceTestCase.java
 *
 */

package samples.userguide.example6;

public class WidgetPriceServiceTestCase extends junit.framework.TestCase {
    public WidgetPriceServiceTestCase(String name) {
        super(name);
    }

    public void testWidgetPrice() {
        samples.userguide.example6.WidgetPrice binding;
        try {
            binding = new WidgetPriceServiceLocator().getWidgetPrice();
        } catch (javax.xml.rpc.ServiceException jre) {
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre );
        }
        ((WidgetPriceSoapBindingStub)binding).setMaintainSession(true);
        assertTrue("binding is null", binding != null);
        try {
            binding.setWidgetPrice("FOO", "$1.00");
        } catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re );
        }
        try {
            java.lang.String value = null;
            value = binding.getWidgetPrice("FOO");
            assertTrue("Wrong Price" + value, value.equals("$1.00"));
        } catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re );
        }
    }
}

