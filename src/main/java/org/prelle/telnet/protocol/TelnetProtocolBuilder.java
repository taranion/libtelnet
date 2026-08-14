package org.prelle.telnet.protocol;

import org.prelle.telnet.event.TelnetEventFactory;
import org.prelle.telnet.event.internal.DefaultTelnetEventFactory;
import org.prelle.telnet.option.CommunicationRole;

/**
 * 
 */
public class TelnetProtocolBuilder {
	
	private CommunicationRole role;
	private TelnetEventFactory factory = new DefaultTelnetEventFactory();
	private TelnetProtocolListener listener;

	//-------------------------------------------------------------------
	/**
	 */
	public TelnetProtocolBuilder(CommunicationRole role) {
		this.role = role;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withEventFactory(TelnetEventFactory factory) {
		this.factory = factory;
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withListener(TelnetProtocolListener listener) {
		this.listener = listener;
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocol build() {
		return new TelnetProtocol(role, factory, listener);
	}

}
