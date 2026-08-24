/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.prelle.telnet.option.TelnetOption;

/**
 * @author prelle
 *
 */
public class TelnetServerSocket extends ServerSocket {

    private final static Logger logger = System.getLogger("telnet.lvl3");
    
    private Function<TelnetSocket, List<TelnetOption>> configFactory;
    private Function<TelnetSocket, TelnetSocketListener> listenerFactory;

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port) throws IOException {
		super(port);
	}

	//-----------------------------------------------------------------
	public TelnetServerSocket(int port, int backlog) throws IOException {
		super(port, backlog);
	}

	//-----------------------------------------------------------------
	public void setExtensionFactory(Function<TelnetSocket, List<TelnetOption>> configFactory) {
		this.configFactory = configFactory;
	}

	//-----------------------------------------------------------------
	public void setListenerFactory(Function<TelnetSocket, TelnetSocketListener> listenerFactory) {
		this.listenerFactory = listenerFactory;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.net.ServerSocket#accept()
	 */
	@Override
	public Socket accept() throws IOException {
		TelnetSocket ret = new TelnetSocket();
		logger.log(Level.DEBUG,"Waiting for new connections");

		// Call an eventually existing supplier for extensions
		if (configFactory != null) {
			for (TelnetOption extension : configFactory.apply(ret)) {
				ret.getStack().add(extension);
			}
		}

		implAccept(ret);
//		ret.setTcpNoDelay(true);

		// Call an eventually existing supplier for listeners
		if (listenerFactory != null) {
			ret.addListener(listenerFactory.apply(ret));
		}
		/*
		 * Send all by default enabled variables
		 */
		logger.log(Level.DEBUG,"Incoming connection from {0},Port {1}", ret.getInetAddress().getHostAddress(), ret.getPort());
		((TelnetOutputStream)ret.out()).logger = System.getLogger("telnet.lvl1.out."+ret.getInetAddress().getHostAddress());
		ret.in().logger = System.getLogger("telnet.lvl1.in."+ret.getInetAddress().getHostAddress());
//		ret.negotiateOptionsAsync();
//		ret.in.startReadingFromSocket();
		
		logger.log(Level.DEBUG,"LEAVE accept()");
		return ret;
	}
}
