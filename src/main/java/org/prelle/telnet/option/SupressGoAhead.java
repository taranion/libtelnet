package org.prelle.telnet.option;

/**
 * 
 */
public class SupressGoAhead implements TelnetOption {

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
	public boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

}
