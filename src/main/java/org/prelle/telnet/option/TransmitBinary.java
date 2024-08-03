/**
 *
 */
package org.prelle.telnet.option;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * @author prelle
 *
 */
public class TransmitBinary extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public TransmitBinary() {
		super(0, "TRANSMIT_BINARY");
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
