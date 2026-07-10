/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetOption;

/**
 * https://tintin.mudhalla.net/protocols/mssp/
 * @author prelle
 *
 */
public class MUDServerStatusProtocol implements TelnetOption<MUDServerStatusProtocol.MSSPListener> {

	protected final static Logger logger = System.getLogger("telnet.option.mssp");

	public static interface MSSPListener extends TelnetOptionListener {
		public void telnetMSSPDataReceived(Map<String,String> data);
	}
	
	public final static int CODE = 70;

	private final static int MSSP_VAR = 1;
	private final static int MSSP_VAL = 2;

	private Supplier<Map<String,String>> dataSupplier;
	private List<MSSPListener> listeners = new ArrayList<>();
	
	//-------------------------------------------------------------------
	public MUDServerStatusProtocol(Supplier<Map<String,String>> dataSupplier) {
		this.dataSupplier = dataSupplier;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}
	@Override
	public String getName() { return "MSSP";}


	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		logger.log(Level.ERROR, "Subnegotiate for MSSP: "+Arrays.toString(values));
		int operation = values[0];
		
		System.exit(1);
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 */
	public boolean negotiateDetails(TelnetProtocol stack) {
		Map<String,String> data = dataSupplier.get();
		StringBuilder buf = new StringBuilder();
		for (Entry<String,String> entry : data.entrySet()) {
			buf.append((char)MSSP_VAR);
			buf.append(entry.getKey());
			buf.append((char)MSSP_VAL);
			buf.append(entry.getValue());
		}
		System.out.println("Send "+buf);
		try {
			stack.getOutputStream().sendSubNegotiation(CODE, buf.toString());
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed sending telnet option",e);
		}
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(MSSPListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

}
