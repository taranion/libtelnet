/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;

import org.prelle.telnet.option.LineMode;
import org.prelle.telnet.option.SuppressGoAhead;
import org.prelle.telnet.option.TelnetEcho;

/**
 * @author prelle
 *
 */
public class PassiveUsage implements Runnable, TelnetOptionListener {

    private final static Logger logger = System.getLogger("app");

	private Thread thread;
	private TelnetServerSocket serverSocket;

	//-----------------------------------------------------------------
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new PassiveUsage();
//		new ActiveUsage();
	}

	public PassiveUsage() throws IOException {
		serverSocket = new TelnetServerSocket(4000)
				.support(new SuppressGoAhead(), Role.PROVIDER)
				.support(new TelnetEcho(), Role.PROVIDER)
//				.passivelySupport(TelnetOptions.ECHO)
//				.passivelySupport(TelnetOptions.EOR)
//				.passivelySupport(TelnetOptions.MSP)
//				.withMUDTerminalTypeStandard()
//				.withNAWS()
				;


		thread = new Thread(this,"ThreadCheck");
		thread.start();
		System.getLogger("app").log(Level.INFO, "Waiting on port 4000");
	}

	//-----------------------------------------------------------------
	public void run() {
		while (true) {
			try {
				Socket vanillaSocket = serverSocket.accept();
				TelnetSocket socket = (TelnetSocket)vanillaSocket;
				socket.addOptionListener(this);

				OutputStream out = socket.getOutputStream();
				logger.log(Level.DEBUG,"Incoming connection via "+socket.getClass()+" and output via "+out);
				PrintWriter pw = new PrintWriter(out);
				pw.print("Wie heisst Du? ");
//				socket.requestEcho();
				pw.flush();

				int data = -1;
				InputStream in = socket.getInputStream();
				do {
					data = in.read();
					System.out.println("Read "+data+" as "+(char)data);
//					if (socket.isFeatureActive(TelnetOption.ECHO)) {
						out.write(data);
//					}
				} while (data!=-1);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			for (NetThread pThread: new ArrayList<NetThread>(playerThreads)) {
//				if (!pThread.isAlive()) {
//					logger.log(Level.DEBUG,"Removing thread "+pThread);
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
			TelnetOptionHandler option, Object data) {
		logger.log(Level.INFO,"Telnet Option Data Changed: "+option.getClass().getSimpleName()+" = "+data);

	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionListener#telnetOptionStatusChange(org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOptionHandler, boolean)
	 */
	@Override
	public void telnetOptionStatusChange(TelnetSocket nvt, TelnetOptionHandler option, boolean active) {
		logger.log(Level.INFO, "Feature {0} is {1}", option.getName(), active?"enabled":"disabled");
	}

}
