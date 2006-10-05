package data;

import java.math.BigDecimal;

import org.eclipse.swt.graphics.RGB;

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
    
    public static RGB fromMappColor(String s)
    {
    	
    	int i = Integer.parseInt(s);
    	
    	RGB result = new RGB(
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
    
    public static String toMappColor(RGB rgb, boolean fTransparent)
    {
    	if (fTransparent)
    		return "-1";
    	else
    	{
	    	int c = ((int)rgb.red) + ((int)rgb.green << 8) + ((int)rgb.blue << 16);
	    	return "" + c;
    	}
    }
}
