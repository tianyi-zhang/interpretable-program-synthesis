package edu.harvard.seas.synthesis;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import edu.harvard.seas.synthesis.sample.*;
import org.apache.commons.io.FileUtils;

public class ResnaxRunner {
	public static String resnax_path = "lib";
	public static int timeout = 1; // 1 second
	
	private String java_class_path;
	private String z3_lib_path;

	private String example_file_path = "input";
	private String program_file_path = "program";
	private String pause_file_path = "pause";
	private String stop_file_path = "stop";
	private String log_dir_path = "resnax_log" + File.separator;
	private String temp_dir_path = "resnax_temp" + File.separator;
	private String log_dir_path2 = "resnax_enumerate" + File.separator;

	private static ResnaxRunner single_instance = null;
	
	public HashMap<String, String> dsl_to_automaton_regex = new HashMap<String, String>();
	public Process process = null;
	public int counter = 0;

	private HashMap<String, Summary> SemanticCoverageSummary = new HashMap<>(), SyntaxCoverageSummary = new HashMap<>();
	
	private ResnaxRunner() {
		if(!log_dir_path.endsWith(File.separator)) {
			log_dir_path += File.separator;
		}
		
		if(!temp_dir_path.endsWith(File.separator)) {
			temp_dir_path += File.separator;
		}
		
		if(!log_dir_path2.endsWith(File.separator)) {
			log_dir_path2 += File.separator;
		}
		
		File log_dir = new File(log_dir_path);
		if(log_dir.exists()) {
			log_dir.delete();
		}
		log_dir.mkdir();
		
		File temp_dir = new File(temp_dir_path);
		if(temp_dir.exists()) {
			temp_dir.delete();
		}
		temp_dir.mkdir();
		
		File log_dir2 = new File(log_dir_path2);
		if(log_dir2.exists()) {
			log_dir2.delete();
		}
		log_dir2.mkdir();
		
		// remove temporary files in case the previous synthesis iteration does not terminate normally
		File f1 = new File(example_file_path);
		if(f1.exists()) {
			f1.delete();
		}
		File f2 = new File(program_file_path);
		if(f2.exists()) {
			f2.delete();
		}
		File f3 = new File(pause_file_path);
		if(f3.exists()) {
			f3.delete();
		}
		File f4 = new File(stop_file_path);
		if(f4.exists()) {
			f4.delete();
		}
		
		// By default
		z3_lib_path = resnax_path;

		String os = System.getProperty("os.name").toLowerCase();
		String jvmBitVersion = System.getProperty("sun.arch.data.model");
		if(os.indexOf("win") >= 0) {
			if(jvmBitVersion.equals("32")) {
				z3_lib_path = resnax_path + File.separator + "win32";
			}
			else if(jvmBitVersion.equals("64")) {
				z3_lib_path = resnax_path + File.separator + "win64";
			}
		}

		// enumerate all jar files in the classpath
		java_class_path = resnax_path + File.separator + "resnax.jar"
		 + File.pathSeparator + resnax_path + File.separator + "antlr-4.7.1-complete.jar"
		 + File.pathSeparator + resnax_path + File.separator + "automaton.jar"
		 + File.pathSeparator + resnax_path + File.separator + "com.microsoft.z3.jar"
		 + File.pathSeparator + resnax_path + File.separator + "javatuples-1.2.jar"
		 + File.pathSeparator + resnax_path + File.separator + "libz3java.dylib"
		 + File.pathSeparator + resnax_path + File.separator + "libz3java.so";
	}
	
	public static ResnaxRunner getInstance() {
		if(single_instance == null) {
			single_instance = new ResnaxRunner();
		} 
		
		return single_instance;
	}
	
