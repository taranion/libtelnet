/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.WellKnownTelnetOptions;
import org.prelle.telnet.mud.MUDTerminalTypeData;
import org.prelle.telnet.option.TelnetCharset.CharsetListener;

/**
 * RFC 1091
 * @see http://tools.ietf.org/html/rfc1091
 * @author prelle
 *
 */
public class TerminalType implements TelnetOption<TerminalType.TerminalTypeListener> {

	protected final static Logger logger = System.getLogger("telnet.option.ttype");

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
		public List<String> getAll() {
			return options;
		}
	}

	public static interface TerminalTypeListener extends TelnetOptionListener {
		public void telnetTerminalTypesLearned(TerminalTypeData data);
	}

	private final static int IS   = 0;
	private final static int SEND = 1;

	protected List<String> options;

	private Integer selected;
	
	private List<TerminalTypeListener> listeners = new ArrayList<>();

	//-----------------------------------------------------------------
	public TerminalType(String ...options) {
		if (options==null || options.length==0) {
			this.options = new ArrayList<>();
		} else
			this.options = List.of(options);
	}

	//-----------------------------------------------------------------
	public TerminalType(TerminalTypeListener listener) {
		listeners.add(listener);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return 24;
	}
	
	public String getName() { return "TTYPE"; }

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#resolveSubCommandName(byte)
	 */
	@Override
	public String resolveSubCommandName(int position, byte value) {
		switch (value) {
		case IS: return "IS";
		case SEND: return "SEND";
		default: return ""+value;
		}
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#startCommunicationAs(org.prelle.telnet.CommunicationRole)
	 */
	@Override
	public boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#initializeAs(org.prelle.telnet.WellKnownTelnetOptions, org.prelle.telnet.CommunicationRole, org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	public boolean negotiateDetails(TelnetProtocol stack) {
		try {
			logger.log(Level.DEBUG, "Ask remote party to send terminal types");
			requestNext(stack.getOutputStream());
			return true;
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
	public void handleSubnegotiation(int[] values, TelnetProtocol origin) {
		int operation = values[0];
		if (operation==SEND) {
			logger.log(Level.INFO, "Remote party requests terminal type information");
			try {
				sendNextFromList(origin.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
//			logger.log(Level.DEBUG, "Remote party provides terminal type information");
			byte[] data = new byte[values.length-1];
			for (int i=1; i<values.length; i++) data[i-1]=(byte) values[i];
			String value = new String(data, StandardCharsets.US_ASCII);
			logger.log(Level.INFO, "TERMINAL_TYPE: Received {0}", value);
			if (!options.contains(value) && !"UNKNOWN".equals(value)) {
				options.add(value);
				try {
					requestNext(origin.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logger.log(Level.INFO, "Received {0}", options);
				for (TerminalTypeListener listener : listeners) {
					if (options.size()==3) {
						listener.telnetTerminalTypesLearned(new MUDTerminalTypeData(options));
					} else {
						listener.telnetTerminalTypesLearned(new TerminalTypeData(options));
					}
				}
				origin.fireSubnegotiationFinished(this);
			}
		}

	}

	//-----------------------------------------------------------------
	private void sendNextFromList(TelnetOutputStream out) throws IOException {
		if (options==null || options.size()==0) {
			out.sendSubNegotiation(WellKnownTelnetOptions.TERMINAL_TYPE.getCode(), IS, "xterm".getBytes(StandardCharsets.ISO_8859_1));
			return;
		}

		if (selected==null) {
			selected=-1;
		}
		selected++;

		String toSend = (selected<options.size())?options.get(selected):options.get(options.size()-1);
		if (selected>=options.size()) {
			selected=null;
		}
		logger.log(Level.INFO,"Send terminal type ''{0}''", toSend);
		out.sendSubNegotiation(WellKnownTelnetOptions.TERMINAL_TYPE.getCode(), IS, toSend.getBytes(StandardCharsets.ISO_8859_1));
	}

	//-----------------------------------------------------------------
	private void requestNext(TelnetOutputStream out) throws IOException {
		byte[] send = new byte[6];
		send[0] = (byte)IAC;
		send[1] = (byte)SB;
		send[2] = (byte)WellKnownTelnetOptions.TERMINAL_TYPE.getCode();
		send[3] = (byte)SEND;
		send[4] = (byte)IAC;
		send[5] = (byte)SE;
		out.writeCommand(send);
		out.flush();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(TerminalTypeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

}
