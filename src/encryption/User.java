package encryption;

import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

public class User {

	public String IP = "";
	//public LinkedHashMap<String, String> ParamMap;
	public String name;
	public int currentValue = 0;
	
	//If currentValue has been received by this client
	public boolean received = false;
	
	//Received base/mod
	public boolean rBase = false;
	public boolean rMod = false;
	public boolean initialized = false;
	
	public User(String IP, String n){
		initialized = true;
		this.IP = IP;
		name = n;
	}
	
	/*
	public void updateParams(HttpServletRequest req){
		ParamMap = new LinkedHashMap<String, String>();
		
		Enumeration<String> parameterNames = req.getParameterNames();
		while(parameterNames.hasMoreElements()){
            String paramName = parameterNames.nextElement();
            ParamMap.put(paramName, req.getParameterValues(paramName)[0]);
		}
	}
	*/
	
}
