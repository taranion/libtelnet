/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * See http://tintin.sourceforge.net/msdp/
 * @see http://tintin.sourceforge.net/msdp/
 * @author prelle
 *
 */
public class MUDServerDataProtocol extends TelnetOptionHandler {

	private final static int	MSDP_VAR = 1;
	private final static int	MSDP_VAL = 2;
	private final static int	MSDP_TABLE_OPEN = 3;
	private final static int	MSDP_TABLE_CLOSE = 4;
	private final static int	MSDP_ARRAY_OPEN = 5;
	private final static int	MSDP_ARRAY_CLOSE = 6;

	static enum RequestState { DEFAULT, CLIENT_NAME, TERMINAL_TYPE, MUD_TERMINAL_TYPE, UNKNOWN}

	//-----------------------------------------------------------------
	public MUDServerDataProtocol() {
		super(69, "MSDP");
	}

//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
//	 */
//	@Override
//	public void initialize(TelnetSocket console) throws IOException {
//		requestUsage(console);
//
////		MUDTerminalTypeData data = new MUDTerminalTypeData();
////		data.setState(RequestState.DEFAULT);
////		console.setOptionState(this, data);
//	}
//
//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
//	 */
//	@Override
//	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
//			throws IOException {
//		logger.log(Level.DEBUG,"performSubNegotiation for "+getName());
////		int sendOrIs = in.read();
////
////		switch (sendOrIs) {
////		case IS:
////			// Remote side sends data - read until IAC
////
////			StringBuffer buf = new StringBuffer();
////			while (true) {
////				int dat = in.read();
////				if (dat==IAC) {
////					logger.trace("SN: IAC");
////					break;
////				}
////
////				logger.trace("SN: "+dat+"  ("+ (char)dat + ")");
////				buf.append( (char)dat );
////			}
////			int dat = in.read(); //SE
////			if (dat!=SE) {
////				logger.warn("Expected subnegotiation end, but found "+dat);
////			}
////			logger.trace("SN: IAC");
////
////			MUDTerminalTypeData data = (MUDTerminalTypeData) nvt.getOptionState(this);
////			switch (data.getState()) {
////			case CLIENT_NAME:
////				// Answer to first TTYPE SEND is a client name
////				data.setClientName(buf.toString());
////				logger.log(Level.DEBUG,"MUD-Client: "+buf.toString());
////				data.setState(RequestState.TERMINAL_TYPE);
////				requestNext(nvt);
////				return;
////			case TERMINAL_TYPE:
////				// Answer to second TTYPE SEND is a generic terminal type
////				data.setTerminalType(buf.toString());
////				logger.log(Level.DEBUG,"Terminal-Type: "+buf.toString());
////				data.setState(RequestState.MUD_TERMINAL_TYPE);
////				requestNext(nvt);
////				return;
////			case MUD_TERMINAL_TYPE:
////				// Answer to third TTYPE SEND is a specific MUD terminal type
////				data.setMudTerminalType(buf.toString());
////				logger.log(Level.DEBUG,"MUD-Terminal: "+buf.toString());
////				data.setState(RequestState.UNKNOWN);
////				nvt.getListener().telnetOptionDataChanged(nvt, this, data);
////				return;
////			default:
////				logger.warn("Don't know what to do with this response: "+data.getState());
////			}
////		default:
////			logger.warn("Don't know what to do with sendOrIs="+sendOrIs);
////		}
//
//	}
//
//	//-----------------------------------------------------------------
//	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
//		logger.log(Level.INFO,getName()+" enabled");
//	}

}
