/**
 *
 */
package org.prelle.telnet.mud;

import java.lang.System.Logger.Level;

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

	public static class RawGMCPMessage{
		private String namespace;
		private String msg;

		public RawGMCPMessage(int[] values) {
			StringBuffer namespace = new StringBuffer();
			StringBuffer msg = new StringBuffer();
			boolean isNamespace = true;
			for (int code : values) {
				if (isNamespace) {
					if (code==32) {
						isNamespace=false;
					} else {
						namespace.append( (char)code );
					}
				} else
					msg.append( (char)code );
			}
			this.namespace = namespace.toString().trim();
			this.msg = msg.toString().trim();;
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
		super(201, "GMCP");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket nvt, TelnetOutputStream out) {
		RawGMCPMessage msg = new RawGMCPMessage(values);
		logger.log(Level.DEBUG,"As {0} we received : {1}", role, msg.getMessage());
		nvt.fireOptionDataChanged(this, msg);
	}

}
