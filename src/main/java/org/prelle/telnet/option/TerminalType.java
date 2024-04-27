/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 1091
 * @see http://tools.ietf.org/html/rfc1091
 * @author prelle
 *
 */
public class TerminalType extends TelnetOptionHandler {
	
	private final static int IS   = 0;
	private final static int SEND = 1;

	//-----------------------------------------------------------------
	public TerminalType() {
		super(24,"TTYPE");
	}

	//-----------------------------------------------------------------
	/**
	 * @throws IOException 
	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket nvt) throws IOException {
		if (!nvt.isInClientMode()) {
			requestUsage(nvt);
		}
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
			throws IOException {
		logger.log(Level.DEBUG,"performSubNegotiation");
		in.setHigherLevelControl(true);
		
		int isOrSend = in.read();
		switch (isOrSend) {
		case IS:
			StringBuffer buf = new StringBuffer();
			int chr = -1;
			do {
				chr = in.read();
				if (chr<240)
					buf.append((char)chr);
			} while (chr!=255);
			logger.log(Level.INFO,"Terminal type = "+buf);
			
			nvt.fireOptionDataChanged(this, buf.toString());
			break;
		case SEND:
		default:
			logger.log(Level.WARNING,"Not implemented: "+isOrSend);
		}

		int se = in.read();
		in.setHigherLevelControl(false);
		if (se!=TelnetConstants.SE) throw new IOException("Expected SE after terminal types IAC");
	}

	//-----------------------------------------------------------------
	@Override
	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
		if (iAmInitiator) {
			logger.log(Level.DEBUG,"Requesting terminal type");
			OutputStream out = nvt.getOutputStream();
			out.write(TelnetConstants.IAC);
			out.write(TelnetConstants.SB);
			out.write(getCode());
			out.write(SEND);
			out.write(TelnetConstants.IAC);
			out.write(TelnetConstants.SE);
			out.flush();
		}
	}

}
