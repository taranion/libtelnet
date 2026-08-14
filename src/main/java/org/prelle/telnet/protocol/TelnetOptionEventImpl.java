package org.prelle.telnet.protocol;

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.option.TelnetOption;

/**
 * 
 */
public abstract class TelnetOptionEventImpl implements TelnetOptionEvent, TelnetEvent {
	
	protected transient TelnetOption option;
	
	//-------------------------------------------------------------------
	protected TelnetOptionEventImpl() {
	}
	
	//-------------------------------------------------------------------
	protected TelnetOptionEventImpl(TelnetOption option) {
		this.option = option;
	}
	
	//-------------------------------------------------------------------
	@Override
	public TelnetOption getOption() { return option; }
	public void setOption(TelnetOption option) { this.option = option; }

}
