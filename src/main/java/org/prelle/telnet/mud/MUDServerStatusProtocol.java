/**
 * 
 */
package org.prelle.telnet.mud;

import java.io.IOException;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.WillVariable;
import org.prelle.telnet.option.TelnetOption;

/**
 * See http://tintin.sourceforge.net/mssp/
 * @see http://tintin.sourceforge.net/mssp/
 * @author prelle
 *
 */
public class MUDServerStatusProtocol extends TelnetOption {

	private final static int    CODE = 70;
	private final static String NAME = "MSSP";
	
	private final static int MSSP_VAR = 1;
	private final static int MSSP_VAL = 2;

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#setDefaults(org.prelle.telnet.TelnetSocket)
	 */
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
		requestUsage(console);
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
