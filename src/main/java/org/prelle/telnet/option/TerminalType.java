/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger.Level;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.WillVariable;

/**
 * RFC 1091
 * @see http://tools.ietf.org/html/rfc1091
 * @author prelle
 *
 */
public class TerminalType extends TelnetOption {

	private final static int    CODE = 24;
	private final static String NAME = "TERMINAL_TYPE";
	
	private final static int IS   = 0;
	private final static int SEND = 1;

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
	 * @throws IOException 
	 * @see org.prelle.telnet.option.TelnetOption#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
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
			out.write(CODE);
			out.write(SEND);
			out.write(TelnetConstants.IAC);
			out.write(TelnetConstants.SE);
			out.flush();
		}
	}

}
