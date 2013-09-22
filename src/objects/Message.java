package objects;

import java.util.Date;

public class Message {
	public String time;
	public String message;
	public User sender;
	
	//False if there is a new message to be pulled by the Thread of the receiver client
	public boolean received = false;

	public Message(String msg, User u) {
		sender = u;
		message = msg;
		time = new Date().toString();
	}
}
