/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * @see https://tintin.mudhalla.net/protocols/gmcp/
 * @see https://www.achaea.com/local/Achaea_GMCP_Spec_20140311.pdf
 * @see https://mume.org/help/generic_mud_communication_protocol
 * @see https://github.com/BeipDev/BeipMU/blob/master/Documentation/GMCP.md
 * @author prelle
 *
 */
public class GenericMUDCommunicationProtocol extends TelnetOptionHandler {

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

	//-----------------------------------------------------------------
	public GenericMUDCommunicationProtocol() {
		super(CODE, "GMCP");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket nvt, TelnetOutputStream out) {
		RawGMCPMessage msg = new RawGMCPMessage(values);
//		logger.log(Level.DEBUG,"As {0} we received2 : {1}", role, msg.getNamespace());
		nvt.fireOptionDataChanged(this, msg);
	}

	//-------------------------------------------------------------------
	public static void send(TelnetOutputStream out, String packName, String command) throws IOException {
		String full = (command!=null)?(packName+" "+command):packName;
		sendSubNegotiationString(out, 201, full);
	}

}
