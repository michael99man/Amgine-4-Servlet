package encryption;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.Functions;


//NOT GOING TO BE AN ACTUAL SERVLET
public class DHServlet{
	private static final long serialVersionUID = 1L;

	public User USER1;
	public User USER2;

	public State state = State.created;

	public static int mod;
	public static int base;

	private static Random rand = new Random();
	private static Random ran = new SecureRandom();

	private enum State {
		// First state
		created,

		// After both clients have connected
		// After server has generated the public modulus and base
		initialized,

		// After both clients have received public modulus and base,
		// And during DiffieHellman Key Exchange
		dhke,

		// Ready to do it again
		finished,

	}

	public void init(ServletConfig config) {
		
		
		Enumeration<String> e = config.getInitParameterNames();
	
		System.out.println("INIT");
		
		while (e.hasMoreElements()){
			System.out.println(e.nextElement());
		}
	}

	public DHServlet() {
	}

	// To be used solely for getting values for Diffie Hellman
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		

		if (Functions.contains(request.getHeaderNames(), "Request")
				&& !(state.equals(State.dhke))) {
			if (state.equals(State.initialized)) {

				if (request.getHeader("Request").equalsIgnoreCase("mod")) {

					out.print(mod);

					if (request.getHeader("Name").equals(USER1.name)) {
						System.out.println("User1 has received the modulus");
						USER1.rMod = true;
					} else if (request.getHeader("Name").equals(USER2.name)) {
						System.out.println("User2 has received the modulus");
						USER2.rMod = true;
					}
				} else if (request.getHeader("Request")
						.equalsIgnoreCase("base")) {
					out.print(base);

					if (request.getHeader("Name").equals(USER1.name)) {
						System.out.println("User1 has received the base");
						USER1.rBase = true;
					} else if (request.getHeader("Name").equals(USER2.name)) {
						System.out.println("User2 has received the base");
						USER2.rBase = true;
					}
				}
			} else {
				// Activated when a client requests the mod/base before the
				// other client has connected

				out.print("NOT_READY");
				System.out.print(".");
				return;
			}

			out.close();
			if (USER1.rBase && USER1.rMod && USER2.rBase && USER2.rMod) {
				System.out
						.println("Both clients have received base and modulus!");
				state = State.dhke;
			}
			return;
		}

		// When client tries to get the number
		if (Functions.contains(request.getHeaderNames(), "GetNumber")) {

			// When other User has not receieved base/mod OR if they haven't
			// sent in their values
			if (!state.equals(State.dhke) || USER1.currentValue == 0
					|| USER2.currentValue == 0) {
				out.print("VALUE_NOT_READY");
				return;
			}

			if (request.getHeader("Name").equalsIgnoreCase(USER1.name)) {
				System.out.println("SENT " + USER2.currentValue + " TO USER 1");
				out.println(USER2.currentValue);
				USER1.received = true;
			} else if (request.getHeader("Name").equalsIgnoreCase(USER2.name)) {
				System.out.println("SENT " + USER1.currentValue + " TO USER 2");
				out.println(USER1.currentValue);
				USER2.received = true;
			}
		} else {
			// Accessed by browser
			out.println("Take your stinking GET Requests off me, you damn dirty browser!");
			return;
		}

		if (USER1.received && USER2.received) {
			state = State.finished;
			clear();
		}
	}

	private void clear() {
		USER1 = new User(USER1.IP, USER1.name);
		USER2 = new User(USER2.IP, USER2.name);

		System.out.println("CLEARED");
		System.out.println("-----------------");

		generate();
		state = State.initialized;
	}

	// Generates Mod and Base

	private void generate() {

		mod = BigInteger.probablePrime(10, ran).intValue();
		base = rand.nextInt(100000);

		System.out.println("MOD = " + mod + " || BASE = " + base);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		// To tell Server that a client has connected
		if (Functions.has(request, "Init", "true") && state.equals(State.created)) {
			response.setContentType("text/String");
			if (USER1 == null || !USER1.initialized) {
				USER1 = new User(request.getRemoteAddr(),
						request.getParameter("Name"));

				System.out.println(USER1.name + " (USER 1) is at " + USER1.IP);
				out.println("Initialization Successful, " + USER1.name + " ("
						+ USER1.IP + ")");
				return;
			} else if (USER2 == null || !USER2.initialized) {
				USER2 = new User(request.getRemoteAddr(),
						request.getParameter("Name"));

				USER2.IP = request.getRemoteAddr();
				USER2.name = request.getParameter("Name");

				System.out.println("");
				System.out.println(USER2.name + " (USER 2) is at " + USER2.IP);
				out.println("Initialization Successful, " + USER2.name + " ("
						+ USER2.IP + ")");

				System.out.println("Both clients have connected");
				state = State.initialized;

				generate();
			}

			out.close();
			return;
		}

		if (state.equals(State.created)
				&& Functions.contains(request.getParameterNames(), "Request")) {
			out.print("NOT_READY");
			System.out.print(".");
			return;
		}

		if (Functions.contains(request.getParameterNames(), "Value")) {
			if (request.getParameter("Name").equalsIgnoreCase(USER1.name)) {
				USER1.currentValue = Integer.parseInt(request
						.getParameter("Value"));
				System.out.println("RECEIEVED " + USER1.currentValue
						+ " FROM USER 1");
			} else if (request.getParameter("Name")
					.equalsIgnoreCase(USER2.name)) {
				USER2.currentValue = Integer.parseInt(request
						.getParameter("Value"));
				System.out.println("RECEIEVED " + USER2.currentValue
						+ " FROM USER 2");
			}
			return;
		}
	}
}
