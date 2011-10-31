/**
 * 
 */
package foo;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.prelle.telnet.NetworkVirtualConsole;
import org.prelle.telnet.NetworkVirtualConsoleListener;
import org.prelle.telnet.TelnetListener;
import org.prelle.telnet.TelnetSocket;

/**
 * @author prelle
 *
 */
public class PassiveUsage implements TelnetListener, NetworkVirtualConsoleListener, Runnable {

    private final static Logger logger = Logger.getLogger("app");
	
	private Thread thread;
	private List<NetThread> playerThreads;

	//-----------------------------------------------------------------
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new PassiveUsage();
	}

	public PassiveUsage() throws IOException {
		PropertyConfigurator.configure("log4j.properties");
		playerThreads = new ArrayList<NetThread>();
		ServerSocket inc = new ServerSocket(4000);
		new TelnetSocket(inc, this, this);
		
		NetworkVirtualConsole.registerOption(new MUDServerStatusProtocol());
		NetworkVirtualConsole.registerOption(new MUDTerminalTypeStandard());
		NetworkVirtualConsole.registerOption(new MUDSoundProtocol());
		
		thread = new Thread(this,"ThreadCheck");
		thread.start();
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetListener#incomingConnection(org.prelle.telnet.NetworkVirtualConsole)
	 */
	@Override
	public void incomingConnection(NetworkVirtualConsole console) {
		logger.debug("inc: "+console);
		NetThread thread = new NetThread(console);
		playerThreads.add(thread);
		thread.start();
		try {
			console.requestEcho();
			console.sendText("Name: ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-----------------------------------------------------------------
	public void run() {
		while (true) {
			for (NetThread pThread: new ArrayList<NetThread>(playerThreads)) {
				if (!pThread.isAlive()) {
					logger.debug("Removing thread "+pThread);
					playerThreads.remove(pThread);
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.NetworkVirtualConsoleListener#windowSizeDetermined(org.prelle.telnet.NetworkVirtualConsole, int, int)
	 */
	@Override
	public void windowSizeDetermined(NetworkVirtualConsole console, int width,
			int height) {
		logger.info("Terminal size = "+width+"x"+height);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.NetworkVirtualConsoleListener#terminalTypeDetermined(org.prelle.telnet.NetworkVirtualConsole, java.lang.String)
	 */
	@Override
	public void terminalTypeDetermined(NetworkVirtualConsole console,
			String termType) {
		logger.info("Terminal type = "+termType);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.NetworkVirtualConsoleListener#interruptProcessRequested(org.prelle.telnet.NetworkVirtualConsole)
	 */
	@Override
	public void interruptProcessRequested(NetworkVirtualConsole console) {
		logger.info("interrupt requested");
		console.close();
	}
}
