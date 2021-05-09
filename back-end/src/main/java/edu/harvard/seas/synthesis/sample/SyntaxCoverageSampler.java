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

public class SyntaxCoverageSampler extends RegexSampler{
	HashMap<String, HashSet<String>> ops_map;
	
	public SyntaxCoverageSampler(List<String> logFiles) {
		super(logFiles);
		ops_map = new HashMap<String, HashSet<String>>();
	}

	@Override
	public Set<String> sample() {
		// run the parser on each regex to extract the operators and constants used in it
		HashSet<String> all_ops = new HashSet<String>();
		for(String regex : match_map.keySet()) {
			HashSet<String> ops;
			try {
				ops = extractOperatorsAndConstants(regex);
				ops_map.put(regex, ops);
				all_ops.addAll(ops);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		ArrayList<String> all_ops_list = new ArrayList<String>(all_ops);
		
		for(String op : all_ops_list) {
			// print the set of operators/constants that have been tried by the synthesizer
			System.out.println(op);
		}
		
		// construct the matrix
		// row -- the operators and constants that have been tried
		// column -- the regexes
		// matrix[i][j] = true if the j-th regex contains the i-th operator/constant, false otherwise
		boolean[][] matrix = new boolean[all_ops.size()][match_map.size()];
		int n = 0;
		ArrayList<String> regex_list = new ArrayList<String>();
		for(String regex : match_map.keySet()) {
			// store the regexes in order for ease of future look-up
			regex_list.add(regex);
			
			HashSet<String> ops = ops_map.get(regex);
			for(int i = 0; i < all_ops_list.size(); i++) {
				if(ops.contains(all_ops_list.get(i))) {
					matrix[i][n] = true;
				} else {
					matrix[i][n] = false;
				}
			}
			n++;
		}
		
		// print the matrix to a csv file
		String path1 = log_files.get(0).getParent() + File.separator + log_files.get(0).getName() + "-matrix.csv";
		File f1 = new File(path1);
		if(f1.exists()) f1.delete();
		for(int i = 0; i < matrix.length; i++) {
			String s = "";
			for(int j = 0; j < matrix[i].length; j++) {
				s += matrix[i][j] + ",";
			}
			s = s.substring(0, s.length() - 1);
			if(i != matrix.length - 1) {
				s += System.lineSeparator();
			}
			try {
				FileUtils.write(f1, s, Charset.defaultCharset(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// print the cost to a csv file
		// set the cost of all regexes to 1
		String path2 = log_files.get(0).getParent() + File.separator + log_files.get(0).getName() + "-cost.csv";
		File f2 = new File(path2);
		if(f2.exists()) f2.delete();
		String costs = "";
		for(int i = 0; i < match_map.size(); i++) {
			costs += "1,";
		}
		costs = costs.substring(0, costs.length() - 1);
		try {
			FileUtils.write(f2, costs, Charset.defaultCharset(), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// run the set cover solver
		Set<String> sample_regex = new HashSet<String>();
		try {
			Set<String> sample_id = solve(path1, path2);
//        	System.out.println("Selected Programs:");
			for(String matrix_col : sample_id) {
				String regex = regex_list.get(Integer.parseInt(matrix_col));
//				System.out.println(regex);
				sample_regex.add(regex);
			}
			
			return sample_regex;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}		 
		
		return sample_regex;
	}
	

	private HashSet<String> extractOperatorsAndConstants(String regex) throws IOException, InterruptedException {
		HashSet<String> ops = new HashSet<String>();
		
		// run the python set cover solver
		File fError = new File("parse-error");
        File fOutput = new File("parse-output");
        
		String[] cmd = {this.python_cmd, "setcover/parser.py", regex};
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectError(fError);
        processBuilder.redirectOutput(fOutput);
        Process process = processBuilder.start();
        process.waitFor();
        
        String error_output = FileUtils.readFileToString(fError, Charset.defaultCharset());
        if(!error_output.isEmpty()) {
        	System.err.println("Regex parser throws an error.");
        	System.err.println(error_output);
        }
        String output = FileUtils.readFileToString(fOutput, Charset.defaultCharset());
        output = output.substring(1, output.length() - 2);
        String[] ss = output.split(", ");
        for(String s : ss) {
        	s = s.substring(1, s.length() - 1);
        	ops.add(s);
        }
        
        fError.delete();
        fOutput.delete();
        
        return ops;
	}

//	public static void main(String[] args) {
//		// switch between tasks
//		int k = 1;
//		
//		String log = "/Users/tz/Research/Whitebox Synthesis/logs/exemples" + k + ".txt";
//		SyntaxCoverageSampler sampler = new SyntaxCoverageSampler(log);
//		sampler.processLogFile();
//		sampler.sample();
//	}
}
