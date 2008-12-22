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

import java.math.BigDecimal;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;

import org.apache.axis.wsi.scm.retailer.catalog.CatalogItem;
import org.apache.axis.wsi.scm.retailer.catalog.CatalogType;

/**
 * Implementation of RetailerPortType
 * 
 * @author Ias (iasandcb@tmax.co.kr)
 */
public class RetailerSoapBindingImpl implements org.apache.axis.wsi.scm.retailer.RetailerPortType, ServiceLifecycle {

    CatalogType catalog = new CatalogType();

    public org.apache.axis.wsi.scm.retailer.catalog.CatalogType getCatalog() throws java.rmi.RemoteException {
        return catalog;
    }

    public org.apache.axis.wsi.scm.retailer.order.PartsOrderResponseType submitOrder(
        org.apache.axis.wsi.scm.retailer.order.PartsOrderType partsOrder,
        org.apache.axis.wsi.scm.retailer.order.CustomerDetailsType customerDetails,
        org.apache.axis.wsi.scm.configuration.ConfigurationType configurationHeader)
        throws
            java.rmi.RemoteException,
            org.apache.axis.wsi.scm.retailer.order.InvalidProductCodeType,
            org.apache.axis.wsi.scm.retailer.BadOrderFault,
            org.apache.axis.wsi.scm.configuration.ConfigurationFaultType {
        return null;
    }

    /**
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object context) throws ServiceException {
        CatalogItem[] items = new CatalogItem[10];
        items[0] = new CatalogItem();
        items[0].setName("TV, Brand1");
        items[0].setDescription("24in, Color, Advanced Velocity Scan Modulation, Stereo");
        items[0].setProductNumber(new java.math.BigInteger("605001"));
        items[0].setCategory("TV");
        items[0].setBrand("Brand1");
        items[0].setPrice((new BigDecimal(299.95)).setScale(2, BigDecimal.ROUND_HALF_UP));

        items[1] = new CatalogItem();
        items[1].setName("TV, Brand2");
        items[1].setDescription("32in, Super Slim Flat Panel Plasma");
        items[1].setProductNumber(new java.math.BigInteger("605002"));
        items[1].setCategory("TV");
        items[1].setBrand("Brand2");
        items[1].setPrice((new BigDecimal(1499.99)).setScale(2, BigDecimal.ROUND_HALF_UP));

        items[2] = new CatalogItem();
        items[2].setName("TV, Brand3");
        items[2].setDescription("50in, Plasma Display");
        items[2].setProductNumber(new java.math.BigInteger("605003"));
        items[2].setCategory("TV");
        items[2].setBrand("Brand3");
        items[2].setPrice(new BigDecimal("5725.98"));

        items[3] = new CatalogItem();
        items[3].setName("Video, Brand1");
        items[3].setDescription("S-VHS");
        items[3].setProductNumber(new java.math.BigInteger("605004"));
        items[3].setCategory("Video");
        items[3].setBrand("Brand1");
        items[3].setPrice(new BigDecimal("199.95"));

        items[4] = new CatalogItem();
        items[4].setName("Video, Brand2");
        items[4].setDescription("HiFi, S-VHS");
        items[4].setProductNumber(new java.math.BigInteger("605005"));
        items[4].setCategory("Video");
        items[4].setBrand("Brand2");
        items[4].setPrice(new BigDecimal("400.00"));

        items[5] = new CatalogItem();
        items[5].setName("Video, Brand3");
        items[5].setDescription("s-vhs, mindv");
        items[5].setProductNumber(new java.math.BigInteger("605006"));
        items[5].setCategory("Video");
        items[5].setBrand("Brand3");
        items[5].setPrice(new BigDecimal("949.99"));

        items[6] = new CatalogItem();
        items[6].setName("DVD, Brand1");
        items[6].setDescription("DVD-Player W/Built-In Dolby Digital Decoder");
        items[6].setProductNumber(new java.math.BigInteger("605007"));
        items[6].setCategory("DVD");
        items[6].setBrand("Brand1");
        items[6].setPrice(new BigDecimal("100.00"));

        items[7] = new CatalogItem();
        items[7].setName("DVD, Brand2");
        items[7].setDescription(
            "Plays DVD-Video discs, CDs, stereo and multi-channel SACDs, and audio CD-Rs & CD-RWs, 27MHz/10-bit video DAC, ");
        items[7].setProductNumber(new java.math.BigInteger("605008"));
        items[7].setCategory("DVD");
        items[7].setBrand("Brand2");
        items[7].setPrice(new BigDecimal("200.00"));

        items[8] = new CatalogItem();
        items[8].setName("DVD, Brand3");
        items[8].setDescription(
            "DVD Player with SmoothSlow forward/reverse; Digital Video Enhancer; DVD/CD Text; Custom Parental Control (20-disc); Digital Cinema Sound modes");
        items[8].setProductNumber(new java.math.BigInteger("605009"));
        items[8].setCategory("DVD");
        items[8].setBrand("Brand3");
        items[8].setPrice(new BigDecimal("250.00"));

        // This one is an invalid product
        items[9] = new CatalogItem();
        items[9].setName("TV, Brand4");
        items[9].setDescription(
            "Designated invalid product code that is allowed to appear in the catalog, but is unable to be ordered");
        items[9].setProductNumber(new java.math.BigInteger("605010"));
        items[9].setCategory("TV");
        items[9].setBrand("Brand4");
        items[9].setPrice(new BigDecimal("149.99"));
        catalog.setItem(items);
    }

    /**
     * @see javax.xml.rpc.server.ServiceLifecycle#destroy()
     */
    public void destroy() {
    }

}
