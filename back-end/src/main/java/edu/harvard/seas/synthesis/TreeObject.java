package edu.harvard.seas.synthesis;

public class TreeObject{
	public String id;
	public String regex;
	public int number;
	public TreeObject[] children;
	public int leaf_count;

	public TreeObject(String id, String regex, int total_descendants, TreeObject[] children) {
		this.id = id;
		this.regex = regex;
		this.number = total_descendants;
		this.children = children;
	}
}
