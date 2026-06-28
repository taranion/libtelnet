/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 *
 * @author prelle
 *
 */
public class MXPOption implements TelnetSubnegotiationHandler {

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
	@Override
	public String getName() { return "MXP"; }
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	public boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#negotiateDetails(org.prelle.telnet.TelnetProtocol)
	 */
	public boolean negotiateDetails(TelnetProtocol stack) {
		logger.log(Level.ERROR, "Ask for support");
		String support = "\u001B[6z<SUPPORT>\u001B[7z";
		try {
			stack.getOutputStream().write(support.getBytes(StandardCharsets.US_ASCII));
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	//-------------------------------------------------------------------
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		logger.log(Level.WARNING, "MXPOption.handleSubnegotiation: "+Arrays.toString(values));
	}

}
