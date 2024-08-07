/**
 *
 */
package org.prelle.telnet.option;

import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 * @author prelle
 *
 */
public class StatusOption extends TelnetSubnegotiationHandler {

	private final static int	IS   = 0;
	private final static int	SEND = 1;

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "TODO: Subnegotiate for STATUS");
	}

}
