/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 * @see https://tintin.mudhalla.net/protocols/gmcp/
 * @see https://www.achaea.com/local/Achaea_GMCP_Spec_20140311.pdf
 * @see https://mume.org/help/generic_mud_communication_protocol
 * @see https://github.com/BeipDev/BeipMU/blob/master/Documentation/GMCP.md
 * @author prelle
 *
 */
public class GenericMUDCommunicationProtocol implements TelnetSubnegotiationHandler {

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

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#getOptionCode()
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
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#startCommunicationAs(org.prelle.telnet.CommunicationRole)
	 */
	@Override
	public boolean startCommunicationAs(CommunicationRole role) {
		return true;
	}

	//-------------------------------------------------------------------
	public static void send(TelnetOutputStream out, String packName, String command) throws IOException {
		String full = (command!=null)?(packName+" "+command):packName;
//		logger.log(Level.WARNING, "GMCP: "+full);
		out.sendSubNegotiation(CODE, full);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		RawGMCPMessage msg = new RawGMCPMessage(values);
		TelnetOptionListener listenerR = stack.getOptionListener(CODE);
		if (!msg.getNamespace().toLowerCase().contains("core.ping"))
			logger.log(Level.INFO,"RCV {0} with {1}", msg.getNamespace(), msg.msg);
//		System.err.println("GenericMUDCommunicationProtocol: RCV: "+msg);
		GMCPReceiver listener = (GMCPReceiver)listenerR;
		if (listener!=null) {
			listener.telnetReceiveGMCP(msg);
		} else
			logger.log(Level.WARNING, "No listener for GMCP - use session.getSocket().setOptionListener(TelnetOption.GMCP, ...)");
	}

	@Override
	public boolean negotiateDetails(TelnetProtocol stack) {
		// TODO Auto-generated method stub
		return true;
	}

}
