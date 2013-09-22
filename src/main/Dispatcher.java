package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import objects.Message;
import encryption.DHServlet;

@WebServlet("/")
// Mapped to hoster/id
// hoster/ does NOT work, as it goes to the MainServlet
public class Dispatcher extends HttpServlet {
	// Job: To take requests from client and reroute them to their proper
	// chatroom

	// List of chatrooms
	private static LinkedList<Chatroom> chatroomList = new LinkedList<Chatroom>();

	//Diffie Hellman Extension
	private static final String DIFFIE_HELLMAN = "/DHKE";
	
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
		int offset = id.indexOf("/Amgine_4/");
		id = id.substring(offset + "/Amgine_4".length() + 1, id.length());

		if (id.indexOf(DIFFIE_HELLMAN) != -1){
			if (!Functions.isClient(request)){
				response.getWriter().println("YOU'VE COME TO THE WRONG NEIGHBORHOOD, BROWSER...");
			}
				
			System.out.println("REQUESTING DIFFIE HELLMAN! ZOMG WHUT TO DO?!");
			id = id.substring(offset + "DIFFIE_HELLMAN".length() + 1, id.length());
			getChatroom(id).dhProcess(t, request, response);
		} else if (getChatroom(id) == null) {
			// This shouldn't happen
			System.out.println("ERROR: CHATROOM '" + id + "' NOT FOUND");
			response.getWriter().println("Chatroom doesn't exist!");
			return;
		} 

		
		
		Chatroom cr = getChatroom(id);
		if (Functions.isClient(request)) {
			//System.out.println("CHATROOM \"" + id + "\" REQUESTED BY CLIENT");

			// Sends the request and response to the chatroom for processing
			cr.process(t, request, response);
		} else {
			System.out.println("CHATROOM \"" + id + "\" REQUESTED BY BROWSER");
			
			PrintWriter out = response.getWriter();
			out.println("Welcome to chatroom \"" + id + "\"!");
			
			for (Message m : cr.msgList) {
				out.println("(" + m.time + ") " + m.sender.name + ": " + m.message);
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
