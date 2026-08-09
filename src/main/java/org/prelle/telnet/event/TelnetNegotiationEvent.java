package org.prelle.telnet.event;

import org.prelle.telnet.WellKnownTelnetOptions;
import org.prelle.telnet.parser.TelnetConstants.ControlCode;

/**
 * WILL / WONT / DO / DONT
 */
public class TelnetNegotiationEvent implements TelnetEvent {
	
	private int option;
	private ControlCode type;

	//-------------------------------------------------------------------
	/**
	 */
	public TelnetNegotiationEvent(ControlCode state, int option) {
		this.option = option;
		this.type = state;
	}

	//-------------------------------------------------------------------
	public TelnetNegotiationEvent(TelnetNegotiationEvent request, ControlCode respondWith) {
		this.option = request.getOption();
		this.type = respondWith;
	}

	//-------------------------------------------------------------------
	public String toString() {
		WellKnownTelnetOptions opt = WellKnownTelnetOptions.valueOf(option);
		return type+"("+(opt!=null?opt.name():String.valueOf(option))+")";
	}

	//-------------------------------------------------------------------
	public int getOption() {
		return option;
	}

	//-------------------------------------------------------------------
	public ControlCode getType() {
		return type;
	}

}
