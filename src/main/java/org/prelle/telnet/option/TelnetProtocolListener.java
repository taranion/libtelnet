package org.prelle.telnet.option;

import org.prelle.telnet.event.TelnetEvent;

/**
 * 
 */
public interface TelnetProtocolListener {
	
	public void onTelnetEvent(TelnetEvent event);
	
	public void optionStateChanged(TelnetOption extension, boolean active);
	
	public void telnetReady();

}
