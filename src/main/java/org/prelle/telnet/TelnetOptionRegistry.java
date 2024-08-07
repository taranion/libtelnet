package org.prelle.telnet;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TelnetOptionRegistry {

	private static Map<Integer, TelnetSubnegotiationHandler> knownOptions = new HashMap<>();

	//-------------------------------------------------------------------
	static {
		for (TelnetOption entry : TelnetOption.values())
		register(entry.getCode(), entry.getOptionHandler());
	}

	//-------------------------------------------------------------------
	public static void register(int code, TelnetSubnegotiationHandler handler) {
		knownOptions.put(code, handler);
	}

	//-------------------------------------------------------------------
	public static TelnetSubnegotiationHandler get(int code) {
		return knownOptions.get(code);
	}

}
