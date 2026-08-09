package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;

/**
 * 
 */
public class EchoMode implements TelnetOption {

	protected final static Logger logger = System.getLogger("telnet.echo");

	public static interface EchoModeListener {
		public void setRemoteEcho(boolean value);
	}

	@Override
	public int getOptionCode() {
		return 1;
	}

	@Override
	public String getName() {
		return "ECHO";
	}
	
	//-----------------------------------------------------------------
	@Override
	public boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	@Override
	public void initiate(TelnetProtocol stack, CommunicationRole role) throws IOException {
		stack.getOutputStream().sendWill(getOptionCode());
	}

}
