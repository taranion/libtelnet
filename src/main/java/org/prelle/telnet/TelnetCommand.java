package org.prelle.telnet;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 *
 */
public class TelnetCommand {

	private ControlCode code;
	private int data;

	//-------------------------------------------------------------------
	public TelnetCommand(ControlCode code) {
		this.code = code;
	}

	//-------------------------------------------------------------------
	public TelnetCommand(ControlCode code, int value) {
		this.code = code;
		this.data = value;
	}

	//-------------------------------------------------------------------
	public String toString() {
		switch (code) {
		case WILL: case WONT:
		case DO  : case DONT:
			return code.name()+"("+data+")";
		default:
			return code.name();
		}
	}

	//-------------------------------------------------------------------
	public ControlCode getCode() { return code; }
	public int getData() { return data; }


}
