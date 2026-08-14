package org.prelle.telnet.event.internal;

import org.prelle.telnet.WellKnownTelnetOptions;
import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.event.TelnetSubnegotiationEvent;

/**
 * 
 */
public class TelnetSubnegotiationEventImpl implements TelnetEvent, TelnetSubnegotiationEvent {

	private int option;
	private byte[] data;
	
	//-------------------------------------------------------------------
	TelnetSubnegotiationEventImpl(int option, byte[] data) {
		this.option = option;
		this.data = data;
	}
	
	//-------------------------------------------------------------------
	TelnetSubnegotiationEventImpl(int option, byte  data) {
		this.option = option;
		this.data = new byte[] { data };
	}
	
	//-------------------------------------------------------------------
	public String toString() {
		WellKnownTelnetOptions opt = WellKnownTelnetOptions.valueOf(option);
		String name = (opt!=null)?opt.name():String.valueOf(option);
		return String.format("SubNeg(%8s): %s", name, new String(data));
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetSubnegotiationEvent#getOption()
	 */
	@Override
	public int getOption() {
		return option;
	}

	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetSubnegotiationEvent#getData()
	 */
	@Override
	public byte[] getData() {
		return data;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetSubnegotiationEvent#getAsIntArray()
	 */
	@Override
	public int[] getAsIntArray() {
		int[] result = new int[data.length];
		for (int i=0; i<data.length; i++) {
			result[i] = data[i] & 0xFF;
		}
		return result;
	}
}
