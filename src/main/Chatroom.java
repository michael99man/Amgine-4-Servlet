package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objects.Message;
import objects.User;

//Not a servlet. Uses the Dispatcher to receive info
public class Chatroom {

	// Commands
	private static final String SEND_MSG = "SEND_MESSAGE";
	private static final String NAME = "NAME";
	// To tell client that there is nothing to receive (header)
	private static final String NOTHING_NEW = "NOTHING_NEW";

	private static final String LEAVE_CMD = "LEAVE_CHATROOM";

	public String id;
	public LinkedList<User> userList = new LinkedList<User>();

	public LinkedList<Message> msgList = new LinkedList<Message>();

	// True if ready to begin RSA
	public boolean RSAready = false;

	// User representation of Server
	private User instance;

	public Chatroom(String id) {
		this.id = id;
		Dispatcher.addChatroom(this);
		instance = new User("Server", id);
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
				Message m = new Message(request.getParameter(SEND_MSG), u,
						request.getParameter("ENCRYPTED").equalsIgnoreCase(
								"TRUE"));
				msgList.add(m);
				System.out.println("Received message: " + m.message);
				response.getWriter().println(
						"SERVLET HAS RECEIVED MESSAGE, " + u.name);
			} else if (Functions.contains(request.getParameterNames(),
					LEAVE_CMD)) {
				// When a user leaves the chatroom
				User u = getUser(request.getParameter(NAME));
				userList.remove(u);
				Message m = new Message(u.name + " has left the chatroom.",
						instance, false);
				msgList.add(m);
			}
		} else if (t.equals(Type.GET)) {

			// Called only when the Thread pulls
			if (Functions.has(request, "PULL", "true")) {
				User u = getUser(request.getHeader(NAME));
				PrintWriter out = response.getWriter();

				boolean sent = false;
				for (Message m : msgList) {
					// If the message has not yet been received, and the
					// PullThread does NOT belong to the client that sent the
					// message...
					if (!m.received && !(m.sender.equals(u))) {
						System.out.println("SENT MESSAGE TO CLIENT");
						out.print("(Message: " + m.message + ")");
						out.print("(Encrypted: "
								+ (m.encrypted ? "TRUE)" : "FALSE)"));
						out.print("(Sender: " + m.sender.name + ")");
						out.print("(Date: " + m.date + ")");
						out.print("(Time: " + m.time + ")");

						// Creates a new row to aid parsing
						out.println();

						m.received = true;
						System.out.println("SENT MESSAGE \"" + m.message
								+ "\" TO USER \"" + u.name + "\"");
						sent = true;
					}
				}

				if (!sent) {
					// Nothing to be pulled!
					out.print(NOTHING_NEW);
				}
			}
		}
	}

	// RSA Processing; Only used to get/set Public key and exponent
	public void rsaProcess(Type t, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();

			if (t.equals(Type.GET)) {
				// This is a request to get the Public Modulus and Public
				User u = getOtherUser(request.getHeader("NAME"));
				if (u.received) {
					if (Functions.contains(request.getHeaderNames(), "MODULUS")){
						System.out.println("Returned " + u.name + "'s modulus");
						out.print(u.publicModulus);
					} else if (Functions.contains(request.getHeaderNames(), "EXPONENT")){
						System.out.println("Returned " + u.name + "'s exponent");
						out.print(u.publicExponent);
					} else {
						System.out.println("WTF: Bad GET Request");
					}
				} else {
					out.print("NOT_READY");
				}
			} else if (t.equals(Type.POST)) {
				User u = getUser(request.getParameter("NAME"));
				if (u == null){
					System.out.println("NO USER BY THE NAME OF " + request.getHeader("NAME"));
				}
				
				u.publicModulus = new BigInteger(
						request.getParameter("MODULUS"));
				System.out.println(u.name + "'s public modulus: " + u.publicModulus);
				u.publicExponent = new BigInteger(
						request.getParameter("EXPONENT"));
				System.out.println(u.name + "'s public exponent: " + u.publicExponent);
				u.received = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private User getOtherUser(String userID) {
		for (User u : userList) {
			if (!u.name.equalsIgnoreCase(userID)) {
				return u;
			}
		}
		return null;
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
		msgList.add(new Message(user.name + " has joined the chatroom!",
				instance, false));

		System.out.println("USER \"" + user.name
				+ "\" HAS BEEN ADDED TO CHATROOM \"" + id + "\"");
	}

}
