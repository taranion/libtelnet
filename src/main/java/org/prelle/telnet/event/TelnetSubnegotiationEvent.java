package org.prelle.telnet.event;

import org.prelle.telnet.WellKnownTelnetOptions;

/**
 * 
 */
public class TelnetSubnegotiationEvent implements TelnetEvent {

	private int option;
	private byte[] data;
	
	//-------------------------------------------------------------------
	public TelnetSubnegotiationEvent(int option, byte[] data) {
		this.option = option;
		this.data = data;
	}
	
	//-------------------------------------------------------------------
	public TelnetSubnegotiationEvent(int option, byte  data) {
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
	 * @return the option
	 */
	public int getOption() {
		return option;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	public int[] getAsIntArray() {
		int[] result = new int[data.length];
		for (int i=0; i<data.length; i++) {
			result[i] = data[i] & 0xFF;
		}
		return result;
	}
}
