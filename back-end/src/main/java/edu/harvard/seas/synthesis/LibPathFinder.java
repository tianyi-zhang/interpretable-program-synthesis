package edu.harvard.seas.synthesis;

public class LibPathFinder {
	public static void main(String[] args) {
        String javaLibPath = System.getProperty("java.library.path");
        System.out.println(javaLibPath);
    }
}
