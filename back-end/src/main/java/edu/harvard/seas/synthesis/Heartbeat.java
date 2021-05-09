package edu.harvard.seas.synthesis;

public class Heartbeat {
	public int example_num; // the number of satisfied examples
	public String regex; // the regular expression
	
	public Heartbeat(int count, String regex) {
		this.example_num = count;
		this.regex = regex;
	}
	
	@Override
	public int hashCode() {
		int hash = 17;
        hash = 31 * example_num + hash;
        hash = 31 * regex.hashCode() + hash;
        return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Heartbeat) {
			Heartbeat other = (Heartbeat) o;
			if(other.example_num == this.example_num && other.regex.equals(this.regex)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "The number of satisfied examples: " + example_num + " --- Regex: " + regex;
	}
}
