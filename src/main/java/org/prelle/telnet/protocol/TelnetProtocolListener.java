package org.prelle.telnet.protocol;

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.option.TelnetOption;

/**
 * 
 */
public interface TelnetProtocolListener {
	
	public void onTelnetEvent(TelnetEvent event);
	
	public void optionStateChanged(TelnetOption extension, boolean active);
	
	public void telnetReady();

}
