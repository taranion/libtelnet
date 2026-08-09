/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger;

import org.prelle.telnet.option.CommunicationRole;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.option.TelnetProtocol;

/**
 *
 * @author prelle
 *
 */
public class MUDSoundProtocolOption implements TelnetOption {

	protected final static Logger logger = System.getLogger("telnet.option.msp");

	//-----------------------------------------------------------------
	public MUDSoundProtocolOption() {
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getOptionCode()
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
	public boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#initiate(org.prelle.telnet.option.TelnetProtocol, org.prelle.telnet.option.CommunicationRole)
	 */
	@Override
	public void initiate(TelnetProtocol stack, CommunicationRole role) throws IOException {
		stack.getOutputStream().sendWill(getOptionCode());
	}

}
