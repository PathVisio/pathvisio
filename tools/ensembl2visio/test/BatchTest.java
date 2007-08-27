package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BatchTest {
	static final String path = "E:\\Documents and Settings\\Thomas\\My Documents\\PathVisio data\\gene databases\\";

	static final String[] databases = new String[] {
		path + "Hs_Derby_20070817b_mixed.pgdb",
		path + "Hs_Derby_20070817b_control.pgdb",
		path + "Hs_Derby_20070817b_separate.pgdb",
		path + "Hs_Derby_20070817b_combined.pgdb",
	};
	
	public static void main(String[] args) {
		String cp = ".;../../lib/derby.jar;../../pathvisio_v1.jar";
		int nrRuns = 3;
		
		try {
			runProcess("javac -cp " + cp + " test/*.java");
			
			for(String db : databases) {
				System.out.println(db);
				List<List<String>> result = new ArrayList<List<String>>();
				for(int i = 0; i < nrRuns; i++) {
					List<String> r = runProcess("java -cp " + cp + " test.TestIndex \"" + db + "\"");
					result.add(r);
				}
				for(int q = 0; q < TestIndex.queries.length; q++) {
					System.out.print(TestIndex.queries[q] + "\t");
					for(int i = 0; i < nrRuns; i++) {
						System.out.print(result.get(i).get(q) + "\t");
					}
					System.out.print("\n");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	static List<String> runProcess(String cmd) throws IOException {
		List<String> out = new ArrayList<String>();
		String line;
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader input = new BufferedReader(
				new InputStreamReader(p.getErrorStream()));
		
		while ((line = input.readLine()) != null) {
			out.add(line);
		}
		input.close();
		return out;
	}
}
