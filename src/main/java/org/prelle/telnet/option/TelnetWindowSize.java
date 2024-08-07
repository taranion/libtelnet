/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 * RFC 1073
 * @author prelle
 *
 */
public class TelnetWindowSize extends TelnetSubnegotiationHandler {

	public final static int CODE = 31;

	protected final static Logger logger = System.getLogger("telnet.option.naws");

	public static interface TelnetNAWSListener {
		public void telnetWindowSizeChanged(int width, int height);
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE if a subnegotiation is needed
	 */
	public void initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		if (role==CommunicationRole.CLIENT) {
			int[] size = origin.getOptionData(CODE);
			if (size!=null && size.length==2) {
				try {
					sendUpdate(origin, size[0], size[1]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#initializeAs(org.prelle.telnet.Role)
//	 */
//	@Override
//	public boolean initializeAs(Role role, TelnetSocket nvt, TelnetOutputStream out) {
//		return false;
//	}
//
//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
//	 */
//	@Override
//	public void handleSubnegotiation(Role role, int[] values, TelnetSocket nvt, TelnetOutputStream out) {
//		int x = values[0]*256 + values[1];
//		int y = values[2]*256 + values[3];
//		logger.log(Level.DEBUG,"Terminal size = "+ x+"*"+y);
//		nvt.fireOptionDataChanged(this, new TelnetWindowSizeData(x, y));
//	}

	//-------------------------------------------------------------------
	public static void sendUpdate(TelnetSocket origin, int w, int h) throws IOException {
		logger.log(Level.INFO, "Send NAWS {0}x{1}", w,h);
		byte[] command = new byte[4];
		command[0] = (byte) (w/256);
		command[1] = (byte) (w%256);
		command[2] = (byte) (h/256);
		command[3] = (byte) (h%256);
		origin.out().sendSubNegotiation(CODE, command);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "TODO: Subnegotiate for NAWS");
	}

}