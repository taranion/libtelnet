/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.WillVariable;

/**
 * @author prelle
 *
 */
public class CarriageReturnDisposition extends TelnetOption {

	private final static int    CODE = 10;
	private final static String NAME = "NOACRD";

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
	 * @throws IOException 
	 * @see org.prelle.telnet.option.TelnetOption#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
	}

}
