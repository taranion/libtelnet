package org.prelle.telnet.protocol;

import org.prelle.telnet.option.TelnetOption;

public class SubnegotiationFinishedEvent extends TelnetOptionEventImpl {
	public SubnegotiationFinishedEvent(TelnetOption option) {
		super(option);
	}
}