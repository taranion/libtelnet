/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.option.MxpSupportTable;
import org.prelle.telnet.TelnetOption;

/**
 *
 * @author prelle
 *
 */
public class MUDSoundProtocolOption implements TelnetOption<MUDSoundProtocolListener> {

	protected final static Logger logger = System.getLogger("telnet.option.msp");

	private CommunicationRole role;
	private List<String> supports;
	private MxpSupportTable supportTable = new MxpSupportTable();
	private String client;
	private List<MUDSoundProtocolListener> listeners = new ArrayList<>();

	//-----------------------------------------------------------------
	public MUDSoundProtocolOption(CommunicationRole role,String ...supports) {
		this.role = role;
		this.supports = List.of(supports);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return 90;
	}
	
	//-------------------------------------------------------------------
	@Override
	public String getName() { return "MSP"; }
	
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
	 * @see org.prelle.telnet.TelnetOption#negotiateDetails(org.prelle.telnet.TelnetProtocol)
	 */
	public boolean negotiateDetails(TelnetProtocol stack) {
		if (role==CommunicationRole.CLIENT) {
			// client should send support
			return false;
		}
		logger.log(Level.ERROR, "Ask for support");
		String support = "\u001B[6z<SUPPORT>\u001B[7z";
		try {
			stack.getOutputStream().write(support.getBytes(StandardCharsets.US_ASCII));
			return false;
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

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(MUDSoundProtocolListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	//-------------------------------------------------------------------
	public void fireCommandReceivedChange(String data) {
		for (MUDSoundProtocolListener l: listeners) {
			l.mspReceivedCommand(data);
		}
	}

}
