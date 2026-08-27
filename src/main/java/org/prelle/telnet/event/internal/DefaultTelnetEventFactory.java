package org.prelle.telnet.event.internal;

import org.prelle.telnet.event.DataEvent;
import org.prelle.telnet.event.TelnetCommand;
import org.prelle.telnet.event.TelnetEventFactory;
import org.prelle.telnet.event.TelnetNegotiationEvent;
import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.parser.TelnetConstants.ControlCode;
import org.prelle.telnet.protocol.OptionStateEvent;
import org.prelle.telnet.protocol.OptionSupportEvent;

/**
 * 
 */
public class DefaultTelnetEventFactory implements TelnetEventFactory {

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetEventFactory#createDataEvent(byte[])
	 */
	@Override
	public DataEvent createDataEvent(byte[] data) {
		return new DataEventImpl(data);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetEventFactory#createTelnetCommand(org.prelle.telnet.parser.TelnetConstants.ControlCode)
	 */
	@Override
	public TelnetCommand createTelnetCommand(ControlCode code) {
		return new TelnetCommandImpl(code);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetEventFactory#createTelnetNegotiationEvent(org.prelle.telnet.parser.TelnetConstants.ControlCode, int)
	 */
	@Override
	public TelnetNegotiationEvent createTelnetNegotiationEvent(ControlCode code, int option) {
		return new TelnetNegotiationEventImpl(code, option);
	}

	@Override
	public TelnetNegotiationEvent createTelnetNegotiationEvent(TelnetNegotiationEvent request, ControlCode answer) {
		return new TelnetNegotiationEventImpl(request, answer);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetEventFactory#createTelnetSubnegotiationEvent(int, byte[])
	 */
	@Override
	public TelnetSubnegotiationEvent createTelnetSubnegotiationEvent(int option, byte[] data) {
		return new TelnetSubnegotiationEventImpl(option, data);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetEventFactory#createOptionStateEvent(org.prelle.telnet.option.TelnetOption, boolean)
	 */
	@Override
	public OptionStateEvent createOptionStateEvent(TelnetOption option, boolean enabled) {
		return new OptionStateEventImpl(option, enabled);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetEventFactory#createOptionSupportEvent(org.prelle.telnet.option.TelnetOption, boolean)
	 */
	@Override
	public OptionSupportEvent createOptionSupportEvent(TelnetOption option, boolean supported) {
		return new OptionSupportEventImpl(option, supported);
	}

}
