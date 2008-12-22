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

package samples.userguide.example5;

/** This is a JavaBean which represents an order for some products.
 * 
 * @author Glen Daniels (gdaniels@apache.org)
 */
public class Order
{
    /** Who's ordering  */
    private String customerName;
    /** Where do they live  */
    private String shippingAddress;
    /** Which items do we want */
    private String itemCodes[];
    /** And how many */
    private int quantities[];
    
    // Bean accessors
    
    public String getCustomerName()
    { return customerName; }
    public void setCustomerName(String name)
    { customerName = name; }
    
    public String getShippingAddress()
    { return shippingAddress; }
    public void setShippingAddress(String address)
    { shippingAddress = address; }
    
    public String [] getItemCodes()
    { return itemCodes; }
    public void setItemCodes(String [] items)
    { itemCodes = items; }
    
    public int [] getQuantities()
    { return quantities; }
    public void setQuantities(int [] quants)
    { quantities = quants; }
}
