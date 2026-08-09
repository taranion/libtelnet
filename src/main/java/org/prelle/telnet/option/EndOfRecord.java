package org.prelle.telnet.option;

/**
 * 
 */
public class EndOfRecord implements TelnetOption {

	//-------------------------------------------------------------------
	/**
	 */
	public EndOfRecord() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getOptionCode() {
		return 25;
	}

	@Override
	public String getName() {
		return "EOR";
	}
	
	//-----------------------------------------------------------------
	@Override
	public boolean startNegotiationAs(CommunicationRole role) {
		return false;
	}

}
