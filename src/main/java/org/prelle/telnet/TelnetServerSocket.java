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
import java.util.stream.Collectors;

import org.prelle.telnet.TelnetSocket.OptionEntry;

/**
 * @author prelle
 *
 */
public class TelnetServerSocket extends ServerSocket {

    private final static Logger logger = System.getLogger("telnet.lvl3");

    private List<OptionEntry> options = new ArrayList<>();

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port) throws IOException {
		super(port);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port, int backlog) throws IOException {
		super(port, backlog);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket support(TelnetOptionHandler value, Role role) {
		options.add(new OptionEntry(value, role));
		if (role==Role.REQUESTER) {
			logger.log(Level.DEBUG, "I will ask clients to perform {0} ({1})", value.getName(), value.getCode());
		} else if (role==Role.PROVIDER){
			logger.log(Level.DEBUG, "I will tell clients that I support {0} ({1})", value.name, value.getCode());
		} else {
			logger.log(Level.DEBUG, "I will tell clients that I support {0} ({1}), but only if asked", value.name, value.getCode());
		}

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
		TelnetSocket ret = new TelnetSocket(options);
		logger.log(Level.DEBUG,"Waiting for new connections");
		implAccept(ret);

		logger.log(Level.DEBUG, "Actively request: {0},"+options.stream().filter(opt -> opt.role==Role.REQUESTER).collect(Collectors.toList()));
		logger.log(Level.DEBUG, "Passive support : {0},"+options.stream().filter(opt -> opt.role==Role.PROVIDER).collect(Collectors.toList()));

		/*
		 * Send all by default enabled variables
		 */
		logger.log(Level.DEBUG,"Incoming connection from {0},Port {1} - now initialize options", ret.getInetAddress().getHostAddress(), ret.getPort());
		ret.initialize();

		logger.log(Level.DEBUG,"LEAVE accept()");
		return ret;
	}
}
