package main;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpRequest;

public class Functions {
	
	//To deal with Params AND Headers
	public static boolean has(HttpServletRequest request, String name,
			String value) {
		if (contains(request.getParameterNames(), name)
				&& request.getParameter(name).equalsIgnoreCase(value)) {
			return true;
		} else if (contains(request.getHeaderNames(), name) && request.getHeader(name).equalsIgnoreCase(value)){
			return true;
		}
		return false;
	}

	public static boolean contains(Enumeration<String> enumeration,
			String value) {

		while (enumeration.hasMoreElements()) {
			if (enumeration.nextElement().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isClient(HttpServletRequest req){
		if (has(req, MainServlet.CLIENT_ID, "true")){
			return true;
		} else {
			return false;
		}
	}
}
