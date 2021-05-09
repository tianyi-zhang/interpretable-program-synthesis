package edu.harvard.seas.synthesis.sample;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public abstract class RegexSampler {
	HashMap<String, ArrayList<String>> match_map;
	HashSet<String> example_set;
	HashMap<String, Integer> example_stat;
	
	List<File> log_files;
	
//	String python_cmd = "python3"; // this works on Priyan's machine but not mine
	public static String python_cmd = "/usr/local/bin/python";
	
	public RegexSampler(List<String> logFiles) {
		match_map = new HashMap<String, ArrayList<String>>();
		example_set = new HashSet<String>();
		log_files = new ArrayList<File>();
		for(String logFile : logFiles) {
			log_files.add(new File(logFile));
		}
		example_stat = new HashMap<>();
	}
	
	
	public void processLogFile() {
		LineIterator it = null;
		for(File f : log_files) {
			try {
				it = FileUtils.lineIterator(f, "UTF-8");
				String cur_regex = "";
				while(it.hasNext()) {
					String line = it.nextLine();
					if(line.startsWith("program: ")) {
						cur_regex = line.substring(line.indexOf(':')+1).trim();
					} else if (!line.startsWith("matching indices:") 
							&& !line.startsWith("matching examples:") 
							&& line.contains("---------")) {
						// this is a matching example record
						ArrayList<String> l;
						if(match_map.containsKey(cur_regex)) {
							l = match_map.get(cur_regex);
						} else {
							l = new ArrayList<String>();
						}
						
						if(!l.contains(line)) {
							l.add(line);
							if(example_stat.containsKey(line)){
								example_stat.put(line, example_stat.get(line) + 1);
							}
							else {
								example_stat.put(line, 1);
							}
						}
						
						match_map.put(cur_regex, l);
						
						// add this example to the example set
						example_set.add(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(it != null) {
					LineIterator.closeQuietly(it);
				}
			}
		}
	}
	
	public abstract Set<String> sample();
	
	public Set<String> solve(String matrix_file, String cost_file) throws IOException, InterruptedException {
		HashSet<String> sample = new HashSet<String>(); // a sample of columns in the matrix
		
		File f1 = new File(matrix_file);
		File f2 = new File(cost_file);
		
		// run the python set cover solver
		File fError = new File("set-cover-error");
        File fOutput = new File("set-cover-output");
        
		String[] cmd = {python_cmd, "setcover/setcover.py", f1.getAbsolutePath(), f2.getAbsolutePath()};
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectError(fError);
        processBuilder.redirectOutput(fOutput);
        Process process = processBuilder.start();
        process.waitFor();
        
        String error_output = FileUtils.readFileToString(fError, Charset.defaultCharset());
        if(!error_output.isEmpty()) {
        	System.err.println("Set cover solver throws an error.");
        	System.err.println(error_output);
        }
        List<String> lines = FileUtils.readLines(fOutput, Charset.defaultCharset());
        if(!lines.isEmpty()) {
        	for(String line : lines) {
        		if(line.startsWith("Selected Program:")) {
        			String matrix_col = line.substring(line.indexOf(':')+1).trim();
        			// add the column index to the sample set
        			sample.add(matrix_col);
        		}
        	}
        }
        
        fError.delete();
        fOutput.delete();
        
        return sample;
	}

	public ArrayList<String> GetExamplesForRegex(String regex) {
		return match_map.getOrDefault(regex, new ArrayList<>());
	}

	public ArrayList<String> GetAllExamples() {
		ArrayList<String> examples = new ArrayList<>();
		examples.addAll(this.example_set);
		return  examples;
	}

	public int GetTotalProgramCount() {
		return match_map.keySet().size();
	}

	public int GetTotalSatisfiedPrograms(String exampleStr) {
		return example_stat.getOrDefault(exampleStr, 0);
	}
}
