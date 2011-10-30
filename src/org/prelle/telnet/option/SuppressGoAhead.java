/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.InputStream;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.NetworkVirtualConsole;
import org.prelle.telnet.WillVariable;

/**
 * @author prelle
 *
 */
public class SuppressGoAhead extends TelnetOption {

	public final static int    CODE = 3;
	public final static String NAME = "SGA";

	//-----------------------------------------------------------------
	@Override
	public void setDefaults(NetworkVirtualConsole nvt) {
		nvt.setOptionVariable(new WillVariable(NAME, false));
		nvt.setOptionVariable(new DoVariable(NAME, false));
	}
	
	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getCode()
	 */
	@Override
	public int getCode() {
		return CODE;
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#initialize(org.prelle.telnet.NetworkVirtualConsole)
	 */
	@Override
	public void initialize(NetworkVirtualConsole console) throws IOException {
		// Not sending and not awaiting echo
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#performSubNegotiation(org.prelle.telnet.NetworkVirtualConsole, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(NetworkVirtualConsole nvt, InputStream in)
			throws IOException {
	}

}
