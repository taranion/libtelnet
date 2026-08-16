package org.prelle.telnet.event.internal;

import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.protocol.OptionStateEvent;
import org.prelle.telnet.protocol.TelnetOptionEventImpl;

/**
 * 
 */
public class OptionStateEventImpl extends TelnetOptionEventImpl implements OptionStateEvent {
	
	private boolean active;

	//-------------------------------------------------------------------
	/**
	 * @param option
	 */
	public OptionStateEventImpl(TelnetOption option, boolean active) {
		super(option);
		this.active = active;
	}
	
	//-------------------------------------------------------------------
	public boolean isActive() {
		return active;
	}

}
