/**
 *
 */
package org.prelle.telnet.option;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 1073
 * @author prelle
 *
 */
public class TelnetWindowSize extends TelnetOptionHandler {

	protected final static Logger logger = System.getLogger("telnet.option.naws");

	//-----------------------------------------------------------------
	public TelnetWindowSize() {
		super(31,"NAWS");
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
}