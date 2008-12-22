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
 * Main.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis Wsdl2java emitter.
 */

package samples.userguide.example6;

public class Main {

    public static void main (String[] args) throws Exception {
        samples.userguide.example6.WidgetPrice binding = new WidgetPriceServiceLocator().getWidgetPrice();
        ((WidgetPriceSoapBindingStub)binding).setMaintainSession(true);
        try {
            ((WidgetPriceSoapBindingStub) binding).setWidgetPrice("FOO", "$1.00");
        } catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re );
        }
        try {
            java.lang.String value = null;
            value = binding.getWidgetPrice("FOO");
            if (value == null ||
                !value.equals("$1.00"))
                System.out.println("Wrong Price" + value);
        } catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re );
        }
    }
}

