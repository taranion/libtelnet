/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 * RFC 1073
 * @author prelle
 *
 */
public class TelnetWindowSize implements TelnetSubnegotiationHandler<TelnetWindowSize.TelnetNAWSListener> {

	public final static int CODE = 31;

	protected final static Logger logger = System.getLogger("telnet.option.naws");

	public static interface TelnetNAWSListener extends TelnetOptionListener {
		public void telnetWindowSizeChanged(int width, int height);
	}
	
	private List<TelnetNAWSListener> listeners = new ArrayList<>();

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#getName()
	 */
	@Override
	public String getName() { return "NAWS"; }
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	public boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.CLIENT;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE if a subnegotiation is needed
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol origin) {
		int[] size = origin.getOptionData(CODE);
		if (size!=null && size.length==2) {
			try {
				sendUpdate(origin, size[0], size[1]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		int code = getOptionCode();
		int x = values[0]*256 + values[1];
		int y = values[2]*256 + values[3];
		logger.log(Level.DEBUG,"Terminal size = "+ x+"*"+y);
		stack.subnegotiationEndedFor(code, new int[] {x,y});

		TelnetNAWSListener listener = stack.getOptionListener(code);
		if (listener!=null) {
			listener.telnetWindowSizeChanged(x, y);
		} else {
			logger.log(Level.TRACE, "No TelnetNAWSListener");
		}
	}

	//-------------------------------------------------------------------
	public static void sendUpdate(TelnetSocket origin, int w, int h) throws IOException {
		sendUpdate(origin.getStack(), w, h);
	}

	//-------------------------------------------------------------------
	public static void sendUpdate(TelnetProtocol origin, int w, int h) throws IOException {
		logger.log(Level.INFO, "Send NAWS {0}x{1}", w,h);
		byte[] command = new byte[4];
		command[0] = (byte) (w/256);
		command[1] = (byte) (w%256);
		command[2] = (byte) (h/256);
		command[3] = (byte) (h%256);
		origin.getOutputStream().sendSubNegotiation(CODE, command);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(TelnetNAWSListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

}