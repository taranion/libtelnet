package org.prelle.telnet.event.internal;

import org.prelle.telnet.WellKnownTelnetOptions;
import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.event.TelnetNegotiationEvent;
import org.prelle.telnet.parser.TelnetConstants.ControlCode;

/**
 * WILL / WONT / DO / DONT
 */
public class TelnetNegotiationEventImpl implements TelnetEvent, TelnetNegotiationEvent {
	
	private int option;
	private ControlCode type;

	//-------------------------------------------------------------------
	/**
	 */
	TelnetNegotiationEventImpl(ControlCode state, int option) {
		this.option = option;
		this.type = state;
	}

	//-------------------------------------------------------------------
	TelnetNegotiationEventImpl(TelnetNegotiationEvent request, ControlCode respondWith) {
		this.option = request.getOption();
		this.type = respondWith;
	}

	//-------------------------------------------------------------------
	public String toString() {
		WellKnownTelnetOptions opt = WellKnownTelnetOptions.valueOf(option);
		return type+"("+(opt!=null?opt.name():String.valueOf(option))+")";
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetNegotiationEvent#getOption()
	 */
	@Override
	public int getOption() {
		return option;
	}

	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetNegotiationEvent#getType()
	 */
	@Override
	public ControlCode getType() {
		return type;
	}

}
