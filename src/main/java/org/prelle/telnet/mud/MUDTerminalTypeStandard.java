/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOptions;
import org.prelle.telnet.TelnetSocket;

/**
 * See http://tintin.sourceforge.net/mtts/
 * @see http://tintin.sourceforge.net/mtts/
 * @author prelle
 *
 *
 */
public class MUDTerminalTypeStandard extends TelnetOptionHandler {

	private final static int	IS   = 0;
	private final static int	SEND = 1;

	static enum RequestState { DEFAULT, CLIENT_NAME, TERMINAL_TYPE, MUD_TERMINAL_TYPE, UNKNOWN}

	//-----------------------------------------------------------------
	public MUDTerminalTypeStandard(int code, String name) {
		super(code,name);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	public void requestUsage(TelnetSocket nvt) throws IOException {
		MUDTerminalTypeData data = new MUDTerminalTypeData();
		data.setState(RequestState.DEFAULT);
		nvt.setOptionState(this, data);

		super.requestUsage(nvt);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
			throws IOException {
		logger.log(Level.DEBUG,"performSubNegotiation for "+getName());
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
				logger.log(Level.INFO,"MUD-Terminal: "+buf.toString());
				data.setState(RequestState.UNKNOWN);
				nvt.setOptionState(this, data);
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
	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) {
		logger.log(Level.INFO,getName()+" enabled");
		logger.log(Level.DEBUG,"Request MUD terminal type");
		((MUDTerminalTypeData)nvt.getOptionState(this)).setState(RequestState.CLIENT_NAME);
		try {
			requestNext(nvt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-----------------------------------------------------------------
	public static void requestNext(TelnetSocket nvt) throws IOException {
		OutputStream out = nvt.getOutputStream();
		byte[] send = new byte[6];
		send[0] = (byte)IAC;
		send[1] = (byte)SB;
		send[2] = (byte)TelnetOptions.MTT.getCode();
		send[3] = (byte)SEND;
		send[4] = (byte)IAC;
		send[5] = (byte)SE;
		out.write(send);
		out.flush();
	}

}
