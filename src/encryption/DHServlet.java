package encryption;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;

import main.Chatroom;
import main.Functions;

//NOT GOING TO BE AN ACTUAL SERVLET
public class DHServlet {

	// The bitlength of the modulus
	private static final int MOD_LENGTH = 31;

	public DHUser USER1;
	public DHUser USER2;

	public int amount;
	public int times = 0;

	public static int mod;
	public static int base;

	private static Random ran = new SecureRandom();

	private Chatroom parent;

	public DHServlet(int x, Chatroom c) {
		amount = x;
		parent = c;

		// Gets the names of the users
		// WARNING: THIS WILL BREAK IF MULTI-USER CHATROOMS ARE IMPLEMENTED
		USER1 = new DHUser(parent.userList.get(0).name);
		USER2 = new DHUser(parent.userList.get(1).name);

		System.out.println();
		System.out.println("_______________ DHKE _______________");

		System.out.println("DHUSER1: " + USER1.name);
		System.out.println("DHUSER2: " + USER2.name);
		generate();
	}

	public String processGet(HttpServletRequest request) {
		String response = "";
		
		// Request for Mod/Base
		if (Functions.contains(request.getHeaderNames(), "REQUEST")) {

			if (request.getHeader("REQUEST").equalsIgnoreCase("MOD")) {
				response = String.valueOf(mod);

				if (request.getHeader("NAME").equals(USER1.name)) {
					System.out.println("User1 has received the modulus");
					USER1.rMod = true;
				} else if (request.getHeader("NAME").equals(USER2.name)) {
					System.out.println("User2 has received the modulus");
					USER2.rMod = true;
				}
			} else if (request.getHeader("REQUEST").equalsIgnoreCase("BASE")) {
				response = String.valueOf(base);

				if (request.getHeader("NAME").equals(USER1.name)) {
					System.out.println("User1 has received the base");
					USER1.rBase = true;
				} else if (request.getHeader("NAME").equals(USER2.name)) {
					System.out.println("User2 has received the base");
					USER2.rBase = true;
				}
			}

			if (USER1.rBase && USER1.rMod && USER2.rBase && USER2.rMod) {
				System.out
						.println("Both clients have received base and modulus!");
			}

			// Request for other client's number
		} else if (Functions.contains(request.getHeaderNames(), "GETVALUE")) {
			// When the value is not ready
			if (USER1.currentValue == 0
					|| USER2.currentValue == 0) {
				response = "VALUE_NOT_READY";
			} else {
				if (request.getHeader("NAME").equalsIgnoreCase(USER1.name)) {
					System.out.println("SENT " + USER2.name + "'s NUMBER ("
							+ USER2.currentValue + ") TO " + USER1.name);
					response = String.valueOf(USER2.currentValue);
					USER1.received = true;
				} else if (request.getHeader("NAME").equalsIgnoreCase(
						USER2.name)) {
					System.out.println("SENT " + USER1.name + "'s NUMBER ("
							+ USER1.currentValue + ") TO " + USER2.name);
					response = String.valueOf(USER1.currentValue);
					USER2.received = true;
				}

				if (USER1.received && USER2.received) {
					System.out.println("FINISHED!");
					clear();
				}
			}
		}
		return response;
	}

	// Only to be used to POST their own integer
	public String processPost(HttpServletRequest request) {
		String response = "";
		if (Functions.contains(request.getParameterNames(), "POSTVALUE")) {
			if (request.getParameter("NAME").equalsIgnoreCase(USER1.name)) {
				USER1.currentValue = Integer.parseInt(request
						.getParameter("POSTVALUE"));
				System.out.println("RECEIEVED " + USER1.currentValue + " FROM "
						+ USER1.name);
				response = "Success";
			} else if (request.getParameter("NAME")
					.equalsIgnoreCase(USER2.name)) {
				USER2.currentValue = Integer.parseInt(request
						.getParameter("POSTVALUE"));
				System.out.println("RECEIEVED " + USER2.currentValue + " FROM "
						+ USER2.name);
				response = "Success";
			}
		}
		return response;
	}

	private void clear() {
		times++;
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (times < amount) {
			USER1.clear();
			USER2.clear();

			System.out.println("CLEARED");
			System.out.println("-----------------");
			
			generate();
		} else {
			//Closes the servlet
			parent.done();
		}
	}

	// Generates Mod and Base

	private void generate() {
		mod = BigInteger.probablePrime(MOD_LENGTH, ran).intValue();
		// Any value that's smaller than the mod
		base = ran.nextInt(mod - 1);

		// Rejects the base if it is equal to the mod or if it is a factor of
		// the modulus - SHOULDN'T HAPPEN
		while (base == mod || mod % base == 0) {
			base = ran.nextInt(mod - 1);
		}

		System.out.println("GENERATED: MOD = " + mod + " || BASE = " + base);
	}

}
