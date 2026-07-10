package org.prelle.telnet;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class TelnetConfiguration {
	
	private List<TelnetOption> extensions = new ArrayList<>();

	//-------------------------------------------------------------------
	public void add(TelnetOption extension) {
		if (!extensions.contains(extension)) {
			extensions.add(extension);
		}
	}

	void processCommand(TelnetInputStream from, TelnetCommand command) {
		
	}
	
}
