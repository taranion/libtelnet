/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.prelle.telnet.option.LineMode;
import org.prelle.telnet.option.SuppressGoAhead;
import org.prelle.telnet.option.TelnetEcho;
import org.prelle.telnet.option.TelnetWindowSize;
import org.prelle.telnet.option.TerminalType;
import org.prelle.telnet.option.TimingMark;
import org.prelle.telnet.option.TransmitBinary;

/**
 * @author prelle
 *
 */
public class TelnetSocket extends Thread {

    private final static Logger logger = Logger.getLogger("telnet");

	private ServerSocket incoming;
	private TelnetListener listener;
	private NetworkVirtualConsoleListener nvtListener;
	
	//-----------------------------------------------------------------
	public TelnetSocket(ServerSocket socket, TelnetListener list, NetworkVirtualConsoleListener nvtList) {
		super("TelnetSocket");
		this.incoming = socket;
		this.start();
		this.listener = list;
		this.nvtListener = nvtList;
		
		NetworkVirtualConsole.registerOption(new TransmitBinary());
		NetworkVirtualConsole.registerOption(new TelnetEcho());
		NetworkVirtualConsole.registerOption(new TelnetWindowSize());
		NetworkVirtualConsole.registerOption(new TimingMark());
		NetworkVirtualConsole.registerOption(new TerminalType());
//		NetworkVirtualConsole.registerOption(new LineMode());
		NetworkVirtualConsole.registerOption(new SuppressGoAhead());
//		NetworkVirtualConsole.registerOption(new CarriageReturnDisposition());
	}
	
	//---------------------------------------------------------------
	/**
	 * This method implements the mainloop of the factory. It is 
	 * called by the NetCenter to start this factory listening and
	 * producing.
	 */
	public void run() {
		do {
			try {
				// Threadgroup ?
				Socket socket = incoming.accept();
				logger.info("Neue Verbindung von "+socket.getInetAddress().getHostAddress());
				NetworkVirtualConsole console = new NetworkVirtualConsole(socket, false, nvtListener);
				listener.incomingConnection(console);
			} catch (IOException ioe) {
				logger.error(ioe.toString());
			}
		} while (!isInterrupted());
	}
	
}
