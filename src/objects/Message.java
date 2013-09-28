package objects;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import main.Functions;

public class Message {
	public String time;
	public String date;
	
	public String message;
	public User sender;
	
	//False if there is a new message to be pulled by the Thread of the receiver client
	public boolean received = false;

	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat(
			"HH:mm:ss");
	
	public Message(String msg, User u) {
		sender = u;
		message = msg;
		date = Functions.getTime(DATE_FORMAT);
		time = Functions.getTime(TIME_FORMAT);
	}
}
