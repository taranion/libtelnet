/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetSocket;
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
	public void setDefaults(TelnetSocket nvt) {
		nvt.setOptionVariable(new WillVariable(CODE, false));
		nvt.setOptionVariable(new DoVariable(CODE, false));
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
	 * @see org.prelle.telnet.option.TelnetOption#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		// Not sending and not awaiting echo
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
			throws IOException {
	}

}
