package org.prelle.telnet.event;

import org.prelle.telnet.parser.TelnetConstants.ControlCode;

/**
 *
 */
public class TelnetCommand implements TelnetEvent {

	private ControlCode code;

	//-------------------------------------------------------------------
	public TelnetCommand(ControlCode code) {
		this.code = code;
	}


	//-------------------------------------------------------------------
	public String toString() {
		return code.name();
	}

	//-------------------------------------------------------------------
	public ControlCode getCode() { return code; }

}
