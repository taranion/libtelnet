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
//	//-----------------------------------------------------------------
//	/**
//	 * Inform remote party of the current window size
//	 * @param sock
//	 * @param x
//	 * @param y
//	 * @throws IOException
//	 */
//	public static void sendNewSize(TelnetSocket sock, int x, int y) throws IOException {
//		if (x>65535)
//			throw new IllegalArgumentException("X to big");
//		if (y>65535)
//			throw new IllegalArgumentException("Y to big");
//
//		TelnetOutputStream out = (TelnetOutputStream) sock.getOutputStream();
//
//		TelnetOptionHandler.startSubNegotiation(sock, TelnetOption.NAWS.getCode());
//		out.write(x>>8);
//		out.write(x%256);
//		out.write(y>>8);
//		out.write(y%256);
//		TelnetOptionHandler.endSubNegotiation(sock, TelnetOption.NAWS.getCode());
//		out.flush();
//	}

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