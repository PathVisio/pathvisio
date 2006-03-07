import java.io.File;

public class TestData {
	File gdbFile;
	File gexFile;
	File mappFile;
	
	public TestData() {
		
	}
	
    public long doTest() {
    	// Connect to the database
    	DbTest db = new DbTest();
    	// Check the starttime
    	long startTime = System.currentTimeMillis();
    	// Load the database
    	db.go();
    	// Check the endtime
    	long endTime = System.currentTimeMillis();
    	return endTime - startTime;
    }
}
