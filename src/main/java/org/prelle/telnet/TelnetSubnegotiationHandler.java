/**
 *
 */
package org.prelle.telnet;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * @author prelle
 *
 */
public abstract class TelnetSubnegotiationHandler implements TelnetConstants {

	protected final static Logger logger = System.getLogger("telnet.option");

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 */
	public boolean initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "Forgot to implement initialization for {0} / {1} as {2}",option.name(), getClass().getName(), role);
		return false;
	}

	//-----------------------------------------------------------------
	public abstract void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out);

}
