/**
 *
 */
package org.prelle.telnet.mud;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * See http://www.zuggsoft.com/zmud/msp.htm
 * @see http://www.zuggsoft.com/zmud/msp.htm
 * @author prelle
 *
 */
public class MUDSoundProtocol extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public MUDSoundProtocol() {
		super(90, "MSP");
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
