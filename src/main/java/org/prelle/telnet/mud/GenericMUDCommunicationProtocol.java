/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetOption;

/**
 * @see https://tintin.mudhalla.net/protocols/gmcp/
 * @see https://www.achaea.com/local/Achaea_GMCP_Spec_20140311.pdf
 * @see https://mume.org/help/generic_mud_communication_protocol
 * @see https://github.com/BeipDev/BeipMU/blob/master/Documentation/GMCP.md
 * @author prelle
 *
 */
public class GenericMUDCommunicationProtocol implements TelnetOption<GenericMUDCommunicationProtocol.GMCPReceiver> {

	protected final static Logger logger = System.getLogger("telnet.gmcp");

	public final static int CODE = 201;

	public static class RawGMCPMessage{
		private String namespace;
		private String msg;

		public RawGMCPMessage(int[] values) {
			StringBuffer ns = new StringBuffer();
			StringBuffer m = new StringBuffer();
			boolean isNamespace = true;
			for (int code : values) {
				if (isNamespace) {
					if (code==32) {
						isNamespace=false;
					} else {
						ns.append( (char)code );
					}
				} else
					m.append( (char)code );
			}
			this.namespace = ns.toString().trim();
			this.msg = m.toString().trim();;
		}
		public String getNamespace() {
			return namespace;
		}
		public String getMessage() {
			return msg;
		}
		public String toString() {
			return "GMCP:"+namespace+" "+msg;
		}
	}

	public static interface GMCPReceiver extends TelnetOptionListener {
		public void telnetReceiveGMCP(RawGMCPMessage message);
	}
	
	private List<GMCPReceiver> listeners = new ArrayList<>();

	//-------------------------------------------------------------------
	public GenericMUDCommunicationProtocol() {
	}

	//-------------------------------------------------------------------
	public GenericMUDCommunicationProtocol(GMCPReceiver callback) {
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
	@Override
	public String getName() {
		return "GMCP";
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#startCommunicationAs(org.prelle.telnet.CommunicationRole)
	 */
	@Override
	public boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}
	
	public ControlCode initiate(TelnetProtocol stack, CommunicationRole role) throws IOException {
		if (role==CommunicationRole.SERVER) {
			stack.getOutputStream().sendWill(getOptionCode());
			return ControlCode.WILL;
		}
		return null;
	}

	//-------------------------------------------------------------------
	public static void send(TelnetOutputStream out, String packName, String command) throws IOException {
		String full = (command!=null)?(packName+" "+command):packName;
//		logger.log(Level.WARNING, "GMCP: "+full);
		out.sendSubNegotiation(CODE, full);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		RawGMCPMessage msg = new RawGMCPMessage(values);
		if (!msg.getNamespace().toLowerCase().contains("core.ping"))
			logger.log(Level.INFO,"RCV {0} with {1}", msg.getNamespace(), msg.msg);
//		System.err.println("GenericMUDCommunicationProtocol: RCV: "+msg);
		listeners.forEach(l -> l.telnetReceiveGMCP(msg));
		
		stack.fireSubnegotiationFinished(this);
	}

	@Override
	public boolean negotiateDetails(TelnetProtocol stack) {
		System.err.println("GenericMUDCommunicationProtocol.negotiateDetails() called");
		try {
			send(stack.getOutputStream(), "Server.Hello","()");
			String[] packages = new String[] {
					"Core 1",
					"Char 1",
					"Char.Login 1",
					"Room 1",
					"Comm 2",
					"Char.Vitals 1",
					"Client.Media 1",
					"mudstd.channel 1",
					"mudstd.combat 1",
					"mudstd.dialog 1",
					"mudstd.resources 1",
					"mudstd.room 1",
					"mudstd.tilemap 1",
					"WebView 1",
					"Beip 1"
			};
			send(stack.getOutputStream(), "Server.Supports.Set","[\""+String.join("\",\"", packages)+"\"]");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(GMCPReceiver listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

}
