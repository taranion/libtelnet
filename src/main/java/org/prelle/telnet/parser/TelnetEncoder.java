package org.prelle.telnet.parser;

import java.nio.charset.StandardCharsets;

import org.prelle.telnet.event.DataEvent;
import org.prelle.telnet.event.TelnetCommand;
import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.event.TelnetNegotiationEvent;
import org.prelle.telnet.event.TelnetSubnegotiationEvent;

/**
 * 
 */
public class TelnetEncoder implements TelnetConstants {
	
	//-------------------------------------------------------------------
	private static byte[] encodeWill(int option) {
		byte[] send = new byte[3];
		send[0] = (byte)IAC;
		send[1] = (byte)WILL;
		send[2] = (byte)option;
		return send;
	}
	
	//-------------------------------------------------------------------
	private static byte[] encodeWont(int option) {
		byte[] send = new byte[3];
		send[0] = (byte)IAC;
		send[1] = (byte)WONT;
		send[2] = (byte)option;
		return send;
	}
	
	//-------------------------------------------------------------------
	private static byte[] encodeDo(int option) {
		byte[] send = new byte[3];
		send[0] = (byte)IAC;
		send[1] = (byte)DO;
		send[2] = (byte)option;
		return send;
	}
	
	//-------------------------------------------------------------------
	private static byte[] encodeDont(int option) {
		byte[] send = new byte[3];
		send[0] = (byte)IAC;
		send[1] = (byte)DONT;
		send[2] = (byte)option;
		return send;
	}
	
	//-----------------------------------------------------------------
	private static byte[] encodeSubNegotiation(int code, byte[] value) {
		byte[] data = new byte[5+value.length];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.SB.code();
		data[2] = (byte)code;
		System.arraycopy(value, 0, data, 3, value.length);
		data[data.length-2] = (byte)ControlCode.IAC.code();
		data[data.length-1] = (byte)ControlCode.SE.code();
		return data;
	}

	//-------------------------------------------------------------------
	private static byte[] encodeSubNegotiation(int code, String line) {
		byte[] value = line.getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[5+value.length];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.SB.code();
		data[2] = (byte)code;
		System.arraycopy(value, 0, data, 3, value.length);
		data[data.length-2] = (byte)ControlCode.IAC.code();
		data[data.length-1] = (byte)ControlCode.SE.code();
		return data;
	}

	//-------------------------------------------------------------------
	private static byte[] encodeSubNegotiation(int code, int command, byte[] value)  {
		byte[] data = new byte[6+value.length];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.SB.code();
		data[2] = (byte)code;
		data[3] = (byte)command;
		System.arraycopy(value, 0, data, 4, value.length);
		data[data.length-2] = (byte)ControlCode.IAC.code();
		data[data.length-1] = (byte)ControlCode.SE.code();
		return data;
	}

	//-------------------------------------------------------------------
	public static byte[] encodeEvent(TelnetEvent event) {
		return switch (event) {
		case DataEvent dataEvent -> dataEvent.getData();
		case TelnetNegotiationEvent negEv -> {
			yield switch (negEv.getType()) {
			case WILL -> encodeWill(negEv.getOption());
			case WONT -> encodeWont(negEv.getOption());
			case DO -> encodeDo(negEv.getOption());
			case DONT -> encodeDont(negEv.getOption());
			default -> throw new IllegalArgumentException("Unsupported negotiation type: " + negEv.getType());
			};
		}
		case TelnetSubnegotiationEvent subEv -> encodeSubNegotiation(subEv.getOption(), subEv.getData());
		case TelnetCommand cmdEv -> new byte[] { (byte)IAC, (byte)cmdEv.getCode().code() };
		default -> throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
		};
	}

}
