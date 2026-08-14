package org.prelle.telnet.event.internal;

import org.prelle.telnet.event.DataEvent;
import org.prelle.telnet.event.TelnetEvent;

/**
 * 
 */
public class DataEventImpl implements TelnetEvent, DataEvent {
	
	private byte[] data;

	//-------------------------------------------------------------------
	DataEventImpl(byte[] data) {
		this.data = data;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.DataEvent#getData()
	 */
	@Override
	public byte[] getData() { return data; }
	
	public String toString() {
		return "DATA: " + data.length + " bytes";
	}

}
