package org.prelle.telnet.event;

/**
 * 
 */
public interface TelnetParserListener {
	
	//-------------------------------------------------------------------
	/**
	 * Called when some telnet command has been parsed.
	 * @param event
	 */
	public void onTelnetEvent(TelnetEvent event);

}
