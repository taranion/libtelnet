package org.prelle.telnet.protocol;

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.option.TelnetOption;

/**
 * 
 */
// TODO: Does this really need to extend TelnetParserEvent?
public interface TelnetOptionEvent extends TelnetEvent {
	
	//-------------------------------------------------------------------
	public TelnetOption getOption();
	public void setOption(TelnetOption option);

}
