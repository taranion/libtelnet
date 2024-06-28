package org.prelle.telnet;

/**
 *
 */
public class TelnetOptionLogic {

	//-------------------------------------------------------------------
	/**
	 */
	public TelnetOptionLogic() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public boolean doWeSupport(int option) {
		if (option==0)
			return true;
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * A DO has been received
	 * @param option
	 * @param out
	 */
	public void remotePartyRequestsThatWeStart(int option, TelnetOutputStream out) {

	}

}
