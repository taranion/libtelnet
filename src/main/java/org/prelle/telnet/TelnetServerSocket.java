/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.prelle.telnet.TelnetConstants.ControlCode;

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

//	//-----------------------------------------------------------------
//	public TelnetServerSocket withNAWS() {
//		activelyRequest(TelnetOption.NAWS);
//		return this;
//	}
//
//	//-----------------------------------------------------------------
//	public TelnetServerSocket withMUDTerminalTypeStandard() {
//		activelyRequest(TelnetOption.MTT);
//		return this;
//	}
//

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.net.ServerSocket#accept()
	 */
	@Override
	public Socket accept() throws IOException {
		TelnetSocket ret = new TelnetSocket();
		logger.log(Level.DEBUG,"Waiting for new connections");
		implAccept(ret);
//		ret.setTcpNoDelay(true);

		/*
		 * Send all by default enabled variables
		 */
		logger.log(Level.DEBUG,"Incoming connection from {0},Port {1}", ret.getInetAddress().getHostAddress(), ret.getPort());
		ret.in().logger = System.getLogger("telnet.lvl1.in."+ret.getInetAddress().getHostAddress());
		ret.out().logger = System.getLogger("telnet.lvl1.out."+ret.getInetAddress().getHostAddress());

		logger.log(Level.DEBUG,"LEAVE accept()");
		return ret;
	}
}
