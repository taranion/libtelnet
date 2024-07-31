/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 1091
 * @see http://tools.ietf.org/html/rfc1091
 * @author prelle
 *
 */
public class TerminalType extends TelnetOptionHandler {

	private final static int IS   = 0;
	private final static int SEND = 1;

	private String[] answers;

	private Integer selected;

	private List<String> received = new ArrayList<>();

	//-------------------------------------------------------------------
	/**
	 * @param compatibility All terminals this instance is compatible to
	 */
	public TerminalType(String...compatibility) {
		super(24,"TTYPE");
		answers = compatibility;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		int operation = values[0];
		if (operation==SEND) {
			logger.log(Level.DEBUG, "Remote party requests terminal type information and we are {0}", role);
			if (role==Role.PROVIDER) {
				try {
					sendNextFromList(out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logger.log(Level.ERROR, "The client requested a terminal type info from us, but we are a server");
			}
		} else {
			logger.log(Level.DEBUG, "Remote party provides terminal type information and we are {0}", role);
			byte[] data = new byte[values.length-1];
			for (int i=1; i<values.length; i++) data[i-1]=(byte) values[i];
			String value = new String(data, origin.getCharset());
			if (role==Role.REQUESTER) {
				logger.log(Level.INFO, "TERMINAL_TYPE: Received {0}", value);
				if (!received.contains(value) && !"UNKNOWN".equals(value)) {
					received.add(value);
					try {
						requestNext(out);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					logger.log(Level.INFO, "Received {0}", received);
					processResult(origin, received);
				}
			} else {
				logger.log(Level.ERROR, "The client requested a terminal type info from us, but we are a server");
			}
		}

	}

	//-----------------------------------------------------------------
	private void sendNextFromList(TelnetOutputStream out) throws IOException {
		if (answers==null || answers.length==0) {
			out.sendSubNegotiation(code, IS, "UNKNOWN".getBytes(StandardCharsets.ISO_8859_1));
			return;
		}

		if (selected==null) {
			selected=-1;
		}
		selected++;

		String toSend = (selected<answers.length)?answers[selected]:answers[answers.length-1];
		if (selected>=answers.length) {
			selected=null;
		}
		logger.log(Level.INFO,"Send terminal type ''{0}''", toSend);
		out.sendSubNegotiation(code, IS, toSend.getBytes(StandardCharsets.ISO_8859_1));
	}

	//-----------------------------------------------------------------
	private void requestNext(TelnetOutputStream out) throws IOException {
		byte[] send = new byte[6];
		send[0] = (byte)IAC;
		send[1] = (byte)SB;
		send[2] = (byte)code;
		send[3] = (byte)SEND;
		send[4] = (byte)IAC;
		send[5] = (byte)SE;
		out.writeCommand(send);
		out.flush();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initializeAs(org.prelle.telnet.Role)
	 */
	@Override
	public boolean initializeAs(Role role, TelnetSocket nvt, TelnetOutputStream out) {
		if (role==Role.PROVIDER) return false;
		logger.log(Level.DEBUG, "Ask remote party to send terminal types");
		try {
			requestNext(out);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * Called when all information has been received
	 */
	protected void processResult(TelnetSocket nvt, List<String> data) {
		nvt.fireOptionDataChanged(this, received);
	}

}
