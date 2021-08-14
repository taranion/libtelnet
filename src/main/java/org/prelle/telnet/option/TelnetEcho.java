/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.WillVariable;

/**
 * RFC 857
 * @see http://tools.ietf.org/html/rfc857
 * @author prelle
 *
 */
public class TelnetEcho extends TelnetOption {

	public final static int    CODE = 1;
	public final static String NAME = "ECHO";

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

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#requestUsage(org.prelle.telnet.TelnetSocket)
	 */
	public void requestUsage(TelnetSocket nvt) throws IOException {
		logger.debug("Suggest "+getName());
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (!nvt.isInClientMode()) {
			nvt.getWillVariable(getCode()).setState(true);
			out.sendWill(getCode());
		} else {
			nvt.getDoVariable(getCode()).setState(true);
			out.sendDo(getCode());
		}
	}

	//-----------------------------------------------------------------
	public void requestStop(TelnetSocket nvt) throws IOException {
		logger.debug("Stop "+getName());
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (!nvt.isInClientMode()) {
			nvt.getWillVariable(getCode()).setState(false);
			out.sendWont(getCode());
		} else {
			nvt.getDoVariable(getCode()).setState(false);
			out.sendDont(getCode());
		}
	}

}
