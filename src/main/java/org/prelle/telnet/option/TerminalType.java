/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.mud.MUDTerminalTypeData;
import org.prelle.telnet.protocol.SubnegotiationFinishedEvent;
import org.prelle.telnet.protocol.TelnetOptionEvent;
import org.prelle.telnet.protocol.TelnetOptionEventImpl;
import org.prelle.telnet.protocol.TelnetProtocol;

/**
 * RFC 1091
 * @see http://tools.ietf.org/html/rfc1091
 * @author prelle
 *
 */
public class TerminalType implements TelnetOption {

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

	public static class TerminalTypesEvent extends TelnetOptionEventImpl {
		private TerminalTypeData data;
		public TerminalTypesEvent(TelnetOption option, TerminalTypeData data) {
			super(option);
			this.data = data;
		}
		public TerminalTypeData getData() { return data; }
	}

	private final static int IS   = 0;
	private final static int SEND = 1;

	protected List<String> options;

	private Integer selected;

	//-----------------------------------------------------------------
	public TerminalType(String ...options) {
		if (options==null || options.length==0) {
			this.options = new ArrayList<>();
		} else
			this.options = List.of(options);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return 24;
	}
	
	public String getName() { return "TTYPE"; }

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#resolveSubCommandName(int, byte)
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
	 * @see org.prelle.telnet.option.TelnetOption#startNegotiationAs(org.prelle.telnet.option.CommunicationRole)
	 */
	@Override
	public boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#negotiateDetails(org.prelle.telnet.protocol.TelnetProtocol)
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol stack, CommunicationRole role) {
		try {
			logger.log(Level.DEBUG, "Ask remote party to send terminal types");
			requestNext(stack);
			return true;
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed requesting terminal type",e);
		}
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#handleSubnegotiation(int[], org.prelle.telnet.protocol.TelnetProtocol)
	 */
	@Override
	public List<TelnetOptionEvent> handleSubnegotiation(TelnetSubnegotiationEvent event, TelnetProtocol stack) {
		List<TelnetOptionEvent> result = new ArrayList<>();
		int[] values = event.getAsIntArray();
		int operation = values[0];
		if (operation==SEND) {
			logger.log(Level.INFO, "Remote party requests terminal type information");
			try {
				sendNextFromList(stack);
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
					requestNext(stack);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logger.log(Level.INFO, "Received {0}", options);
					if (options.size()==3) {
						result.add(new TerminalTypesEvent(this,new MUDTerminalTypeData(options)));
					} else {
						result.add(new TerminalTypesEvent(this,new TerminalTypeData(options)));
					}
				result.add(new SubnegotiationFinishedEvent(this));
			}
		}

		return result;
	}

	//-----------------------------------------------------------------
	private void sendNextFromList(TelnetProtocol proto) throws IOException {
		if (options==null || options.size()==0) {
			byte[] data = new byte[6];
			data[0] = IS;
			System.arraycopy("xterm".getBytes(StandardCharsets.US_ASCII), 0, data, 1, 5);			
			proto.sendResponse(proto.factory().createTelnetSubnegotiationEvent(this.getOptionCode(), data));
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
		byte[] data = new byte[toSend.length()+1];
		data[0] = IS;
		System.arraycopy(toSend.getBytes(StandardCharsets.US_ASCII), 0, data, 1, toSend.length());
		proto.sendResponse(proto.factory().createTelnetSubnegotiationEvent(this.getOptionCode(), data));
	}

	//-----------------------------------------------------------------
	private void requestNext(TelnetProtocol proto) throws IOException {
		proto.sendResponse(proto.factory().createTelnetSubnegotiationEvent(this.getOptionCode(), new byte[] {(byte)SEND}));
	}

}
