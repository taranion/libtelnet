/**
 * 
 */
package org.prelle.telnet.mud;

import java.io.IOException;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * See http://tintin.sourceforge.net/mssp/
 * @see http://tintin.sourceforge.net/mssp/
 * @author prelle
 *
 */
public class MUDServerStatusProtocol extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public MUDServerStatusProtocol() {
		super(70, "MSSP");
	}
	
	private final static int MSSP_VAR = 1;
	private final static int MSSP_VAL = 2;

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
			throws IOException {
	}

}
