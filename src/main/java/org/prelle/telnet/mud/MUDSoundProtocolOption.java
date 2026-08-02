/**
 *
 */
package org.prelle.telnet.mud;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;

/**
 *
 * @author prelle
 *
 */
public class MUDSoundProtocolOption implements TelnetOption<TelnetOptionListener> {

	protected final static Logger logger = System.getLogger("telnet.option.msp");

	private CommunicationRole role;

	//-----------------------------------------------------------------
	public MUDSoundProtocolOption(CommunicationRole role) {
		this.role = role;
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
//		if (role==CommunicationRole.CLIENT) {
//			// client should send support
//			return false;
//		}
		return false;
	}

	//-------------------------------------------------------------------
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		logger.log(Level.WARNING, "MSPOption.handleSubnegotiation: "+Arrays.toString(values));
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(TelnetOptionListener listener) {
	}

}
