/**
 *
 */
package org.prelle.telnet.option;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * @author prelle
 *
 */
public class CarriageReturnDisposition extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public CarriageReturnDisposition() {
		super(10, "NOCARD");
	}

//	//-----------------------------------------------------------------
//	/**
//	 * @throws IOException
//	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
//	 */
//	@Override
//	public void initialize(TelnetSocket console) throws IOException {
//		requestUsage(console);
//	}

}
