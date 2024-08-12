/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetServerSocket extends ServerSocket {

    private final static Logger logger = System.getLogger("telnet.lvl3");

	private Map<Integer, ControlCode> negotiate = new LinkedHashMap<>();

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port) throws IOException {
		super(port);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port, int backlog) throws IOException {
		super(port, backlog);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket support(int code, ControlCode willOrDo) {
//		if (willOrDo!=ControlCode.WILL && willOrDo!=ControlCode.DO)
//			throw new IllegalArgumentException("Only WILL or DO expected here");
		negotiate.put(code, willOrDo);
		return this;
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
		for (Entry<Integer, ControlCode> entry : negotiate.entrySet()) {
			ret.support(entry.getKey(), entry.getValue());
		}

		/*
		 * Send all by default enabled variables
		 */
		logger.log(Level.DEBUG,"Incoming connection from {0},Port {1} - now initialize options", ret.getInetAddress().getHostAddress(), ret.getPort());
		ret.out().logger = System.getLogger("telnet.lvl1.out."+ret.getInetAddress().getHostAddress());
		ret.in().logger = System.getLogger("telnet.lvl1.in."+ret.getInetAddress().getHostAddress());

		logger.log(Level.DEBUG,"LEAVE accept()");
		return ret;
	}
}
