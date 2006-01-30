/*
Copyright 2005 H.C. Achterberg, R.M.H. Besseling, I.Kaashoek, 
M.M.Palm, E.D Pelgrim, BiGCaT (http://www.BiGCaT.unimaas.nl/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and 
limitations under the License.
*/

import java.awt.Color;

/**
 * This class takes care of colors given as a string-gmml format. This can contain hex, integer or string color data. This class is a helper to sort this out and get the propper color data out of the string.
 * TODO add something to find and use int colors! This is not supported atm! 
 *
 */

public class GmmlColor
{
	float[] color = new float[3];
	
	//Constructor
	/**
	 * Create an empty GmmlColor
	 */
	public GmmlColor() {
		//Constructor can be empty       
    }

   /**
    * Create a new GmmlColor and store the color in it, note that if the string is an invalid gmml color string it will result in black.
    */
	public GmmlColor(String colorstring)
	{
		storeColor(colorstring);        
    }
   
	/**
	 * This method returns a Color that awt can use
	 */
	public Color getColor()
	{
		Color returncolor = new Color(color[0],color[1],color[2]);
		return returncolor;
	}
	
	/**
	 * This method returns a color in the format of a float[3] with r,g,b stored in it.
	 */
	public float[] getColorFloat()
	{
		return color;
	}
	
	/**
	 * Store a gmml valid color string
	 */
	public void storeColor(String colorstring)
	{
		if(!storeStringColor(colorstring))
		{
			if(!storeHexColor(colorstring))
			{
				if(Integer.parseInt(colorstring.trim()) != -1)
				{
					System.out.println("'"+colorstring + "' is not a valid color value!");
				} else {
					color[0] = 0;
					color[1] = 0;
					color[2] = 0;
				}
			}
		}
	}

	/**
	 * Convert a gmml valid color string into a float[3]
	 */
	public static float[] convertStringToFloat(String colorstring)
	{
		GmmlColor temp = new GmmlColor(colorstring);
		float[] colorfloat = temp.getColorFloat();
		return colorfloat;
	}
	
	/**
	 * Convert a gmml valid color string into a Color awt can use
	 */
	public static Color convertStringToColor(String colorstring)
	{
		GmmlColor temp = new GmmlColor(colorstring);
		Color color = temp.getColor();
		return color;
	}

	/**
	 * Convert an awt Color object into a valid hex string for storing in files
	 */	
	public static String convertColorToString(Color color)
	{
		String rhex = Integer.toHexString(color.getRed());
		String ghex = Integer.toHexString(color.getGreen());
		String bhex = Integer.toHexString(color.getBlue());
		if (color.getRed() < 16) {rhex = "0"+rhex;}
		if (color.getGreen() < 16) {ghex = "0"+ghex;}
		if (color.getBlue() < 16) {bhex = "0"+bhex;}
		String hexcolor = (rhex+ghex+bhex).toUpperCase();
		return hexcolor;
	}

		
	//StoreColor method
	private boolean storeHexColor(String scolor)
	{
		//Trim the string and break it into bytes
		String trimcolor = scolor.trim();
		
		if(trimcolor.length()==6)
		{
			//Break appart the string
			String red = ""+trimcolor.charAt(0)+trimcolor.charAt(1);
			String green = ""+trimcolor.charAt(2)+trimcolor.charAt(3);
			String blue = ""+trimcolor.charAt(4)+trimcolor.charAt(5);
			
			try
			{
				//Convert a hex string into an integer
				int r = Integer.parseInt( red.trim(), 16 /* radix */ );
				int g = Integer.parseInt( green.trim(), 16 /* radix */ );
				int b = Integer.parseInt( blue.trim(), 16 /* radix */ );
			
				//Make the desired 0.0 - 1.0 doubles out of the integers			
				color[0] = r / 255f;
				color[1] = g / 255f;
				color[2] = b / 255f;
				
				return true;	//No errors
			}
			catch (NumberFormatException e) {}
			//Here the string is length 6 but the content is not propper Hex
		}
		
		return false;	//Not a propper Hex Value!
	}

	private boolean storeStringColor(String scolor)
	{
		//Color string table
		String[][] colortable = {
			{"Aqua","0","1","1"},
			{"Black","0","0","0"},
			{"Blue","0","0","1"},
			{"Fuchsia","1","0","1"},
			{"Gray","0.5","0.5","0.5"},
			{"Green","0","0.5","0"},
			{"Lime","0","1","0"},
			{"Maroon","0.5","0","0"},
			{"Navy","0","0","0.5"},
			{"Olive","0.5","0.5","0"},
			{"Purple","0.5","0","0.5"},
			{"Red","1","0","0"},
			{"Silver","0.75","0.75","0.75"},
			{"Teal","0","0.5","0.5"},
			{"White","1","1","1"},
			{"Yellow","1","1","0"}
		};

		String trimcolor = scolor.trim();
		
		//Test each known color name
		for(int i=0; i < 16; i++) { //The length of the table is 16 hardcoded
			if (trimcolor.equalsIgnoreCase(colortable[i][0])) {
				//Insert the color as doubles
				color[0] = Float.parseFloat(colortable[i][1]);
				color[1] = Float.parseFloat(colortable[i][2]);
				color[2] = Float.parseFloat(colortable[i][3]);
				
				return true;	//No errors
			}
		}
		return false;	//Not a recognised color name
	}

}