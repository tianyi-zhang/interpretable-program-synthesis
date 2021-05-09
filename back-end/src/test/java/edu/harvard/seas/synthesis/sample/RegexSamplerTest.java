package edu.harvard.seas.synthesis.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class RegexSamplerTest {
	@Test
	public void testLogAnalysis() {
		String log = "src/test/resources/0";
		List<String> l = new ArrayList<String>();
		l.add(log);
		SemanticCoverageSampler sampler = new SemanticCoverageSampler(l);
		sampler.processLogFile();
		assertEquals(6, sampler.example_set.size());
		assertEquals(36968, sampler.match_map.size());
		
		String regex = "or(<0>,<+>)";
		ArrayList<String> matches = sampler.match_map.get(regex);
		assertEquals(4, matches.size());
		assertTrue(matches.contains("+---------true"));
		assertTrue(matches.contains("++---------false"));
		assertTrue(matches.contains("abcdefg---------false"));
		assertTrue(matches.contains(">?&*(---------false"));
	}
	
	@Test
	public void testSemanticCoverageSampling() {
		String log = "src/test/resources/0";
		List<String> l = new ArrayList<String>();
		l.add(log);
		SemanticCoverageSampler sampler = new SemanticCoverageSampler(l);
		sampler.processLogFile();
		Set<String> set = sampler.sample();
		assertEquals(2, set.size());
	}
	
	@Test
	@Ignore
	public void testSyntacticCoverageSampling() {
		String log = "src/test/resources/0-small";
		List<String> l = new ArrayList<String>();
		l.add(log);
		SyntaxCoverageSampler sampler = new SyntaxCoverageSampler(l);
		sampler.processLogFile();
		Set<String> set = sampler.sample();
		assertEquals(23, set.size());
	}
}
