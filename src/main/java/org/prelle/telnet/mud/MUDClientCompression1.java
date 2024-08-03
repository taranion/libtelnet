/**
 *
 */
package org.prelle.telnet.mud;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * See http://
 * @see http://
 * @author prelle
 *
 */
public class MUDClientCompression1 extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public MUDClientCompression1() {
		super(85, "COMPRESS");
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
