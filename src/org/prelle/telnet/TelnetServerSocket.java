/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.prelle.telnet.option.TelnetOption;

/**
 * @author prelle
 *
 */
public class TelnetServerSocket extends ServerSocket {

    private final static Logger logger = Logger.getLogger("telnet.lvl3");

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
		logger.debug("Waiting for new connections");
		implAccept(ret);

		/*
		 * Send all by default enabled variables
		 */
		logger.debug("Initialize options");
		for (TelnetOption option : TelnetConfiguration.getKnownOptions()) {
			logger.debug("Initialize "+option.getName());
			option.initialize(ret);
		}
		
		return ret;
	}
}
