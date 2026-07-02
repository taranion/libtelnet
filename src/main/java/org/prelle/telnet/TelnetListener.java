package org.prelle.telnet;

/**
 * 
 */
public interface TelnetListener {
	
	public void optionStateChanged(TelnetSubnegotiationHandler extension, boolean active);

	public void telnetCommandReceived(TelnetCommand command);
}
