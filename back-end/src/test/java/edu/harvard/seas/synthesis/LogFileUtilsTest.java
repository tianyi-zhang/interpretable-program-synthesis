package edu.harvard.seas.synthesis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

public class LogFileUtilsTest {
	
	@Test
	public void testParseTreeLog() {
		String filename = "src/test/resources/tree_log.txt";
		File f = new File(filename);
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> parent_map = new HashMap<String, String>();
		LinkedHashMap<String, ArrayList<String>> tree = 
				LogFileUtils.readTreeBasedLogFile(f, map, parent_map);
		assertEquals(2, tree.size());
		assertEquals(32, map.size());
		
		String root = "v:?";
		List<String> children = tree.get(root);
		assertEquals(13, children.size());
		assertEquals("concat(v:?,v:?)", children.get(0));
		assertEquals(null, parent_map.get("v:?"));
		assertEquals(root, parent_map.get(children.get(0)));
		
		String sketch = "or(v:?,v:?)";
		List<String> children2 = tree.get(sketch);
		assertEquals(11, children2.size());
		assertEquals("or(v:?,<num>)", children2.get(0));
		assertEquals(root, parent_map.get(sketch));
		assertEquals(sketch, parent_map.get(children2.get(0)));
	}
	
	@Test
	public void testParseLargeTreeLog() {
		String filename = "src/test/resources/tree_log_large.txt";
		File f = new File(filename);
		HashMap<String, String> map = new HashMap<String, String>(); 
		HashMap<String, String> parent_map = new HashMap<String, String>();
		LinkedHashMap<String, ArrayList<String>> tree = 
				LogFileUtils.readTreeBasedLogFile(f, map, parent_map);
		String json = LogFileUtils.convertTreeBasedLogToJSON(tree);
		System.out.println(json);
		
		assertEquals(587, map.size());
	}
	
	
	@Test
	public void testParseTreeLogToJson() {
		String filename = "src/test/resources/tree_log.txt";
		File f = new File(filename);
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> parent_map = new HashMap<String, String>();
		LinkedHashMap<String, ArrayList<String>> tree = 
				LogFileUtils.readTreeBasedLogFile(f, map, parent_map);
		String json = LogFileUtils.convertTreeBasedLogToJSON(tree);
		System.out.println(json);
		
		assertEquals(32, map.size());
	}
	
	@Test
	public void testContinuousSynthesisLog() {
		String filename = "src/test/resources/tree_log_continuous.txt";
		File f = new File(filename);
		HashMap<String, String> backup = new HashMap<String, String>();
		backup.put("96", "or(<+>,v:?)");
		HashMap<String, String> parent_map = new HashMap<String, String>();
		parent_map.put("or(<+>,v:?)", "or(v:?,v:?)");
		parent_map.put("or(v:?,v:?)", "v:?");
		
		LinkedHashMap<String, ArrayList<String>> tree = LogFileUtils.readTreeBasedLogFile(f, backup, parent_map);
		ArrayList<String> children = tree.get("or(<+>,v:?)");
		assertEquals(9, children.size());
		assertEquals(1, tree.get("v:?").size());
		assertEquals(1, tree.get("or(v:?,v:?)").size());
		
		String json = LogFileUtils.convertTreeBasedLogToJSON(tree);
		
	}
	
	@Test
	public void testReadHeartbeat() {
		String filename = "src/test/resources/0-small";
		File f = new File(filename);
		HashMap<String, String> dsl_to_regex = new HashMap<String, String>();
		ArrayList<Heartbeat> heartbeats = LogFileUtils.readLogFileToHeartbeat(f, dsl_to_regex);
		assertEquals(1336, heartbeats.size());
		// this log file does not have automaton regexes logged, so the size is 0
		assertEquals(0, dsl_to_regex.size());
		
		assertEquals(3, heartbeats.get(0).example_num);
		assertEquals("optional(<0>)", heartbeats.get(0).regex);
	}
	
	@Test
	public void testReadAutomatonRegex() {
		String filename = "src/test/resources/0-small-with-automata";
		File f = new File(filename);
		HashMap<String, String> dsl_to_regex = new HashMap<String, String>();
		ArrayList<Heartbeat> heartbeats = LogFileUtils.readLogFileToHeartbeat(f, dsl_to_regex);
		assertEquals(19, heartbeats.size());
		// this log file does not have automaton regexes logged, so the size is 0
		assertEquals(19, dsl_to_regex.size());
		assertTrue(dsl_to_regex.containsKey("startwith(<A>)"));
		assertEquals("([A]).*", dsl_to_regex.get("startwith(<A>)"));
	}
	
	@Test
	public void testParseTreeLogWithLoops() {
		String filename = "src/test/resources/tree_log_loop.txt";
		File f = new File(filename);
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> parent_map = new HashMap<String, String>();
		LinkedHashMap<String, ArrayList<String>> tree = 
				LogFileUtils.readTreeBasedLogFile(f, map, parent_map);
		assertEquals(194, tree.size());
		assertEquals(4626, map.size());
		
		String root = "v:?";
		List<String> children = tree.get(root);
		assertEquals(11, children.size());
		assertEquals("or(v:?,v:?)", children.get(0));
		assertEquals(null, parent_map.get("v:?"));
		assertEquals(root, parent_map.get(children.get(0)));
		
		String sketch = "or(v:?,v:?)";
		List<String> children2 = tree.get(sketch);
		assertEquals(14, children2.size());
		assertEquals("or(startwith(v:?),v:?)", children2.get(0));
		assertEquals(root, parent_map.get(sketch));
		assertEquals(sketch, parent_map.get(children2.get(0)));
		
		// this call should not step into a loop and throw StackOverflow exception
		String json = LogFileUtils.convertTreeBasedLogToJSON(tree);
	}
	
	@Test
	public void testParseTreeLogWithNoRoot() {
		String filename = "src/test/resources/tree_log_no_root.txt";
		File f = new File(filename);
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> parent_map = new HashMap<String, String>();
		LinkedHashMap<String, ArrayList<String>> tree = 
				LogFileUtils.readTreeBasedLogFile(f, map, parent_map);
		assertFalse(map.containsKey("v:?"));
		String json = LogFileUtils.convertTreeBasedLogToJSON(tree);
		assertFalse(json.trim().isEmpty());
		assertTrue(json.contains("tree-node-dummy"));
	}
}
