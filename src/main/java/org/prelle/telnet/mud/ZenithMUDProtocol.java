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
public class ZenithMUDProtocol extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public ZenithMUDProtocol() {
		super(93, "ZMP");
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
//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
//	 */
//	@Override
//	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
//			throws IOException {
//	}

}
