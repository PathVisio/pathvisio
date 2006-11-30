package data;

import java.util.Arrays;
import java.util.List;

public class OrientationType {

	// warning: don't change these constants. Correct mapping to .MAPP format depends on it.
	public static final int TOP		= 0;
	public static final int RIGHT	= 1;
	public static final int BOTTOM	= 2;
	public static final int LEFT	= 3;

	// Some mappings to Gpml
	private static final List orientationMappings = Arrays.asList(new String[] {
			"top", "right", "bottom", "left"
	});

	public static int getMapping(String value)
	{
		return orientationMappings.indexOf(value);
	}
	
	public static String getMapping(int value)
	{
		return (String)orientationMappings.get(value);
	}

}
