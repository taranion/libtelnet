/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 857
 * @see http://tools.ietf.org/html/rfc857
 * @author prelle
 *
 */
public class TelnetEnvironmentOption extends TelnetOptionHandler {

	//-------------------------------------------------------------------
	public TelnetEnvironmentOption() {
		super(39, "NEW-ENVIRON");
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		// Not sending and not awaiting echo
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
