/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOptionDeleteMe;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.mud.MUDTerminalTypeData;

/**
 * @author prelle
 *
 */
public class StatusOption extends TelnetOptionHandler {

	private final static int	IS   = 0;
	private final static int	SEND = 1;

	//-----------------------------------------------------------------
	public StatusOption() {
		super(5, "STATUS");
	}

//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
//	 */
//	@Override
//	public void initialize(TelnetSocket console) throws IOException {
//		requestUsage(console);
//	}
//
//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#optionEnabled(org.prelle.telnet.TelnetSocket, boolean)
//	 */
//	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
//		logger.log(Level.INFO,getName()+" enabled");
//		logger.log(Level.DEBUG,"Request status");
//		OutputStream out = nvt.getOutputStream();
//		byte[] send = new byte[6];
//		send[0] = (byte)IAC;
//		send[1] = (byte)SB;
//		send[2] = (byte)TelnetOption.STATUS.getCode();
//		send[3] = (byte)SEND;
//		send[4] = (byte)IAC;
//		send[5] = (byte)SE;
//		out.write(send);
//		out.flush();
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
//		int sendOrIs = in.read();
//		logger.log(Level.TRACE,"Got "+sendOrIs);
//
//		Map<Integer, Boolean> ret = new HashMap<>();
//		switch (sendOrIs) {
//		case IS:
//			// Remote side sends data - read until IAC
//			logger.log(Level.TRACE,"IS");
//
//			StringBuffer buf = new StringBuffer();
//			while (true) {
//				int dat = in.read();
//				ControlCode code = ControlCode.getCodeFor(dat);
//				if (code==null) {
//					logger.log(Level.TRACE,"RCV: Unknown code {0}",dat);
//				} else {
//					logger.log(Level.TRACE,"RCV: {0}", code);
//				}
//				if (dat==IAC) {
//					// End of list
//					break;
//				}
//
//				int optCode = in.read();
//				TelnetOption option = TelnetOption.valueOf(optCode);
//				if (option==null) {
//					logger.log(Level.INFO,"{0} unknown option {1}", code, optCode);
//				} else {
//					logger.log(Level.INFO,"{0} {1}", code, option);
//				}
//
//				ret.put(optCode, dat==WILL);
//			}
//			int dat = in.read(); //SE
//			if (dat!=SE) {
//				logger.log(Level.WARNING,"Expected subnegotiation end, but found "+dat);
//			}
//
//			logger.log(Level.INFO, "Remote status: "+ret);
//			return;
//
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
////				logger.log(Level.INFO,"MUD-Terminal: "+buf.toString());
////				data.setState(RequestState.UNKNOWN);
////				nvt.setOptionState(this, data);
////				nvt.fireOptionDataChanged(this, data);
////				return;
////			default:
////				logger.log(Level.WARNING,"Don't know what to do with this response: "+data.getState());
////			}
//		default:
//			logger.log(Level.WARNING,"Don't know what to do with sendOrIs="+sendOrIs);
//		}
//
//	}

}
