package org.prelle.telnet;

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.option.TelnetOption;

/**
 * 
 */
public interface TelnetSocketListener {
	
	public void onTelnetEvent(TelnetEvent event);
	
	public void optionStateChanged(TelnetOption extension, boolean active);
	
	public void telnetReady();

}
