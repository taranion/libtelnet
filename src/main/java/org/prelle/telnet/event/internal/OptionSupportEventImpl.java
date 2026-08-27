package org.prelle.telnet.event.internal;

import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.protocol.OptionSupportEvent;
import org.prelle.telnet.protocol.TelnetOptionEventImpl;

/**
 * 
 */
public class OptionSupportEventImpl extends TelnetOptionEventImpl implements OptionSupportEvent {
	
	private boolean supported;

	//-------------------------------------------------------------------
	/**
	 * @param option
	 */
	public OptionSupportEventImpl(TelnetOption option, boolean active) {
		super(option);
		this.supported = active;
	}
	
	//-------------------------------------------------------------------
	@Override
	public boolean isSupported() {
		return supported;
	}

}
