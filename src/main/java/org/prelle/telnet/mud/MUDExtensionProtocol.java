/**
 * 
 */
package org.prelle.telnet.mud;

import java.io.IOException;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * See http://www.zuggsoft.com/zmud/msp.htm
 * @see http://www.zuggsoft.com/zmud/msp.htm
 * @author prelle
 *
 */
public class MUDExtensionProtocol extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public MUDExtensionProtocol() {
		super(91,"MXP");
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {

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
