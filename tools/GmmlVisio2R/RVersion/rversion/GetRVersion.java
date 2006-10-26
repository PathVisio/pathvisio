package rversion;

public class GetRVersion {

	static 
	{
		System.loadLibrary("RVersion");
	}
	
	public static native String rniGetVersionR();
	
}
