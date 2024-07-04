/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * @author prelle
 *
 */
public class TelnetOptionHandler implements TelnetConstants {

	protected final static Logger logger = System.getLogger("telnet.option");

	protected int code;
	protected String name;

	//-----------------------------------------------------------------
	public TelnetOptionHandler(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getName() { return name; }
	public int getCode() { return code; }
	public String toString() { return name+"("+code+")"; }

	//-----------------------------------------------------------------
	public boolean requiresSubnegotiation() {
		return false;
	}

	//-----------------------------------------------------------------
	protected static void startSubNegotiation(TelnetSocket sock, int code) throws IOException {
		OutputStream out = sock.getOutputStream();
		out.write(IAC);
		out.write(SB);
		out.write(code);
	}

	//-----------------------------------------------------------------
	protected static void endSubNegotiation(TelnetSocket sock, int code) throws IOException {
		OutputStream out = sock.getOutputStream();
		out.write(IAC);
		out.write(SE);
		out.write(code);
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 */
	public boolean initializeAs(Role role, TelnetSocket origin, TelnetOutputStream out) {
		if (role==Role.PROVIDER) return false;
		logger.log(Level.WARNING, "Forgot to implement initialization for {0} / {1} as {2}",name, getClass().getName(), role);
		return false;
	}

	//-----------------------------------------------------------------
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "Forgot to implement dealing with subnegotiation for {0} / {1}",name, getClass().getName());
	}


}
