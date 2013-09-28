package encryption;


public class DHUser {

	public String name;
	public int currentValue = 0;
	
	//If currentValue has been received by this client
	public boolean received = false;
	
	//Received base/mod
	public boolean rBase = false;
	public boolean rMod = false;
	
	public DHUser(String n){
		name = n;
	}

	public void clear() {
		rBase = false;
		rMod = false;
		received = false;
		currentValue = 0;
	}
	
}
