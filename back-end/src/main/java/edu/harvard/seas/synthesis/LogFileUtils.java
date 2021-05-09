package edu.harvard.seas.synthesis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogFileUtils {
	
	public static LinkedHashMap<String, ArrayList<String>> readTreeBasedLogFile(File log_file, 
			HashMap<String, String> prev_map, HashMap<String, String> parent_map) {
		LinkedHashMap<String, ArrayList<String>> tree = new LinkedHashMap<String, ArrayList<String>>();
		HashMap<String, String> new_map = new HashMap<String, String>();
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(log_file, "UTF-8");
			while(it.hasNext()) {
				String line = it.nextLine();
				if(line.trim().isEmpty()) {
					continue;
				}
				
				String id = line.substring(0, line.indexOf(':')).trim();
				line = line.substring(line.indexOf(':') + 1).trim();
				String regex = line.substring(0, line.lastIndexOf(':')).trim();
				
				// comment out the code below since it's hard to convert it back when repeatatleast is marked as 
				// a starting/avoided branch in the next iteration
//				if(regex.contains("-1")) {
//					// handle repeat(?, -1), repeatatleast(?, -1), repeatrange(?, -1, -1)
//					regex = regex.replaceAll("-1", "?");
//				}
				
				if(!id.equals("-1")) {
					// this is not a concrete regex (not a leaf in the tree)
					// need to store its id and regex for further lookup
					new_map.put(id, regex);
				}
				
				String parentId = line.substring(line.lastIndexOf(':') + 1).trim();
				if(parentId.equals("0")) {
					// this is the root regex, v:?
					continue;
				}
				
				String parent = new_map.get(parentId); // theoretically the parent regex is always logged first
				if(parent == null) {
					// the parent node is logged in the previous iterations
					parent = prev_map.get(parentId);
					
					// continue to add ancestors till the root node
					String grand_parent = parent_map.get(parent);
					String dummy = parent; // use a separate copy of parent 
					while(grand_parent != null) {
						ArrayList<String> children;
						if(tree.containsKey(grand_parent)) {
							children = tree.get(grand_parent);
						} else {
							children = new ArrayList<String>();
						}
						
						// check if this regex is redundant before adding it to the list
						if(!children.contains(dummy)) {
							children.add(dummy);
						}
						
						tree.put(grand_parent, children);
						
						// continue to look up
						dummy = grand_parent;
						grand_parent = parent_map.get(grand_parent);
					}
				}
				
				if(parent == null) {
					System.err.println("How could the parent be null? It seems some previous logs are not read properly.");
					System.err.println("Child: " + regex + " ParentID: " + parentId);
					parent = "v:?";
				}
				
				// add the parent-child relationship to the parent map for future lookup
				if(!parent.equals(regex)) {
					parent_map.put(regex, parent);
				}
				
				ArrayList<String> children;
				if(tree.containsKey(parent)) {
					children = tree.get(parent);
				} else {
					children = new ArrayList<String>();
				}
				
				// check if this regex is redundant before adding it to the list
				if(!children.contains(regex)) {
					children.add(regex);
				}
				tree.put(parent, children);
			}
			
			// put all new id-regex pairs to the prev_regex map
			prev_map.putAll(new_map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// since we strip away constraints like ?{...} from sketches
		// now we have many duplicated sketches, which causes loops because parent and child regexes are the same
		// need to remove those regexes
		LinkedHashMap<String, ArrayList<String>> deduplicate = new LinkedHashMap<String, ArrayList<String>>();
		for(String parent : tree.keySet()) {
			ArrayList<String> children = tree.get(parent);
			if(children.contains(parent)) {
				children.remove(parent);
			}
			deduplicate.put(parent, children);
		}
		
		return deduplicate;
	}

	public static TreeObject convertTreeBasedLogToTreeObject(LinkedHashMap<String, ArrayList<String>> tree) {
		if(tree.isEmpty()) {
			return null;
		}
		
		int id_count = 0;
		HashMap<String, TreeObject> map = new HashMap<String, TreeObject>();
		for(String parent : tree.keySet()) {
			ArrayList<String> children = tree.get(parent);
			
			// set a cap on the number of tree nodes to show
			ArrayList<String> nodesToShow;
			if(children.size() > 20) {
				// take a random sample
				nodesToShow = getRandomSubset(children, 20, children.size());
			} else {
				// show all children as tree nodes
				nodesToShow = children;
			}
			
			TreeObject o;
			if(map.containsKey(parent)) {
				o = map.get(parent);
			} else {
				// summarize the total number of regexes that can be derived from this sketch
				int count = countDescendants(tree, parent);
				o = new TreeObject("tree-node-" + id_count, parent, count, new TreeObject[nodesToShow.size()]);
				id_count++;
			}
			
			for(int i = 0; i < nodesToShow.size(); i++) {
				String child = nodesToShow.get(i);
				List<String> l = tree.get(child);
				int size;
				if(l == null) {
					size = 0;
				} else {
					size = l.size();
				}
				
				if(size > 20) {
					// also apply the cap here
					size = 20;
				}
				
				TreeObject o2;
				if(map.containsKey(child)) {
					o2 = map.get(child);
				} else {
					int count = countDescendants(tree, child);
					
					o2 = new TreeObject("tree-node-" + id_count, child, count, new TreeObject[size]);
					id_count++;
					
					map.put(child, o2);
				}
				
				o.children[i] = o2;
			}
			
			map.put(parent, o);
		}
		
		TreeObject root = map.get("v:?");
		
		if(root == null) {
			// there is no v:? in tree logs
			// it is very likely that a user annotated several tree branches to start from 
			// create a dummy root instead
			List<TreeObject> list = new ArrayList<TreeObject>();
			int sum = 0;
			for(String regex : tree.keySet()) {
				// check whether this regex has a parent in the current tree
				boolean hasNoParent = true;
				for(ArrayList<String> children : tree.values()) {
					if(children.contains(regex)) {
						hasNoParent = false;
						break;
					}
				}

				if(hasNoParent) {
					TreeObject tNode = map.get(regex);
					sum += tNode.number;
					list.add(tNode);
				}
			}

			TreeObject[] arr = new TreeObject[list.size()];
			arr = list.toArray(arr);
			root = new TreeObject("tree-node-dummy", "v:?", sum, arr);
		}

		if(root != null) {
			processLeafCount(root);
		}
		
		return root;
	}

	public static String convertTreeBasedLogToJSON(LinkedHashMap<String, ArrayList<String>> tree) {
		TreeObject root = convertTreeBasedLogToTreeObject(tree);

		if(root == null){
			return "";
		}

		ObjectMapper om = new ObjectMapper();
		String json = "";
		try {
			json = om.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return json.replaceAll("v:\\?", "?");
	}

	private static void processLeafCount(TreeObject treeObject){
		if (treeObject.children.length == 0) {
			if(treeObject.regex.contains("?")) {
				treeObject.leaf_count = 0;
				return;
			}
			treeObject.leaf_count = 1;
			return;
		}

		for (TreeObject child: treeObject.children) {
			processLeafCount(child);
		}

		treeObject.leaf_count = Stream.of(treeObject.children).mapToInt(child -> child.leaf_count).sum();
	}

	private static ArrayList<String> getRandomSubset(ArrayList<String> list, int n, int bound) {
		Random randNum = new Random();
		Set<Integer> set = new LinkedHashSet<Integer>();
		while (set.size() < n) {
			set.add(randNum.nextInt(bound));
		}
		
		ArrayList<String> subList = new ArrayList<String>();
		for(int k : set) {
			subList.add(list.get(k));
		}
		
		return subList;
	}
	
	private static int countDescendants(LinkedHashMap<String, ArrayList<String>> tree, String node) {
		int count = 0;
		
		if(tree.containsKey(node)) {
			List<String> children = tree.get(node);
			for(String child : children) {
				count ++;
				count += countDescendants(tree, child);
			}
		}
		
		return count;
	}
	
	public static ArrayList<Integer> readLogFile(File log_file) {
		ArrayList<Integer> arr = new ArrayList<Integer>();
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(log_file, "UTF-8");
			while(it.hasNext()) {
				String line = it.nextLine();
				if (line.startsWith("matching indices:")) {
					line = line.substring(17).trim();
					arr.add(line.split(" ").length);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(it != null) {
				LineIterator.closeQuietly(it);
			}
		}
		
		return arr;
	}
	
	public static ArrayList<Heartbeat> readLogFileToHeartbeat(File log_file, HashMap<String, String> dsl_to_automaton) {
		ArrayList<Heartbeat> arr = new ArrayList<Heartbeat>();
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(log_file, "UTF-8");
			String cur_regex = "";
			while(it.hasNext()) {
				String line = it.nextLine();
				if(line.trim().isEmpty()) {
					continue;
				}
				
				if(line.startsWith("program: ")) {
					cur_regex = line.substring(line.indexOf(':') + 1).trim();
				}
				
				if(line.startsWith("automaton:")) {
					String automaton_regex = line.substring(line.indexOf(':') + 1).trim();
					dsl_to_automaton.put(cur_regex, automaton_regex);
				}
				
				if (line.startsWith("matching indices:")) {
					line = line.substring(17).trim();
					int num = line.split(" ").length;
					if(!cur_regex.isEmpty()) {
						Heartbeat hb = new Heartbeat(num, cur_regex);
						if(!arr.contains(hb)) {
							arr.add(hb);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(it != null) {
				LineIterator.closeQuietly(it);
			}
		}
		
		return arr;
	}
}
