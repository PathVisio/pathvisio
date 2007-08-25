package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BatchTest {
	static final String path = "E:\\Documents and Settings\\Thomas\\My Documents\\PathVisio data\\gene databases\\";

	static final String[] databases = new String[] {
		path + "Hs_Derby_20070817b_control.pgdb",
		path + "Hs_Derby_20070817b_control.pgdb",
		path + "Hs_Derby_20070817b_combined.pgdb",
		path + "Hs_Derby_20070817b_combined.pgdb",
		path + "Hs_Derby_20070817b_mixed.pgdb",
		path + "Hs_Derby_20070817b_mixed.pgdb",
		path + "Hs_Derby_20070817b_separate.pgdb",
		path + "Hs_Derby_20070817b_separate.pgdb"
	};
	
	public static void main(String[] args) {
		System.out.println("Running tests in " +  System.getProperty("user.dir"));
		String cp = ".;../../lib/derby.jar;../../pathvisio_v1.jar";
		try {
			runProcess("javac -cp " + cp + " test/*.java");
			
			for(String db : databases) {
				runProcess("java -cp " + cp + " test.TestIndex \"" + db + "\"");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	static void runProcess(String cmd) throws IOException {
		String line;
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader input = new BufferedReader(
				new InputStreamReader(p.getErrorStream()));
		
		while ((line = input.readLine()) != null) {
			System.out.println(line);
		}
		input.close();
	}
}
