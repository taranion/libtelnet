/**
 *
 */
package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 * https://tintin.mudhalla.net/protocols/mssp/
 * @author prelle
 *
 */
public class MUDServerStatusProtocol extends TelnetSubnegotiationHandler {

	protected final static Logger logger = System.getLogger("telnet.option.mssp");

	public final static int CODE = 70;

	private final static int MSSP_VAR = 1;
	private final static int MSSP_VAL = 2;

	private Supplier<Map<String,String>> dataSupplier;
	
	//-------------------------------------------------------------------
	public MUDServerStatusProtocol(Supplier<Map<String,String>> dataSupplier) {
		this.dataSupplier = dataSupplier;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.ERROR, "Subnegotiate for MSSP: "+Arrays.toString(values));
		int operation = values[0];
		
		System.exit(1);
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 */
	public boolean initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.ERROR, "TODO: initializeAs "+role);
		switch (role) {
		case SERVER:
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
				out.sendSubNegotiation(CODE, buf.toString());
			} catch (IOException e) {
				logger.log(Level.WARNING, "Failed sending telnet option",e);
			}
			break;
		}
		return false;
	}

}
