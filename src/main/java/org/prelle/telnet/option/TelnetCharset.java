/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.HashMap;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 * https://datatracker.ietf.org/doc/html/rfc2066
 * @author prelle
 *
 */
public class TelnetCharset extends TelnetSubnegotiationHandler {

	private final static int REQUEST  = 1;
	private final static int ACCEPTED = 2;
	private final static int REJECTED = 3;
	private final static int TTABLE_IS = 4;
	private final static int TTABLE_REJECTED = 5;
	private final static int TTABLE_ACK = 6;
	private final static int TTABLE_NAK = 8;

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "TODO: Subnegotiate for CHARSET");
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 */
	public boolean initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		if (role==CommunicationRole.SERVER) {
		} else {
			logger.log(Level.WARNING, "Acting as PROVIDER not implemented");
		}
		return false;
	}

}
