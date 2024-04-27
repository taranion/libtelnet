/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * https://www.ietf.org/proceedings/44/I-D/draft-ietf-tn3270e-telnet-tls-01.txt
 * @author prelle
 *
 */
public class TelnetTLS extends TelnetOptionHandler {

	public TelnetTLS() {
		super(46, "STARTTLS");
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
			throws IOException {
	}

}
