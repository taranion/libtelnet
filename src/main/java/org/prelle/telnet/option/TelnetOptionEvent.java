package org.prelle.telnet.option;

import org.prelle.telnet.event.TelnetEvent;

/**
 * 
 */
public abstract class TelnetOptionEvent implements TelnetEvent {
	
	public static class SubnegotiationFinishedEvent extends TelnetOptionEvent {
		public SubnegotiationFinishedEvent(TelnetOption option) {
			super(option);
		}
	}
	
	
	protected TelnetOption option;
	
	protected TelnetOptionEvent(TelnetOption option) {
		this.option = option;
	}
	
	public TelnetOption getOption() { return option; }

}
