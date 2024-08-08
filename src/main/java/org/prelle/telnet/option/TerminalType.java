/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;
import org.prelle.telnet.mud.MUDTerminalTypeData;

/**
 * RFC 1091
 * @see http://tools.ietf.org/html/rfc1091
 * @author prelle
 *
 */
public class TerminalType extends TelnetSubnegotiationHandler {

	public static class TerminalTypeData {
		protected List<String> options = new ArrayList<>();
		public TerminalTypeData() {
			options = new ArrayList<>();
		}
		public TerminalTypeData(String... values) {
			options = List.of(values);
		}
		public TerminalTypeData(List<String> values) {
			options = values;
		}

		public void addOption(String value) {
			options.add(value);
		}
		public boolean hasOption(String value) {
			return options.contains(value);
		}
		public String getFirstOption() {
			return options.isEmpty()?null:options.get(0);
		}
	}

	public static interface TerminalTypeListener extends TelnetOptionListener {
		public void telnetTerminalTypesLearned(TerminalTypeData data);
	}

	private final static int IS   = 0;
	private final static int SEND = 1;

	private String[] answers;

	private Integer selected;

	//-----------------------------------------------------------------
	public TerminalType(String ...options) {
		this.answers = options;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#initializeAs(org.prelle.telnet.TelnetOption, org.prelle.telnet.CommunicationRole, org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	public boolean initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		try {
			if (role==CommunicationRole.SERVER) {
				logger.log(Level.DEBUG, "Ask remote party to send terminal types");
				List<String> received = new ArrayList<>();
				origin.setOptionData(TelnetOption.TERMINAL_TYPE.getCode(), received);
				requestNext(out);
				return true;
			}
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed requesting terminal type",e);
		}
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		int operation = values[0];
		if (operation==SEND) {
			logger.log(Level.INFO, "Remote party requests terminal type information");
			try {
				sendNextFromList(out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
//			logger.log(Level.DEBUG, "Remote party provides terminal type information");
			byte[] data = new byte[values.length-1];
			for (int i=1; i<values.length; i++) data[i-1]=(byte) values[i];
			List<String> received = origin.getOptionData(TelnetOption.TERMINAL_TYPE.getCode());
			String value = new String(data, StandardCharsets.US_ASCII);
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
					origin.subnegotiationEndedFor(code,received);

					TerminalTypeListener listener = origin.getOptionListener(code);
					if (listener!=null) {
						if (received.size()==3) {
							listener.telnetTerminalTypesLearned(new MUDTerminalTypeData(received));
						} else {
							listener.telnetTerminalTypesLearned(new TerminalTypeData(received));
						}
					} else {
						logger.log(Level.TRACE, "No TerminalTypeListener");
					}
				}
		}

	}

	//-----------------------------------------------------------------
	private void sendNextFromList(TelnetOutputStream out) throws IOException {
		if (answers==null || answers.length==0) {
			out.sendSubNegotiation(TelnetOption.TERMINAL_TYPE.getCode(), IS, "UNKNOWN".getBytes(StandardCharsets.ISO_8859_1));
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
		out.sendSubNegotiation(TelnetOption.TERMINAL_TYPE.getCode(), IS, toSend.getBytes(StandardCharsets.ISO_8859_1));
	}

	//-----------------------------------------------------------------
	private void requestNext(TelnetOutputStream out) throws IOException {
		byte[] send = new byte[6];
		send[0] = (byte)IAC;
		send[1] = (byte)SB;
		send[2] = (byte)TelnetOption.TERMINAL_TYPE.getCode();
		send[3] = (byte)SEND;
		send[4] = (byte)IAC;
		send[5] = (byte)SE;
		out.writeCommand(send);
		out.flush();
	}

}
