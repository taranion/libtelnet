/**
 *
 */
package org.prelle.telnet.option;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * @author prelle
 *
 */
public class TimingMark extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public TimingMark() {
		super(6, "TIMING_MARK");
	}

//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
//	 */
//	@Override
//	public void initialize(TelnetSocket console) {
//		// Not sending and not awaiting echo
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
