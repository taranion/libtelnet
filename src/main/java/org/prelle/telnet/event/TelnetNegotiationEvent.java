package org.prelle.telnet.event;

import org.prelle.telnet.parser.TelnetConstants.ControlCode;

public interface TelnetNegotiationEvent extends TelnetEvent {

	//-------------------------------------------------------------------
	int getOption();

	//-------------------------------------------------------------------
	ControlCode getType();

}