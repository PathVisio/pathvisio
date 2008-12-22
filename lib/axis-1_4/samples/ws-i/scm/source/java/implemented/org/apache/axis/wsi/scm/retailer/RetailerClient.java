/*
 * Copyright 2002-2004 The Apache Software Foundation.
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
package org.apache.axis.wsi.scm.retailer;

import org.apache.axis.wsi.scm.retailer.catalog.CatalogItem;
import org.apache.axis.wsi.scm.retailer.catalog.CatalogType;

/**
 * Test client for RetailerService
 * 
 * @author Ias (iasandcb@tmax.co.kr)
 */
public class RetailerClient {
    public static void main(String[] args) throws Exception {
        // Make a service
        RetailerService service = new RetailerServiceLocator();

        // Now use the service to get a stub which implements the SEI.
        RetailerPortType port = service.getRetailerPort();
        CatalogType catalog = port.getCatalog();
        CatalogItem[] items = catalog.getItem();
        for (int i = 0; i < items.length; i++) {
            System.out.println("------------------");
            CatalogItem item = items[i];
            System.out.println(item.getBrand());
        }
    }
}
