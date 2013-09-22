package main;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

import objects.User;


//Mapped to hoster/Amgine_4/
//Nothing further than that

@WebServlet("")
public class MainServlet extends HttpServlet {
//Jobs:
	//To create chatrooms (POST)
	//To add users to chatrooms (GET)
	
	//NOTE: ALL CLIENT REQUESTS TO THIS SERVLET MUST HAVE THIS HEADER FIELD SET TO BE TRUE
	//NOTE: HEADER, NOT PARAMS
	public static final String CLIENT_ID = "Client"; 
	public static final String BROWSER_MESSAGE = "GTFO YA DIRTY DAMN BROWSER";
	
	
	//PARAMETER/HEADER COMMANDS
	
	//Parameter to create a chatroom
	private static final String CREATE_CMD = "CREATECHATR";
	//Value: Chatroom name
	
	//Header to join a chatroom
	private static final String JOIN_CMD = "JOINCHATR";
	//Value: Chatroom name (must have been created already)
	
	//Parameter/Header to determine name of client
	private static final String NAME = "NAME";
	//Value: Name
	
	
	//To create chatroom:
	//Command is : "CREATECHATR", "<crname>" in Parameters
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException{
		if (Functions.isClient(request)){
			
			if (Dispatcher.getChatroom(request.getParameter(CREATE_CMD)) !=null){
				System.out.println("Chatroom " + request.getParameter(CREATE_CMD) + " already exists!");
				return;
			}
			
			//Creates a chatroom and adds the user to it
			Chatroom cr = new Chatroom(request.getParameter(CREATE_CMD));
			cr.addUser(new User(request.getParameter(NAME), request.getRemoteAddr()));
			
			//Returns location of chatroom
			response.getWriter().print(request.getRequestURL().toString() + cr.id);
		} else {
			response.getWriter().println(BROWSER_MESSAGE);
			return;
		}
	}
	
	//To join chatroom
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException{
		if (Functions.isClient(request)){
			//Adds the user to the requested chatroom
			if (Dispatcher.getChatroom(request.getHeader(JOIN_CMD)) != null){
				
				if (Dispatcher.getChatroom(request.getHeader(JOIN_CMD)).userList.size() >= 2){
					System.out.println("Chatroom is full! >2 member chatrooms not yet implemented!");
					return;
				}
				User u = new User(request.getHeader(NAME), request.getRemoteAddr());
				Chatroom cr = Dispatcher.getChatroom(request.getHeader(JOIN_CMD));
				cr.addUser(u);
				
				//Returns location of chatroom
				response.getWriter().print(request.getRequestURL().toString() + cr.id);
			} else {
				System.out.println("CHATROOM DOES NOT EXIST");
				response.getWriter().println("CHATROOM ID INVALID");
			}
			
		} else {
			response.getWriter().println(BROWSER_MESSAGE);
			response.getWriter().close();
			return;
		}
	}
}
