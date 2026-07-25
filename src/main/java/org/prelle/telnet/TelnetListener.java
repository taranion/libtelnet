package org.prelle.telnet;

/**
 * 
 */
public interface TelnetListener {
	
	public void optionStateChanged(TelnetOption extension, boolean active);
	
	public void telnetReady();

	public void telnetCommandReceived(TelnetCommand command);
}
