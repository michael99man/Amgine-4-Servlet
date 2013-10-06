package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Locale;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objects.Message;
import objects.User;

@WebServlet("/")
// Mapped to hoster/id
// hoster/ does NOT work, as it goes to the MainServlet
public class Dispatcher extends HttpServlet {
	// Job: To take requests from client and reroute them to their proper
	// chatroom

	// List of chatrooms
	public static LinkedList<Chatroom> chatroomList = new LinkedList<Chatroom>();

	// Diffie Hellman Extension
	private static final String EXTENSION = "/RSA";

	// Hoster
	// public static final String HOSTER = "amgine4-michael99man.rhcloud.com/";
	public static final String HOSTER = "localhost:8080/Amgine_4/";

	private static final long serialVersionUID = -8110153131733414341L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		process(Chatroom.Type.GET, request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		process(Chatroom.Type.POST, request, response);
	}

	private void process(Chatroom.Type t, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String id = request.getRequestURL().toString();

		// To account for changing hoster URL
		int offset = id.indexOf(HOSTER);

		id = id.substring(offset + HOSTER.length(), id.length());

		boolean rsa = false;
		
		int i = id.indexOf(EXTENSION);
		if (i == -1) i = id.indexOf(EXTENSION.toLowerCase(Locale.US));
		
		if (i!=-1) {
			rsa = true;
			id = id.substring(0, i);
		}

		if (getChatroom(id) == null) {
			// This would only happen if a browser tried to access an invalid
			// URL
			System.out.println("ERROR: CHATROOM '" + id + "' NOT FOUND");
			response.getWriter()
					.println("Chatroom '" + id + "' doesn't exist!");
			return;
		} else if (rsa) {
			if (!Functions.isClient(request)) {
				PrintWriter out = response.getWriter();
				out.println("RSA INFO FOR CHATROOM: '" + id + "'");
				for (User u : getChatroom(id).userList) {
					out.println(u.name + ":");
					if (u.received) {
						out.println("\tModulus: " + u.publicModulus);
						out.println("\tExponent: " + u.publicExponent);
					} else {
						out.println("\tNot received");
					}
				}
				return;
			} else {
				System.out.println("RSA Process: " + t.name());
				getChatroom(id).rsaProcess(t, request, response);
				return;
			}
		}

		Chatroom cr = getChatroom(id);
		if (Functions.isClient(request)) {
			// System.out.println("CHATROOM \"" + id +
			// "\" REQUESTED BY CLIENT");

			// Sends the request and response to the chatroom for processing
			cr.process(t, request, response);
		} else {
			System.out.println("CHATROOM \"" + id + "\" REQUESTED BY BROWSER");

			PrintWriter out = response.getWriter();
			out.println("Welcome to chatroom \"" + id + "\"!");

			for (Message m : cr.msgList) {
				out.println(m.date + " - " + m.time + " " + m.sender.name
						+ ": " + m.message);
			}
			out.close();
		}
	}

	public static void addChatroom(Chatroom cr) {
		chatroomList.add(cr);
	}

	public static Chatroom getChatroom(String ID) {
		for (Chatroom cr : chatroomList) {
			if (cr.id.equalsIgnoreCase(ID)) {
				// Returns the chatroom
				return cr;
			}
		}
		return null;
	}
}
