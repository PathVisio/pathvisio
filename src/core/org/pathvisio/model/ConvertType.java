// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.model;

import java.awt.Color;
import java.math.BigDecimal;

public class ConvertType
{
    public static int parseIntSafe (String s, int def)
    {
    	int result = def;
    	try
    	{
    		result = Integer.parseInt(s);
    	}
    	catch (Exception e) {}
    	return result;
    }

    public static double parseDoubleSafe (String s, double def)
    {
    	double result = def;
    	try
    	{
    		result = Double.parseDouble(s);
    	}
    	catch (Exception e) {}
    	return result;
    }

    public static String makeInteger (String s)
    {
    	double d = Double.parseDouble(s);
        BigDecimal b = BigDecimal.valueOf((long)d);
        return b.toString();
    }

    public static String toGmmlColor(String s)
    {
        int i = Integer.parseInt(s);
        if (i == -1)
        {
        	return "Transparent";
        }
        
        String hexstring = Integer.toHexString(i);

        // pad with zeroes up to a lenght of 6.
        while (hexstring.length() < 6)
        {
        	hexstring = "0" + hexstring;
        }
        
        return hexstring;
    }
    
    public static Color fromMappColor(String s)
    {
    	
    	int i = Integer.parseInt(s);
    	
    	Color result = new Color(
    			i & 0xFF,
    			(i & 0xFF00) >> 8,
    			(i & 0xFF0000) >> 16
    	);
    	
    	return result;
    }
    
    public static String toMappColor(String s)
    {
    
    	if (s.equals("Transparent"))
    	{
    		return "-1";
    	}
    	else
    	{
			int i = Integer.parseInt(s, 16);
			return Integer.toString(i);
    	}
    }
    
    public static String toMappColor(Color rgb, boolean fTransparent)
    {
    	if (fTransparent)
    		return "-1";
    	else
    	{
	    	int c = (rgb.getRed()) + (rgb.getGreen() << 8) + (rgb.getBlue() << 16);
	    	return "" + c;
    	}
    }
}
