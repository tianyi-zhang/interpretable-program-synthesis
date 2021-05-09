package edu.harvard.seas.synthesis;

public class Regex {
	public String regex;
	public String[] include;
	public String[] exclude;
	public String[] maybe;
	
	@Override
	public String toString() {
		return regex;
	}
}
