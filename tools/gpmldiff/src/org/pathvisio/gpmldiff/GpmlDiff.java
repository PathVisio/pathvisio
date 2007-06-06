package org.pathvisio.gpmldiff;

import java.io.File;

/**
   Class containing main method
*/
class GpmlDiff
{
	static File fileOld = null;
	static File fileNew = null;
	
	/**
	   Parse Command-line Options
	*/
	static boolean parseCliOptions(String argv[])
	{
		String error = null;
		if (argv.length != 2) error = "Two parameters expected";
		if (error == null)
		{
			fileOld = new File(argv[0]);
			if (!fileOld.exists()) error = argv[0] + ": File not found";
		}
		if (error == null)
		{
			fileNew = new File(argv[1]);
			if (!fileNew.exists()) error = argv[1] + ": File not found";
		}
		if (error != null)
		{
			System.out.println(error);
			printUsage();
		}
		return error == null;
	}
	
	/**
	   Prints helpful info on the command line
	*/
	static void printUsage()
	{
		System.out.print (
			"Gpmldiff\n" +
			"\n" +
			"Usage:\n" +
			"  Gpmldiff old.gpml new.gpml\n" +
			"\n" +
			"Finds the difference between the two files\n"
			);
	}
	
	/**
	   Entry point.
	*/
    public static void main(String argv[])
	{
		if (parseCliOptions(argv))
		{
			PwyDoc doc1 = PwyDoc.read (fileOld);
			PwyDoc doc2 = PwyDoc.read (fileNew);
			SearchNode result = doc1.findCorrespondence (doc2, new BasicSim(), new BasicCost());
			DiffOutputter out = new BasicOutputter();
			doc1.writeResult (result, doc2, out);
		}
	}
}