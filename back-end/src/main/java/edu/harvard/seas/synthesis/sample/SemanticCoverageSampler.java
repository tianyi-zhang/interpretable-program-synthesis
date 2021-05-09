package edu.harvard.seas.synthesis.sample;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class SemanticCoverageSampler extends RegexSampler {
	
	public SemanticCoverageSampler(List<String> logFiles) {
		super(logFiles);
	}

	@Override
	public Set<String> sample() {
		// construct the matrix
		// row -- the input-output examples
		// column -- the regexes
		// matrix[i][j] = true if the j-th regex matches the i-th example, false otherwise
		boolean[][] matrix = new boolean[example_set.size()][match_map.size()];
		int n = 0;
		
		// convert example map to a list
		ArrayList<String> example_list = new ArrayList<String>(example_set);
		
		ArrayList<String> regex_list = new ArrayList<String>();
		for(String regex : match_map.keySet()) {
			// store the regexes in order for ease of future look-up
			regex_list.add(regex);
			
			ArrayList<String> matches = match_map.get(regex);
			for(int i = 0; i < example_list.size(); i++) {
				String example = example_list.get(i);
				if(matches.contains((example) + "")) {
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
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return sample_regex;
	}
	
//	public static void main(String[] args) throws IOException, InterruptedException {
//		// switch between tasks
//		int k = 1;
//		
//		String log = "/Users/tz/Research/Whitebox Synthesis/logs/exemples" + k + ".txt";
//		SemanticCoverageSampler sampler = new SemanticCoverageSampler(log);
//		sampler.processLogFile();
//		sampler.sample();
//	}
}
