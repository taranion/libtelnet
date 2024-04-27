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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author prelle
 *
 */
public class TelnetServerSocket extends ServerSocket {

    private final static Logger logger = System.getLogger("telnet.lvl3");
    
    /** Send DO requests for these options on incoming connections */
    private List<TelnetOptions> activelyRequest = new ArrayList<>();
    /** */
    private List<TelnetOptions> passiveSupport = new ArrayList<>();

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port) throws IOException {
		super(port);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port, int backlog) throws IOException {
		super(port, backlog);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket activelyRequest(TelnetOptions value) {
		activelyRequest.add(value);
		logger.log(Level.DEBUG, "I will ask clients to perform {0} ({1})", value.name(), value.getCode());
		return this;
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket passivelySupport(TelnetOptions value) {
		passiveSupport.add(value);
		logger.log(Level.DEBUG, "I will tell clients that I support {0} ({1})", value.name(), value.getCode());
		return this;
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket withNAWS() {
		activelyRequest(TelnetOptions.NAWS);
		return this;
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket withMUDTerminalTypeStandard() {
		activelyRequest(TelnetOptions.MTT);
		return this;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.net.ServerSocket#accept()
	 */
	@Override
	public Socket accept() throws IOException {
		TelnetSocket ret = new TelnetSocket(activelyRequest, passiveSupport);
		logger.log(Level.DEBUG,"Waiting for new connections");
		implAccept(ret);

		logger.log(Level.DEBUG, "Actively request: "+activelyRequest);
		logger.log(Level.DEBUG, "Passive support : "+passiveSupport);
		
		/*
		 * Send all by default enabled variables
		 */
		logger.log(Level.DEBUG,"Incoming connection from {0},Port {1} - now initialize options", ret.getInetAddress().getHostAddress(), ret.getPort());
		for (TelnetOptions option : activelyRequest) {
			logger.log(Level.DEBUG,"..Request "+option.name());
			option.getOptionHandler().requestUsage(ret);
		}
		for (TelnetOptions option : passiveSupport) {
			logger.log(Level.DEBUG,"..Indicate "+option.name());
			option.getOptionHandler().indicateSupport(ret);
		}
//		for (TelnetOption option : TelnetConfiguration.getKnownOptions()) {
//			logger.log(Level.DEBUG,"..Initialize "+option.getName());
//			option.initialize(ret);
//		}
		
		logger.log(Level.DEBUG,"LEAVE accept()");
		return ret;
	}
}
