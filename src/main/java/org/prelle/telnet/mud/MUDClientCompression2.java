/**
 *
 */
package org.prelle.telnet.mud;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * @author prelle
 *
 */
public class MUDClientCompression2 extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public MUDClientCompression2() {
		super(86, "COMPRESS2");
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
