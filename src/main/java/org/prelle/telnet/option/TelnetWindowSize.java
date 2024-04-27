/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOptions;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 1073
 * @author prelle
 *
 */
public class TelnetWindowSize extends TelnetOptionHandler {
	
	//-----------------------------------------------------------------
	public TelnetWindowSize(int code, String name) {
		super(code,name);
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
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
		in.setHigherLevelControl(true);
		// NAWS Sub negotiation
		int x1 = in.read();
		int x2 = in.read();
		int y1 = in.read();
		int y2 = in.read();
		int x = x1*256 + x2;
		int y = y1*256 + y2;
		logger.log(Level.INFO,"Terminal Width = "+ x+"*"+y);
		
//		in.readUntilSE();
		in.read();
		in.read();
		in.setHigherLevelControl(false);
		logger.log(Level.DEBUG,"Terminal Width done");
		
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

		TelnetOptionHandler.startSubNegotiation(sock, TelnetOptions.NAWS.getCode());
		out.write(x>>8);
		out.write(x%256);
		out.write(y>>8);
		out.write(y%256);
		TelnetOptionHandler.endSubNegotiation(sock, TelnetOptions.NAWS.getCode());
		out.flush();
	}
}