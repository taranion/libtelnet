/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.NetworkVirtualConsole;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.WillVariable;

/**
 * @author prelle
 *
 */
public class TerminalType extends TelnetOption {

	private final static int    CODE = 24;
	private final static String NAME = "TERMINAL_TYPE";
	
	private final static int IS   = 0;
	private final static int SEND = 1;
	
	private String terminalType;

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#setDefaults(org.prelle.telnet.NetworkVirtualConsole)
	 */
	@Override
	public void setDefaults(NetworkVirtualConsole nvt) {
		nvt.setOptionVariable(new WillVariable(NAME, false));
		nvt.setOptionVariable(new DoVariable(NAME, false));
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
	 * @see org.prelle.telnet.option.TelnetOption#initialize(org.prelle.telnet.NetworkVirtualConsole)
	 */
	@Override
	public void initialize(NetworkVirtualConsole console) throws IOException {
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#performSubNegotiation(org.prelle.telnet.NetworkVirtualConsole, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(NetworkVirtualConsole nvt, InputStream in)
			throws IOException {
		int isOrSend = in.read();
		switch (isOrSend) {
		case IS:
			StringBuffer buf = new StringBuffer();
			int chr = -1;
			do {
				chr = in.read();
				if (chr<255)
					buf.append((char)chr);
			} while (chr!=255);
			logger.info("Terminal type = "+buf);
			terminalType = buf.toString();
			break;
		case SEND:
		default:
			logger.warn("Not implemented: "+isOrSend);
		}
		
		if (in.read()!=TelnetConstants.SE) throw new IOException("Expected SE after terminal types IAC");
		
		nvt.getListener().terminalTypeDetermined(nvt, terminalType);
	}

	//-----------------------------------------------------------------
	@Override
	protected void optionEnabled(NetworkVirtualConsole nvt, boolean iAmInitiator) throws IOException {
		if (iAmInitiator) {
			logger.debug("Requesting terminal type");
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

	//-----------------------------------------------------------------
	/**
	 * @return the terminalType
	 */
	public String getTerminalType() {
		return terminalType;
	}

}
