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
		startSubNegotiation(sock.getOutputStream(), code);
	}

	//-----------------------------------------------------------------
	protected static void endSubNegotiation(TelnetSocket sock, int code) throws IOException {
		endSubNegotiation(sock.getOutputStream(), code);
	}

	//-----------------------------------------------------------------
	protected static void startSubNegotiation(OutputStream out, int code) throws IOException {
		out.write(IAC);
		out.write(SB);
		out.write(code);
	}

	//-----------------------------------------------------------------
	protected static void endSubNegotiation(OutputStream out, int code) throws IOException {
		out.write(IAC);
		out.write(SE);
//		out.write(code);
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE if a subnegotiation is needed
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

	//-----------------------------------------------------------------
	public static void sendSubNegotiationString(TelnetOutputStream out, int code, String data) throws IOException {
		startSubNegotiation(out, code);
		out.write(data.getBytes(StandardCharsets.UTF_8));
		endSubNegotiation(out, code);
		logger.log(Level.TRACE, "--{0}--> {1}", code,data);
	}


}
