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
public class TelnetWindowSize extends TelnetOption {

	private final static int    CODE = 31;
	private final static String NAME = "NAWS";

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetWindowSize() {
		// TODO Auto-generated constructor stub
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#setDefaults(org.prelle.telnet.NetworkVirtualConsole)
	 */
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
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#performSubNegotiation(org.prelle.telnet.NetworkVirtualConsole, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(NetworkVirtualConsole nvt, InputStream in) throws IOException {
		// NAWS Sub negotiation
		int x1 = in.read();
		int x2 = in.read();
		int y1 = in.read();
		int y2 = in.read();
		int x = x1*256 + x2;
		int y = y1*256 + y2;
		logger.info("Terminal Width = "+ x+"*"+y);
		nvt.setWindowSize(new int[]{x,y});
		
		in.read(); // IAC
		in.read(); // SE
		
		nvt.getListener().windowSizeDetermined(nvt, x, y);
	}

}
