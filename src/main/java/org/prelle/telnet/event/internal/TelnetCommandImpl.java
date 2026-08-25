package org.prelle.telnet.event.internal;

import org.prelle.telnet.event.TelnetCommand;
import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.parser.TelnetConstants.ControlCode;

/**
 *
 */
public class TelnetCommandImpl implements TelnetEvent, TelnetCommand {

	private ControlCode code;

	//-------------------------------------------------------------------
	TelnetCommandImpl(ControlCode code) {
		this.code = code;
	}


	//-------------------------------------------------------------------
	public String toString() {
		if (code == null) {
			return "TelnetCommand:NULL";
		}
		return code.name();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetCommand#getCode()
	 */
	@Override
	public ControlCode getCode() { return code; }

}
