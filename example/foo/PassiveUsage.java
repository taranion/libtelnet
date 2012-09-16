/**
 * 
 */
package foo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.prelle.telnet.TelnetConfiguration;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetServerSocket;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.option.SuppressGoAhead;
import org.prelle.telnet.option.TelnetEcho;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.option.TelnetWindowSize;
import org.prelle.telnet.option.TerminalType;
import org.prelle.telnet.option.TimingMark;
import org.prelle.telnet.option.TransmitBinary;

/**
 * @author prelle
 *
 */
public class PassiveUsage implements Runnable, TelnetOptionListener {

    private final static Logger logger = Logger.getLogger("app");
	
	private Thread thread;
	private ServerSocket serverSocket;

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
		serverSocket = new TelnetServerSocket(4000);
//		new NetworkVirtualConsole(inc, this, this);
		
		TelnetConfiguration.registerOption(new TransmitBinary());
		TelnetConfiguration.registerOption(new TelnetEcho());
		TelnetConfiguration.registerOption(new TelnetWindowSize());
		TelnetConfiguration.registerOption(new TimingMark());
		TelnetConfiguration.registerOption(new TerminalType());
//		TelnetConfiguration.registerOption(new LineMode());
		TelnetConfiguration.registerOption(new SuppressGoAhead());
//		TelnetConfiguration.registerOption(new CarriageReturnDisposition());
		TelnetConfiguration.registerOption(new MUDServerStatusProtocol());
		TelnetConfiguration.registerOption(new MUDTerminalTypeStandard());
		TelnetConfiguration.registerOption(new MUDSoundProtocol());
		TelnetConfiguration.registerOption(new MUDServerDataProtocol());
		
		thread = new Thread(this,"ThreadCheck");
		thread.start();
	}

	//-----------------------------------------------------------------
	public void run() {
		while (true) {
			try {
				Socket vanillaSocket = serverSocket.accept();
				TelnetSocket socket = (TelnetSocket)vanillaSocket;
				
				OutputStream out = socket.getOutputStream();
				logger.debug("Incoming connection via "+socket.getClass()+" and output via "+out);
				PrintWriter pw = new PrintWriter(out);
				pw.print("Wie heisst Du? ");
				socket.requestEcho();
				pw.flush();
				
				int data = -1;
				InputStream in = socket.getInputStream();
				do {
					data = in.read();					
				} while (data!=-1);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			for (NetThread pThread: new ArrayList<NetThread>(playerThreads)) {
//				if (!pThread.isAlive()) {
//					logger.debug("Removing thread "+pThread);
//					playerThreads.remove(pThread);
//				}
//			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetOptionListener#telnetOptionDataChanged(org.prelle.telnet.NetworkVirtualConsole, org.prelle.telnet.option.TelnetOption, java.lang.Object)
	 */
	@Override
	public void telnetOptionDataChanged(TelnetSocket nvt,
			TelnetOption option, Object data) {
		logger.info("Telnet Option Data Changed: "+option.getClass().getSimpleName()+" = "+data);
		
		switch (option.getCode()) {
		case MUDTerminalTypeStandard.CODE:
		}
	}
}
