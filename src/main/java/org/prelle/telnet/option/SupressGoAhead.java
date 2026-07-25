package org.prelle.telnet.option;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;

/**
 * 
 */
public class SupressGoAhead implements TelnetOption<TelnetOptionListener> {

	//-------------------------------------------------------------------
	/**
	 */
	public SupressGoAhead() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getOptionCode() {
		return 3;
	}

	@Override
	public String getName() {
		return "SGA";
	}
	
	//-----------------------------------------------------------------
	@Override
	public boolean startCommunicationAs(CommunicationRole role) {
		return false;
	}

	@Override
	public boolean negotiateDetails(TelnetProtocol stack) {
		return false;
	}

	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
	}

	@Override
	public void addListener(TelnetOptionListener listener) {
	}

}
