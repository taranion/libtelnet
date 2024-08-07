/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;

/**
 * @author prelle
 *
 */
public abstract class TelnetSubnegotiationHandler implements TelnetConstants {

	protected final static Logger logger = System.getLogger("telnet.option");

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 */
	public void initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "Forgot to implement initialization for {0} / {1} as {2}",option.name(), getClass().getName(), role);
	}

//	//-----------------------------------------------------------------
//	/**
//	 * Called after the use of a option has been confirmed
//	 * @return TRUE if a subnegotiation is needed
//	 */
//	public boolean initializeAs(Role role, TelnetSocket origin, TelnetOutputStream out) {
//		if (role==Role.PROVIDER) return false;
//		logger.log(Level.WARNING, "Forgot to implement initialization for {0} / {1} as {2}",name, getClass().getName(), role);
//		return false;
//	}

	//-----------------------------------------------------------------
	public abstract void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out);

//	//-----------------------------------------------------------------
//	public static void sendSubNegotiationString(TelnetOutputStream out, int code, String data) throws IOException {
//		startSubNegotiation(out, code);
//		out.write(data.getBytes(StandardCharsets.UTF_8));
//		endSubNegotiation(out, code);
//		logger.log(Level.TRACE, "--{0}--> {1}", code,data);
//	}


}
