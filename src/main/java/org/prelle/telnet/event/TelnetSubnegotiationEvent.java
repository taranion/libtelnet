package org.prelle.telnet.event;

public interface TelnetSubnegotiationEvent extends TelnetEvent {

	//-------------------------------------------------------------------
	/**
	 * @return the option
	 */
	int getOption();

	//-------------------------------------------------------------------
	/**
	 * @return the data
	 */
	byte[] getData();

	int[] getAsIntArray();

}