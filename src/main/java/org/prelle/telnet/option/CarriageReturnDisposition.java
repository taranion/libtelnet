/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

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
