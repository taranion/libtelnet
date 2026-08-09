package org.prelle.telnet.event;

/**
 * 
 */
public class DataEvent implements TelnetEvent {
	
	private byte[] data;

	//-------------------------------------------------------------------
	public DataEvent(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() { return data; }
	
	public String toString() {
		return "DATA: " + data.length + " bytes";
	}

}
