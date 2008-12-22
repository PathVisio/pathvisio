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
 * WidgetPriceSoapBindingImpl.java
 *
 * This file is the SOAP implementation of the WidgetPrice Web Service
 */

package samples.userguide.example6;

import java.util.HashMap;

public class WidgetPriceSoapBindingImpl implements samples.userguide.example6.WidgetPrice {
    HashMap table = new HashMap();
    public void setWidgetPrice(java.lang.String name, java.lang.String price) throws java.rmi.RemoteException {
        table.put(name, price);

    }
    public java.lang.String getWidgetPrice(java.lang.String name) throws java.rmi.RemoteException {
        return (String) table.get(name);
    }
}
