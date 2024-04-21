/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;

import org.prelle.telnet.option.TelnetOption;

/**
 * @author prelle
 *
 */
public class TelnetServerSocket extends ServerSocket {

    private final static Logger logger = System.getLogger("telnet.lvl3");

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port) throws IOException {
		super(port);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port, int backlog) throws IOException {
		super(port, backlog);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.net.ServerSocket#accept()
	 */
	@Override
	public Socket accept() throws IOException {
		TelnetSocket ret = new TelnetSocket();
		logger.log(Level.DEBUG,"Waiting for new connections");
		implAccept(ret);

		/*
		 * Send all by default enabled variables
		 */
		logger.log(Level.DEBUG,"Initialize options");
		for (TelnetOption option : TelnetConfiguration.getKnownOptions()) {
			logger.log(Level.DEBUG,"Initialize "+option.getName());
			option.initialize(ret);
		}
		
		return ret;
	}
}
