/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.protocol.SubnegotiationFinishedEvent;
import org.prelle.telnet.protocol.TelnetOptionEvent;
import org.prelle.telnet.protocol.TelnetOptionEventImpl;
import org.prelle.telnet.protocol.TelnetProtocol;

/**
 * RFC 1073
 * @author prelle
 *
 */
public class TelnetWindowSize implements TelnetOption {

	public final static int CODE = 31;

	protected final static Logger logger = System.getLogger("telnet.option.naws");

	public static class TerminalWindowSizeEvent extends TelnetOptionEventImpl {
		private int width;
		private int height;
		public TerminalWindowSizeEvent(TelnetOption option, int width, int height) {
			super(option);
			this.width = width;
			this.height = height;
		}
		public int getColumns() { return width; }
		public int getRows() { return height; }
		public String toString() {
			return "Window Size: "+width+"x"+height;
		}
	}
	
	private int rows = -1;
	private int cols = -1;
	private boolean isFirstEvent = true;

	//-------------------------------------------------------------------
	public TelnetWindowSize() {
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getName()
	 */
	@Override
	public String getName() { return "NAWS"; }
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	@Override
	public boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#initiate(org.prelle.telnet.protocol.TelnetProtocol, org.prelle.telnet.option.CommunicationRole)
	 */
	@Override
	public void initiate(TelnetProtocol stack, CommunicationRole role) throws IOException {
		if (role==CommunicationRole.SERVER) {
			stack.sendResponse(stack.factory().createTelnetNegotiationEvent(ControlCode.DO, getOptionCode()));
		}
	}
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	public boolean startSubNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.CLIENT;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE if a subnegotiation is needed
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol origin, CommunicationRole role) {
		if (role==CommunicationRole.SERVER) {
			return false;
		}
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
	public List<TelnetOptionEvent> handleSubnegotiation(TelnetSubnegotiationEvent event, TelnetProtocol stack) {
		int[] values = event.getAsIntArray();
		int x = values[0]*256 + values[1];
		int y = values[2]*256 + values[3];
		logger.log(Level.DEBUG,"Terminal size = "+ x+"x"+y);
		cols = x;
		rows = y;
		
		List<TelnetOptionEvent> result = new ArrayList<>();
		result.add(new TerminalWindowSizeEvent(this, x, y));
		if (isFirstEvent) {
			isFirstEvent = false;
			result.add(new SubnegotiationFinishedEvent(this));
		}
		return result;
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
			
			origin.sendResponse(origin.factory().createTelnetSubnegotiationEvent(CODE, command));
		}
	}

}