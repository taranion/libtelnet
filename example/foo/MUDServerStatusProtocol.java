/**
 * 
 */
package foo;

import java.io.IOException;
import java.io.InputStream;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.NetworkVirtualConsole;
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
	 * @see org.prelle.telnet.option.TelnetOption#setDefaults(org.prelle.telnet.NetworkVirtualConsole)
	 */
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
		requestUsage(console);
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
