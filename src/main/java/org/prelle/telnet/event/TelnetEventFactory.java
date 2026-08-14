package org.prelle.telnet.event;

import org.prelle.telnet.parser.TelnetConstants.ControlCode;

/**
 * 
 */
public interface TelnetEventFactory {

	public DataEvent createDataEvent(byte[] data);
	
	public TelnetCommand createTelnetCommand(ControlCode code);
	
	public TelnetNegotiationEvent createTelnetNegotiationEvent(ControlCode code, int option);
	public TelnetNegotiationEvent createTelnetNegotiationEvent(TelnetNegotiationEvent request, ControlCode answer);
	
	public TelnetSubnegotiationEvent createTelnetSubnegotiationEvent(int option, byte[] data);
	
}
