package main;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

import objects.User;

//Mapped to hoster/Amgine_4/
//Nothing further than that

@WebServlet("")
public class MainServlet extends HttpServlet {
	// Jobs:
	// To create chatrooms (POST)
	// To add users to chatrooms (GET)

	private static final long serialVersionUID = 1L;
	// NOTE: ALL CLIENT REQUESTS TO THIS SERVLET MUST HAVE THIS HEADER FIELD SET
	// TO BE TRUE
	// NOTE: HEADER, NOT PARAMS
	public static final String CLIENT_ID = "Client";

	// PARAMETER/HEADER COMMANDS

	// Parameter to create a chatroom
	private static final String CREATE_CMD = "CREATECHATR";
	// Value: Chatroom name

	// Header to join a chatroom
	private static final String JOIN_CMD = "JOINCHATR";
	// Value: Chatroom name (must have been created already)

	// Parameter/Header to determine name of client
	private static final String NAME = "NAME";

	// Value: Name

	// To create chatroom:
	// Command is : "CREATECHATR", "<crname>" in Parameters
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (Functions.isClient(request)) {

			if (Dispatcher.getChatroom(request.getParameter(CREATE_CMD)) != null) {
				System.out
						.println("Chatroom " + request.getParameter(CREATE_CMD)
								+ " already exists!");
				response.getWriter().print("ERROR");
				return;
			}

			// Creates a chatroom and adds the user to it
			Chatroom cr = new Chatroom(request.getParameter(CREATE_CMD));
			cr.addUser(new User(request.getParameter(NAME), request
					.getRemoteAddr()));

			// Returns location of chatroom
			response.getWriter().print(
					request.getRequestURL().toString() + cr.id);
		} else {
			response.getWriter().println("ERROR");
			response.getWriter().close();
			return;
		}
	}

	// To join chatroom
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		PrintWriter pw = response.getWriter();
		if (Functions.isClient(request)) {
			// Adds the user to the requested chatroom
			if (Dispatcher.getChatroom(request.getHeader(JOIN_CMD)) != null) {

				if (Dispatcher.getChatroom(request.getHeader(JOIN_CMD)).userList
						.size() >= 2) {
					System.out
							.println("Chatroom is full! >2 member chatrooms not yet implemented!");
					pw.print("ERROR");
					return;
				}

				User u = new User(request.getHeader(NAME),
						request.getRemoteAddr());
				Chatroom cr = Dispatcher.getChatroom(request
						.getHeader(JOIN_CMD));
				cr.addUser(u);

				// Returns location of chatroom
				pw.print(request.getRequestURL().toString() + cr.id);
			} else {
				System.out.println("CHATROOM \"" + request.getHeader(JOIN_CMD)
						+ "\" DOES NOT EXIST");
				pw.print("ERROR");
			}
		} else {
			pw.println("Welcome to the main page of my servlet for Amgine 4!");
			pw.println("");
			pw.println("To use Amgine 4, contact Michael for the .jar application.");
			pw.println("If you would like to read recent conversations (unencrypted), just go to currentlink/chatroomID to read the messages in the chatroom.");
			pw.println("Thanks for checking out Amgine 4!");
			pw.println("\nHosting provided by Red Hat Cloud");

			pw.println("\n\n---Chatrooms---");
			for (Chatroom c : Dispatcher.chatroomList) {
				String s = "";
				for (User u : c.userList) {
					s += u.name + ", ";
				}
				if (!s.equals("")) s = s.substring(0, s.length() - 2);
				pw.println("\t" + c.id + " (" + s + ")" + " - " + Dispatcher.HOSTER + c.id);
			}
		}
		response.getWriter().close();
	}
}
