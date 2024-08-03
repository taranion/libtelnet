package org.prelle.telnet;

import java.util.HashMap;
import java.util.Map;

import org.prelle.telnet.option.TransmitBinary;

/**
 *
 */
public class TelnetOptionRegistry {

	private static Map<Integer, TelnetOptionHandler> knownOptions = new HashMap<>();

	//-------------------------------------------------------------------
	static {
		register(new TelnetOptionHandler(0, "TRANSMIT_BINARY"));
		register(new TransmitBinary());
	}

	//-------------------------------------------------------------------
	public static void register(TelnetOptionHandler handler) {
		knownOptions.put(handler.getCode(), handler);
	}

}
