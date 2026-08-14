package org.prelle.telnet.event;

import org.prelle.telnet.parser.TelnetConstants.ControlCode;

public interface TelnetCommand extends TelnetEvent {

	//-------------------------------------------------------------------
	ControlCode getCode();

}