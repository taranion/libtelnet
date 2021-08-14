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
	 * @see org.prelle.telnet.option.TelnetOption#setDefaults(org.prelle.telnet.TelnetSocket)
	 */
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
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
		in.setHigherLevelControl(true);
		// NAWS Sub negotiation
		int x1 = in.read();
		int x2 = in.read();
		int y1 = in.read();
		int y2 = in.read();
		int x = x1*256 + x2;
		int y = y1*256 + y2;
		logger.info("Terminal Width = "+ x+"*"+y);
		
//		in.readUntilSE();
		in.read();
		in.read();
		in.setHigherLevelControl(false);
		logger.debug("Terminal Width done");
		
		nvt.fireOptionDataChanged(this, new TelnetWindowSizeData(x, y));
	}

	//-----------------------------------------------------------------
	/**
	 * Inform remote party of the current window size
	 * @param sock
	 * @param x
	 * @param y
	 * @throws IOException
	 */
	public static void sendNewSize(TelnetSocket sock, int x, int y) throws IOException {
		if (x>65535)
			throw new IllegalArgumentException("X to big");
		if (y>65535)
			throw new IllegalArgumentException("Y to big");
		
		TelnetOutputStream out = (TelnetOutputStream) sock.getOutputStream();

		TelnetOption.startSubNegotiation(sock, CODE);
		out.write(x>>8);
		out.write(x%256);
		out.write(y>>8);
		out.write(y%256);
		TelnetOption.endSubNegotiation(sock, CODE);
		out.flush();
	}
}