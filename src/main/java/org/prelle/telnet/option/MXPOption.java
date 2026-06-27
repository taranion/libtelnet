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
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;
import org.prelle.telnet.mud.MUDTerminalTypeData;

/**
 *
 * @author prelle
 *
 */
public class MXPOption extends TelnetSubnegotiationHandler {

	protected final static Logger logger = System.getLogger("telnet.option.mxp");
	

	public static class MXPFeatures {
		
	}

	public static interface MXPListener extends TelnetOptionListener {
		public void telnetMXPLearned(MXPFeatures data);
	}

	private List<String> supports;
	private String client;

	//-----------------------------------------------------------------
	public MXPOption(String ...supports) {
		this.supports = List.of(supports);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return 91;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#initializeAs(org.prelle.telnet.TelnetOption, org.prelle.telnet.CommunicationRole, org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	public boolean initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		System.err.println("MXPOption.initializeAs "+role);
		try {
			if (role==CommunicationRole.SERVER) {
				logger.log(Level.ERROR, "Ask for support");
				String support = "\u001B[6z<SUPPORT>\u001B[7z";
				out.write(support.getBytes(StandardCharsets.US_ASCII));
				return true;
			}
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed requesting terminal type",e);
		}
		return false;
	}

	//-------------------------------------------------------------------
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "MXPOption.handleSubnegotiation: "+Arrays.toString(values));
	}

	public void processMXP(String input) {
		logger.log(Level.ERROR, "MXP:"+input);
	}

}