	public static void reset() {
		if(single_instance == null) {
			return;
		}
		
		// kill the current synthesis process
		if(single_instance.process != null && single_instance.process.isAlive()) {
			single_instance.process.destroy();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(single_instance.process.isAlive()) {
				single_instance.process.destroyForcibly();
			}
		}
		
		// remove the temporary files
		File f1 = new File(single_instance.example_file_path);
		f1.delete();
		File f2 = new File(single_instance.program_file_path);
		f2.delete();
		File f3 = new File(single_instance.pause_file_path);
		f3.delete();
		File f4 = new File(single_instance.stop_file_path);
		f4.delete();
		
		// remove log files if any
		File fError = new File("resnax-error");
		if(fError.exists()) {
			fError.delete();
		}
        File fOutput = new File("resnax-output");
		if(fOutput.exists()) {
			fOutput.delete();
		}
		File fLog1 = new File(single_instance.log_dir_path);
		fLog1.delete();
		File fLog2 = new File(single_instance.log_dir_path2);
		fLog2.delete();
		File fTemp = new File(single_instance.temp_dir_path);
		fTemp.delete();
		
		single_instance = null;
	}
	
	private String prev_sketch = "";
	private String prev_excludes = "";
	private String prev_branches_to_priotize = "";
	private String prev_branches_to_avoid = "";
	private HashSet<String> prev_examples = new HashSet<String>();
	public List<String> run(Example[] examples, Regex[] regexes, String branches_to_priotize, String branches_to_avoid) {
		// reset the previous mapping between DSL regexes and automaton regexes
		dsl_to_automaton_regex.clear();
		
		// remove any left-over pause or stop files if any
		File f1 = new File(pause_file_path);
		if(f1.exists()) {
			f1.delete();
		}
		File f2 = new File(stop_file_path);
		if(f2.exists()) {
			f2.delete();
		}
		
		// write the input examples to the example file
		File f = new File(example_file_path);
		String s = "";
		HashSet<String> example_set = new HashSet<String>();
		for(Example example : examples) {
			s += "\"" + example.input + "\"," + (example.output ? "+" : "-") + System.lineSeparator();
			example_set.add(example.input + "," + example.output);
		}
		s+= System.lineSeparator();
		
		// use a random ground truth, it does not matter, just a requirement by the synthesizer
		s += "repeatatleast(or(<A>,or(<B>,<C>)),1)";
		s += System.lineSeparator();
		
		// parse the annotations to sketches
		String sketch = parseAnnotationToSketch(examples, regexes);
		
		HashSet<String> exclude_set = new HashSet<String>();
		for(Regex regex : regexes) {
			if(regex.exclude.length == 0) continue;
			
			for(String exclude : regex.exclude) {
				exclude_set.add(exclude);
			}
		}
		

		String excludes = "";
		for(String e : exclude_set) {
			excludes += e + "&&";
		}
		
		if(!excludes.isEmpty()) {
			excludes = excludes.substring(0, excludes.length() - 2);
		} else {
			excludes = ",";
		}
		
		String must_includes = "";
		for(Regex regex : regexes) {
			if(regex.include.length == 0) continue;
			
			for(String must : regex.include) {
				if(!must_includes.contains(must)) {
					must_includes += must + "&&";
				}
			}
		}
		
		if(!must_includes.isEmpty()) {
			must_includes = must_includes.substring(0, must_includes.length() - 2);
		} else {
			must_includes = ",";
		}
		
		boolean restart;
		HashSet<String> copy = new HashSet<String>(prev_examples);
		copy.removeAll(example_set);
		if(process == null) {
			// this is the first iteration
			counter = 0;
			restart = true;
		} else if(!sketch.equals(prev_sketch) || !excludes.equals(prev_excludes) 
				|| !branches_to_priotize.equals(prev_branches_to_priotize)
				|| !branches_to_avoid.equals(prev_branches_to_avoid)
				|| !copy.isEmpty()) {
			// user intent may have changed, redo the synthesis from scratch
			counter = 0;
			restart = true;
		} else if(process != null && !process.isAlive()) {
			// the synthesis process has crashed due to error such as out of memory
			// have to restart
			counter = 0;
			restart = true;
		} else {
			restart = false;
		}
		
		if(restart) {
			// remove previous log files
			File log_dir = new File(log_dir_path2);
			if(log_dir.exists()) {
				for(File log_file : log_dir.listFiles()) {
					log_file.delete();
				}
			}
			
			// reset the other global variables
			prev_sketch = "";
			prev_excludes = "";
			prev_branches_to_priotize = "";
			prev_branches_to_avoid = "";
			regex_map.clear();
			parent_map.clear();
		}
		
		
		// set the signal 
		s += "READY-" + counter;
		
		try {
			// write the examples to the example file
			FileUtils.write(f, s, Charset.defaultCharset(), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			invokeResnax(sketch, excludes, must_includes, branches_to_priotize, branches_to_avoid, restart, examples.length);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		// get the new synthesized programs
		ArrayList<String> new_regexes = new ArrayList<String>();
		try {
			File log_file = new File(program_file_path);
			if(log_file.exists()) {
				List<String> lines = FileUtils.readLines(log_file, Charset.defaultCharset());
				for(int i = 0; i < lines.size()-1; i+=2) {
					String curr_dsl_regex = lines.get(i).trim();
					String curr_automaton_regex = lines.get(i+1).trim();
					if(!new_regexes.contains(curr_dsl_regex)) {
						// avoid duplication
						new_regexes.add(curr_dsl_regex);
						dsl_to_automaton_regex.put(curr_dsl_regex, curr_automaton_regex);
					}
				}
				
				if(new_regexes.isEmpty()) {
					System.err.println("Synthesis timeout. No program is generated.");
				}
				
				log_file.delete();
			} else {
				System.err.println("No resnax log file exists. The synthesizer crashed.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// check if there is a stop signal
    	File stopFile = new File(stop_file_path);
    	if(stopFile.exists()) {
    		String data;
			try {
				data = FileUtils.readFileToString(stopFile, Charset.defaultCharset());
				if (data.contains("stop-" + counter)) {
	            	// kill the process
	    			if(process != null && process.isAlive()) {
	    				process.destroy();
	    			}
	            }
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// delete it after reading
			stopFile.delete();
    	}
    	
    	// delete the pause file if it exists
    	File pauseFile = new File(pause_file_path);
    	if(pauseFile.exists()) {
    		pauseFile.delete(); 		
    	}
    	
    	prev_sketch = sketch;
    	prev_excludes = excludes;
    	prev_branches_to_priotize = branches_to_priotize;
    	prev_branches_to_avoid = branches_to_avoid;
    	prev_examples = example_set;
    	counter++;
		
		return new_regexes;
	}
	
	public void invokeResnax(String sketch, String excludes, String must_includes, 
			String branches_to_priotize, String branches_to_avoid, 
			boolean restart, int example_num) throws IOException, InterruptedException {
        File fError = new File("resnax-error");
        if(fError.exists()) {
        	fError.delete();
        }
        File fOutput = new File("resnax-output");
        if(fOutput.exists()) {
        	fOutput.delete();
        }
        
		if(restart) {
			// check if the process is still alive, if it is , then kill it.
			if(process != null && process.isAlive()) {
				process.destroy();
				Thread.sleep(2000);
				if(process.isAlive()) {
					process.destroyForcibly();
				}
			}
			
			int maxMem;
			try {
				// This is specific to Oracle JVM
				long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory
				        .getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
				maxMem = (int) (memorySize / (1024 * 1024 * 1024));
			} catch(Exception e) {
				// catch any exceptions that arise in other JVMs
				// if any exception occurs, make a conversative choice of only allocating a max of 8G memory
				maxMem = 8;
			}
			
			String jvmBitVersion = System.getProperty("sun.arch.data.model");
			if(jvmBitVersion.equals("32")) {
				// If the JVM is 32-bit, we can only allocate a max of 4G memory.
				maxMem = 4;
				
				// If it is a Windows system, be more conservative and only allocate 1G memory
				String os = System.getProperty("os.name").toLowerCase();
				if(os.indexOf("win") >= 0) {
					maxMem = 1;
				}
			}

			// invoke resnax in a separate thread and set a timeout
			String[] cmd = {"java", "-Xmx" + maxMem + "G", "-Djava.library.path=" + z3_lib_path,
					"-cp", java_class_path, "-ea",  "MRGA.Main", //"resnax.Main", 
					"0", // dataset : 0 - so, 1 - deepregex, 2 - kb13
					example_file_path, // file path to the input-output examples
					log_dir_path, // path to the log directory
					sketch,
					"1", 
					"2", // mode : 1 - normal, 2 - prune, 3 - forgot, 4 - pure-enumeration, 5 - example-only
					"0", // extended mode, what is this?
					temp_dir_path,
					"5", 
					excludes,
					example_file_path,
					program_file_path,
					timeout * 1000 + "",
					must_includes,
					branches_to_priotize,
					branches_to_avoid};
	        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
	        processBuilder.redirectError(fError);
	        processBuilder.redirectOutput(fOutput);
	        process = processBuilder.start();
		}
		
		ArrayList<Heartbeat> prev_arr = new ArrayList<Heartbeat>();		
        while(true) {
        	// read the log file
        	File log_file = new File(log_dir_path2 + counter);
        	if(log_file.exists()) {
        		ArrayList<Heartbeat> arr = LogFileUtils.readLogFileToHeartbeat(log_file, dsl_to_automaton_regex);
        		if(prev_arr.size() != arr.size() && arr.size() > prev_arr.size()) {
        			List<Heartbeat> sublist = arr.subList(prev_arr.size(), arr.size());
        			// sample if the array size is too large
        			if(sublist.size() > 100) {
        				int counter = 0;
        				List<Heartbeat> new_subList = new ArrayList<Heartbeat>();
        				for(int i = 0; i < sublist.size(); i++) {
        					Heartbeat ht = sublist.get(i);
        					double ratio = ((double) ht.example_num) / example_num;
        					if(ratio > 0.7) {
        						// a regex that satisfies at least 70% of user-given examples
        						// keep it as it may be useful
        						new_subList.add(ht);
        					} else {
        						// only sample one of three regexes
        						if(counter % 5 == 0) {
        							new_subList.add(ht);
        						}
        						counter++;
        					}
        				}
        				
        				sublist = new_subList;    
        				
        				// comment out the ineffective old sampling method
//        				// sample 10% of programs if more than 10 programs have unchanged number of satisfied examples
//        				// in a row
//        				int unchanged_len = 0;
//        				int prev = arr.get(0).example_num;
//        				for(int i = 0; i < sublist.size(); i++) {
//        					int x = arr.get(i).example_num;
//        					if(x != prev) {
//        						if(unchanged_len > 10) {        							
//        							// only pick 1% of these unchanged data points
//        							for(int j = i-unchanged_len; j < i; j=j+10) {
//        								new_subList.add(sublist.get(j));
//        							}
//        						} else {
//        							// add the previous unchanged data points
//        							new_subList.addAll(sublist.subList(i-unchanged_len, i));
//        						}
//        						
//        						unchanged_len = 1;
//        					} else {
//        						unchanged_len++;
//        					}
//        					
//        					prev = x;
//        				}
//        				
//        				sublist = new_subList;
        			}
        			
        			// only send this array to the front end to update the chart
        			// when new data has been logged
            		SynthesisServerHandler.sendObjectAsJSONMessage(sublist, "heartbeat");
            		prev_arr = arr;
        		}
        	}
        	
        	// wait till the signal is there
        	File f = new File(program_file_path);
        	if(f.exists()) {
        		String data = FileUtils.readFileToString(f, Charset.defaultCharset());
	            if (data.contains("READY-" + counter)) {
	            	break;
	            }
        	}  
        	
        	if (fError.exists()) {
        		String errorMessage = FileUtils.readFileToString(fError, Charset.defaultCharset());
        		if(!errorMessage.trim().isEmpty() && errorMessage.contains("Exception in thread")) {
        			// an error occurs 
        			if(fOutput.exists()) {
        	        	System.out.println(FileUtils.readFileToString(fOutput, Charset.defaultCharset()));
        	            fOutput.delete();            
        	        }
        			System.out.println("Error occurs during program synthesis");
        			System.out.println(errorMessage);
        			fError.delete();
            		return;
        		}
        	}
        	
        	// sleep 1 seconds
          	Thread.sleep(1000);
        }
        
        if(fError.exists()) {
        	System.out.println(FileUtils.readFileToString(fError, Charset.defaultCharset()));            
        	fError.delete();
        }
        if(fOutput.exists()) {
        	System.out.println(FileUtils.readFileToString(fOutput, Charset.defaultCharset()));
            fOutput.delete();            
        }
	}
	
	public boolean isConcreteProgramReached() {
		File log_file = new File(log_dir_path2 + (counter - 1));
		if(log_file.exists()){
			// check if the log file is empty
			String content;
			try {
				content = FileUtils.readFileToString(log_file, Charset.defaultCharset());
				if(content.trim().isEmpty()) {
					// the log file is empty, meaning the synthesizer has been busy with
					// expanding sketches and haven't reached to a concrete program yet
					// in the 20 seconds
					return false;
				} else {
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	public Summary GetSynthesisSummary(SamplerType samplerType, int continuous) {
		// Check if synthesis complete
		List<String> log_files = new ArrayList<String>(); 
		if(continuous > 0) {
			// need to read previous n log files since the user has chosen to continue the synthesis for more than one 
			// iterations
			for(int i = continuous; i >= 0; i--) {
				String path = log_dir_path2 + (counter - 1 - i);
				File f = new File(path);
				if(f.exists()) {
					log_files.add(f.getPath());
				}
			}
		} else {
			String path = log_dir_path2 + (counter - 1);
			File f = new File(path);
			if(f.exists()) {
				log_files.add(f.getPath());
			}
		}
		
		if(!log_files.isEmpty()){
			// Run the sampler and return the results as JSON
			// RegexSampler sampler = new SemanticCoverageSampler("src/test/resources/0");
			RegexSampler sampler;
			switch (samplerType){
				case Semantic:
					sampler = new SemanticCoverageSampler(log_files);
					break;
				case Syntax:
					return null;
//					sampler = new SyntaxCoverageSampler(log_file_path);
//					break;s
				case MaxExamples:
				default:
					sampler = new MaxExamplesSampler(log_files);
					break;
				case Unique:
					sampler = initializeUniqueSampler(log_files, continuous);
					break;
			}
			
			sampler.processLogFile();
			Set<String> sample_set = sampler.sample();

			ArrayList<String> example_list = new ArrayList<>(this.prev_examples);
			HashMap<String, ExampleStat> exampleMap = new HashMap<>();
			ArrayList<ExampleStat> exampleStats = new ArrayList<>();
			for(String ex: example_list){
				String ex_str = convertPrevExampleString(ex);
				ExampleStat exampleStat = parseExampleString(ex_str);
				exampleStat.stat = sampler.GetTotalSatisfiedPrograms(ex_str);
				exampleMap.put(ex_str, exampleStat);
				exampleStats.add(exampleStat);
			}

			ArrayList<ProgramStat> programStats = new ArrayList<>();
			//for all the sample programs, update the example stat and satisfies
			for(String sample: sample_set){
				ProgramStat programStat = new ProgramStat();
				programStat.program = sample;
				programStat.satisfies = new ArrayList<>();
				// Get all the examples - update the stats and program satisfies
				ArrayList<String> satisfiedExamples = sampler.GetExamplesForRegex(sample);
				for(String se: satisfiedExamples){
					ExampleStat es = exampleMap.get(se);
					programStat.satisfies.add(exampleStats.indexOf(es));
				}

				programStats.add(programStat);
			}

			Summary summary = new Summary();
			summary.programs = programStats;
			summary.examples = exampleStats;
			summary.total_programs = sampler.GetTotalProgramCount();
			summary.sampler_type = samplerType.toString();

			if(samplerType == SamplerType.Semantic){
				SemanticCoverageSummary.put(log_files.toString(), summary);
			} else if(samplerType == SamplerType.Syntax){
				SyntaxCoverageSummary.put(log_files.toString(), summary);
			}

			return summary;
		}

		return null;
	}

	private UniqueSampler initializeUniqueSampler (List<String> logfile, int continuous) {
		if(counter == 1 && !regex_map.isEmpty()) {
			regex_map.clear();
			parent_map.clear();
		}

		LinkedHashMap<String, ArrayList<String>> tree = readTreeLog(continuous);

		TreeObject treeRoot = LogFileUtils.convertTreeBasedLogToTreeObject(tree);

		return new UniqueSampler(logfile, treeRoot);
	}

	public ExampleStat parseExampleString(String exString) {
		if(exString.endsWith("---------true")){
			ExampleStat example = new ExampleStat();
			example.input = exString.substring(0,exString.length() - "---------true".length());
			example.positive = true;
			example.stat = 0;
			return example;
		}
		else{
			ExampleStat example = new ExampleStat();
			example.input = exString.substring(0,exString.length() - "---------false".length());
			example.positive = false;
			example.stat = 0;
			return example;
		}
	}

	public String convertPrevExampleString(String exString) {
		if(exString.endsWith(",true")){
			return "" + exString.substring(0,exString.length() - ",true".length()) + "---------true";
		}
		else{
			return "" + exString.substring(0,exString.length() - ",false".length()) + "---------false";
		}
	}
	
	public String parseAnnotationToSketch(Example[] examples, Regex[] regexes) {
		HashSet<String> exact_matches = new HashSet<String>();
		HashSet<String> not_matches = new HashSet<String>();
		HashSet<String> char_families = new HashSet<String>();
		HashSet<String> includes = new HashSet<String>();
		
		for(Example example : examples) {
			if(example.output) {
				// only consider exact match in positive examples
				for(String s : example.exact) {
					exact_matches.add(s);
				}
			}
			
			if(!example.output) {
				// only consider unmatch in negative examples
				for(String s : example.unmatch) {
					not_matches.add(s);
				}
			}
			
			for(String s : example.generalize) {
				String char_family = s.substring(s.lastIndexOf("@@@") + 3);
				if(char_family.equals("any")) {
					// handle it outside
					continue;
				}
				
				char_families.add('<' + char_family + '>');
				// comment out this heuristic, seems not so helpful
//				if(!example.output) {
//					char_families.add("not(contain(<" + char_family + ">))");
//				}
			}
		}
		
		for(Regex regex : regexes) {
			for(String s : regex.include) {
				includes.add(s);
			}
		}
		
		for(Regex regex : regexes) {
			for(String s : regex.maybe) {
				includes.add(s);
			}
		}
		
		String sketch = "?";
		String sketch_includes = "";
		HashSet<String> single_chars = new HashSet<String>();
		HashSet<String> sequences = new HashSet<String>();
		
		for(String match : exact_matches) {
			if(match.length() == 1) {
				// a single character
				single_chars.add("<" + match + ">");
			} else {
				char[] chars = match.toCharArray();
				// Option 1: treat multiple characters as a sequence
				String s = "";
				for(int i = 0; i < chars.length - 1; i++) {
					s += "concat(<" + chars[i] + ">,";
				}
				s+= "<" + chars[chars.length - 1] + ">";
				for(int i = 0; i < chars.length - 1; i++) {
					s+= ")";
				}
				sequences.add(s);
				
				// Option 2: treat multiple characters separately, not as a sequence
				for(char c : chars) {
					single_chars.add("<" + c + ">");
				}
			}
		}
		
		for(String unmatch : not_matches) {
			if(unmatch.length() == 1) {
				// a single character
				single_chars.add("<" + unmatch + ">");
			} else {
				char[] chars = unmatch.toCharArray();
				// Option 1: treat multiple characters as a sequence
				String s = "";
				for(int i = 0; i < chars.length - 1; i++) {
					s += "concat(<" + chars[i] + ">,";
				}
				s+= "<" + chars[chars.length - 1] + ">";
				for(int i = 0; i < chars.length - 1; i++) {
					s+= ")";
				}
				sequences.add(s);
				
				// Option 2: treat multiple characters separately, not as a sequence
				for(char c : chars) {
					single_chars.add("<" + c + ">");
				}
			}
		}
		
		for(String char_family : char_families) {
			sequences.add(char_family);
		}
		
		for(String include : includes) {
			if(include.equals("repeatatleast")) {
				include = "repeatatleast(?,-1)";
			} else if (include.equals("contain")) {
				include = "contain(?)";
			} else if (include.equals("or")) {
				include = "or(?,?)";
			} else if (include.equals("startwith")) {
				include = "startwith(?)";
			} else if (include.equals("endwith")) {
				include = "endwith(?)";
			} else if (include.equals("optional")) {
				include = "optional(?)";
			} else if (include.equals("star")) {
				include = "star(?)";
			} else if (include.equals("kleenestar")) {
				include = "kleenestar(?)";
			} else if (include.equals("repeat")) {
				include = "repeat(?,-1)";
			} else if (include.equals("repeatrange")) {
				include = "repeatrange(?,-1,-1)";
			} else if (include.equals("concat")) {
				include = "concat(?,?)";
			} else if (include.equals("not")) {
				include = "not(?)";
			} else if (include.equals("notcc")) {
				include = "notcc(?)";
			} else if (include.equals("and")) {
				include = "and(?,?)";
			} else if (include.equals("or")) {
				include = "or(?,?)";
			} else if (include.equals("sep")) {
				include = "sep(?,?)";
			}
			
			sketch_includes += include + ",";
		}
		
		ArrayList<String> l = new ArrayList<String>(single_chars);
		if(l.size() > 1) {
			sketch += "{";
			// add a disjunction of all single chars
			for(int i = 0; i < l.size() - 1; i++) {
				sketch += "or(" + l.get(i)+ ",";
			}
			sketch += l.get(l.size() - 1) ;
			for(int i = 0; i < l.size() - 1; i++) {
				sketch += ")";
			}
			
			// then add individual chars
			for(int i = 0; i < l.size(); i++) {
				sketch += "," + l.get(i);
			}
		} else if (l.size() == 1) {
			sketch += "{" + l.get(0);
		}
		
		if(sequences.size() > 0) {
			if(sketch.contains("{")) {
				sketch += ",";
			} else {
				sketch += "{";
			}
			
			for(String sequence : sequences) {
				sketch += sequence + ",";
			}
			
			if(sketch.endsWith(",")) {
				sketch = sketch.substring(0, sketch.length() - 1);
			}
		}
		
		if(!sketch_includes.isEmpty()) {
			if(sketch.contains("{")) {
				sketch += "," + sketch_includes;
			} else {
				sketch += "{" + sketch_includes;
			}
		}
		
		if(sketch.contains("{")) {
			if(sketch.endsWith(",")) {
				sketch = sketch.substring(0, sketch.length() - 1);
			}
			sketch += "}";
		}
		
		return sketch;
	}
	

	public HashMap<String, String> regex_map = new HashMap<String, String>(); // id -> regex
	public HashMap<String, String> parent_map = new HashMap<String, String>(); // child -> parent
	
	public String getTreeVisualizationData(int continuous) {
		if(counter == 1 && !regex_map.isEmpty()) {
			regex_map.clear();
			parent_map.clear();
		} 
		
		LinkedHashMap<String, ArrayList<String>> tree = readTreeLog(continuous);
		
		String json = LogFileUtils.convertTreeBasedLogToJSON(tree);
		return json;
	}
	
	private LinkedHashMap<String, ArrayList<String>> readTreeLog(int continuous) {
		LinkedHashMap<String, ArrayList<String>> tree = new LinkedHashMap<String, ArrayList<String>>();
		if(continuous > 0) {
			// need to read previous n log files since the user has chosen to continue the synthesis for more than one 
			// iterations
			for(int i = continuous; i >= 0; i--) {
				File log_file = new File(log_dir_path2 + (counter - 1 - i) + "_tree");
				LinkedHashMap<String, ArrayList<String>> new_tree 
					= LogFileUtils.readTreeBasedLogFile(log_file, regex_map, parent_map);
				for(String parent : new_tree.keySet()) {
					ArrayList<String> children = new_tree.get(parent);
					if(tree.containsKey(parent)) {
						ArrayList<String> l = tree.get(parent);
						for(String child : children) {
							if(!l.contains(child)) {
								// merge
								l.add(child);
							}
						}
						tree.put(parent, l);
					} else {
						tree.put(parent, children);
					}
				}
			}
		} else {
			File log_file = new File(log_dir_path2 + (counter - 1) + "_tree");
			tree = LogFileUtils.readTreeBasedLogFile(log_file, regex_map, parent_map);
		}
		
		return tree;
	}

	public void pause() {
		// write the signal to the pause file
		File f = new File(pause_file_path);
		try {
			FileUtils.writeStringToFile(f, "pause-" + (counter - 1), Charset.defaultCharset(), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
