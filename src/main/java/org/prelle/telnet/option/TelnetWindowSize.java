/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 1073
 * @author prelle
 *
 */
public class TelnetWindowSize extends TelnetOptionHandler {

	public final static int CODE = 31;

	protected final static Logger logger = System.getLogger("telnet.option.naws");

	//-----------------------------------------------------------------
	public TelnetWindowSize() {
		super(CODE,"NAWS");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initializeAs(org.prelle.telnet.Role)
	 */
	@Override
	public boolean initializeAs(Role role, TelnetSocket nvt, TelnetOutputStream out) {
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket nvt, TelnetOutputStream out) {
		int x = values[0]*256 + values[1];
		int y = values[2]*256 + values[3];
		logger.log(Level.DEBUG,"Terminal size = "+ x+"*"+y);
		nvt.fireOptionDataChanged(this, new TelnetWindowSizeData(x, y));
	}

	//-------------------------------------------------------------------
	public static void sendUpdate(TelnetOutputStream out, int w, int h) throws IOException {
		startSubNegotiation(out, CODE);
		byte[] command = new byte[4];
		command[0] = (byte) (w/256);
		command[1] = (byte) (w%256);
		command[2] = (byte) (h/256);
		command[3] = (byte) (h%256);
		out.write(command);
		endSubNegotiation(out, CODE);
	}

}