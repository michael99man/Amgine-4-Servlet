package objects;

import java.math.BigInteger;

public class User {
	public String IP;
	public String name;
	
	public boolean joined;
	
	public BigInteger publicModulus;
	public BigInteger publicExponent;
	public boolean received;
	
	public User(String name, String IP){
		this.IP = IP;
		this.name = name;
	}
}
