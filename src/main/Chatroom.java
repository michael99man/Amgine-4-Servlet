package main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.BasicHttpEntity;

import objects.Message;
import objects.User;

//Not a servlet. Uses the Dispatcher to receive info
public class Chatroom {

	// Commands
	private static final String SEND_MSG = "SEND_MESSAGE";
	private static final String NAME = "NAME";
	// To tell client that there is nothing to receive (header)
	private static final String NOTHING_NEW = "NOTHING_NEW";

	// To tell the client what the message is (header)
	private static final String MESSAGE = "MESSAGE";

	// To tell the client who the sender was (header)
	private static final String SENDER = "SENDER";

	// To tell the client when the message was sent (header)
	private static final String TIME = "TIME";

	public String id;
	public LinkedList<User> userList = new LinkedList<User>();

	public LinkedList<Message> msgList = new LinkedList<Message>();

	public Chatroom(String id) {
		this.id = id;
		Dispatcher.addChatroom(this);
	}

	public enum Type {
		POST, GET
	}

	// Will have to deal with PullThread's GET Requests
	public void process(Type t, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// Receives this from dispatcher servlet
		if (t.equals(Type.POST)) {
			if (Functions.contains(request.getParameterNames(), SEND_MSG)) {

				User u = getUser(request.getParameter(NAME));
				Message m = new Message(request.getParameter(SEND_MSG), u);
				msgList.add(m);
				System.out.println("Received message: " + m.message);
				response.getWriter().println("RECEIVED MESSAGE, " + u.name);
			}
		} else if (t.equals(Type.GET)) {

			// If the thread is pulling...
			if (Functions.has(request, "PULL", "true")) {
				User u = getUser(request.getHeader(NAME));
				PrintWriter out = response.getWriter();

				for (Message m : msgList) {
					//If the message has not yet been received, and the PullThread does NOT belong to the client that sent the message...
					if (!m.received && !(m.sender.equals(u))) {
						System.out.println("SENT MESSAGE TO CLIENT");
						out.print("(Message: " + m.message + ")");
						out.print("(Sender: " + m.sender.name + ")");
						out.print("(Time: " + m.time + ")");
						m.received = true;
					}
				}

				// Nothing to be pulled!
				out.print(NOTHING_NEW);
			}

		}
	}

	// Diffie-Hellman processing
	public void dhProcess(Type t, HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

	public User getUser(String userID) {
		for (User u : userList) {
			if (u.name.equalsIgnoreCase(userID)) {
				return u;
			}
		}
		return null;
	}

	public boolean hasUser(String userID) {
		for (User u : userList) {
			if (u.name.equalsIgnoreCase(userID)) {
				return true;
			}
		}
		return false;
	}

	public void addUser(User user) {
		userList.add(user);
		msgList.add(new Message(user.name + " has joined the chatroom!", user));

		System.out.println("USER \"" + user.name
				+ "\" HAS BEEN ADDED TO CHATROOM \"" + id + "\"");
	}
}
