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
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 1073
 * @author prelle
 *
 */
public class TelnetWindowSize implements TelnetOption<TelnetWindowSize.TelnetNAWSListener> {

	public final static int CODE = 31;

	protected final static Logger logger = System.getLogger("telnet.option.naws");

	public static interface TelnetNAWSListener extends TelnetOptionListener {
		public void telnetWindowSizeChanged(int width, int height);
	}
	
	private List<TelnetNAWSListener> listeners = new ArrayList<>();
	
	private int rows = -1;
	private int cols = -1;
	private boolean enabled = false;

	//-------------------------------------------------------------------
	public TelnetWindowSize() {
	}

	//-------------------------------------------------------------------
	public TelnetWindowSize(TelnetNAWSListener callback) {
		addListener(callback);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getName()
	 */
	@Override
	public String getName() { return "NAWS"; }
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	public boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#initiate(org.prelle.telnet.TelnetProtocol, org.prelle.telnet.CommunicationRole)
	 */
	@Override
	public ControlCode initiate(TelnetProtocol stack, CommunicationRole role) throws IOException {
		if (role==CommunicationRole.CLIENT) {
			stack.getOutputStream().sendWill(getOptionCode());
			return ControlCode.WILL;
		} else {
			stack.getOutputStream().sendDo(getOptionCode());
			return ControlCode.DO;
		}
	}
	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE if a subnegotiation is needed
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol origin) {
		System.err.println("TelnetWindowSize.negotiateDetails: cols="+cols+", rows="+rows);
		try {
			if (cols>0 && rows>0)
				sendUpdate(origin, cols, rows);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		int[] size = origin.getOptionData(CODE);
//		if (size!=null && size.length==2) {
//			try {
//				sendUpdate(origin, size[0], size[1]);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		int x = values[0]*256 + values[1];
		int y = values[2]*256 + values[3];
		logger.log(Level.DEBUG,"Terminal size = "+ x+"x"+y);
		cols = x;
		rows = y;
		listeners.forEach( l -> l.telnetWindowSizeChanged(x, y));
	}

	//-------------------------------------------------------------------
	public static void sendUpdate(TelnetSocket origin, int w, int h) throws IOException {
		sendUpdate(origin.getStack(), w, h);
	}

	//-------------------------------------------------------------------
	public void update(TelnetProtocol origin, int w, int h) throws IOException {
		this.cols = w;
		this.rows = h;
		sendUpdate(origin, w, h);
	}

	//-------------------------------------------------------------------
	public static void sendUpdate(TelnetProtocol origin, int w, int h) throws IOException {
		if (origin.isFeatureActive(CODE)) {
			logger.log(Level.INFO, "Send NAWS {0}x{1}", w,h);
			byte[] command = new byte[4];
			command[0] = (byte) (w/256);
			command[1] = (byte) (w%256);
			command[2] = (byte) (h/256);
			command[3] = (byte) (h%256);
			origin.getOutputStream().sendSubNegotiation(CODE, command);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(TelnetNAWSListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public int getRows() { return rows; }
	public int getColumns() { return cols; }
}