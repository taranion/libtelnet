/**
 * 
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger.Level;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.WillVariable;
import org.prelle.telnet.option.TelnetOption;

/**
 * See http://tintin.sourceforge.net/mtts/
 * @see http://tintin.sourceforge.net/mtts/
 * @author prelle
 *
 */
public class MUDTerminalTypeStandard extends TelnetOption {

	public final static int    CODE = 24;  // oder 70??
	private final static int	IS   = 0;
	private final static int	SEND = 1;
	
	private final static String NAME = "MTTS";

	static enum RequestState { DEFAULT, CLIENT_NAME, TERMINAL_TYPE, MUD_TERMINAL_TYPE, UNKNOWN} 
	
	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#setDefaults(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void setDefaults(TelnetSocket nvt) {
		nvt.setOptionVariable(new WillVariable(CODE, false));
		nvt.setOptionVariable(new DoVariable(CODE, false));
	}
	
	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getCode()
	 */
	@Override
	public int getCode() {
		return CODE;
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
		
		MUDTerminalTypeData data = new MUDTerminalTypeData();
		data.setState(RequestState.DEFAULT);
		console.setOptionState(this, data);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
			throws IOException {
		logger.log(Level.DEBUG,"performSubNegotiation for "+NAME);
		int sendOrIs = in.read();
		
		switch (sendOrIs) {
		case IS:
			// Remote side sends data - read until IAC

			StringBuffer buf = new StringBuffer();
			while (true) {
				int dat = in.read();
				if (dat==IAC) {
					logger.log(Level.TRACE,"SN: IAC");
					break;
				}
				
				logger.log(Level.TRACE,"SN: "+dat+"  ("+ (char)dat + ")");
				buf.append( (char)dat );
			}
			int dat = in.read(); //SE
			if (dat!=SE) {
				logger.log(Level.WARNING,"Expected subnegotiation end, but found "+dat);
			}
			logger.log(Level.TRACE,"SN: IAC");
			
			MUDTerminalTypeData data = (MUDTerminalTypeData) nvt.getOptionState(this);
			switch (data.getState()) {
			case CLIENT_NAME:
				// Answer to first TTYPE SEND is a client name
				data.setClientName(buf.toString());
				logger.log(Level.DEBUG,"MUD-Client: "+buf.toString());
				data.setState(RequestState.TERMINAL_TYPE);
				requestNext(nvt);
				return;
			case TERMINAL_TYPE:
				// Answer to second TTYPE SEND is a generic terminal type
				data.setTerminalType(buf.toString());
				logger.log(Level.DEBUG,"Terminal-Type: "+buf.toString());
				data.setState(RequestState.MUD_TERMINAL_TYPE);
				requestNext(nvt);
				return;
			case MUD_TERMINAL_TYPE:
				// Answer to third TTYPE SEND is a specific MUD terminal type
				data.setMudTerminalType(buf.toString());
				logger.log(Level.DEBUG,"MUD-Terminal: "+buf.toString());
				data.setState(RequestState.UNKNOWN);
				nvt.fireOptionDataChanged(this, data);
				return;
			default:
				logger.log(Level.WARNING,"Don't know what to do with this response: "+data.getState());
			}
		default:
			logger.log(Level.WARNING,"Don't know what to do with sendOrIs="+sendOrIs);
		}
		
	}

	//-----------------------------------------------------------------
	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
		logger.log(Level.INFO,getName()+" enabled");
		logger.log(Level.DEBUG,"Request MUD terminal type");
		((MUDTerminalTypeData)nvt.getOptionState(this)).setState(RequestState.CLIENT_NAME);
		requestNext(nvt);
	}

	//-----------------------------------------------------------------
	public static void requestNext(TelnetSocket nvt) throws IOException {
		OutputStream out = nvt.getOutputStream();
		byte[] send = new byte[6];
		send[0] = (byte)IAC; 
		send[1] = (byte)SB; 
		send[2] = (byte)CODE;
		send[3] = (byte)SEND;
		send[4] = (byte)IAC;
		send[5] = (byte)SE;
		out.write(send);
		out.flush();		
	}

}
