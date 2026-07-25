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

import org.prelle.telnet.option.LineMode;
import org.prelle.telnet.option.LineMode.LineModeListener;
import org.prelle.telnet.option.TelnetCharset;
import org.prelle.telnet.option.TelnetCharset.CharsetListener;
import org.prelle.telnet.option.TelnetEnvironmentOption;
import org.prelle.telnet.option.TelnetEnvironmentOption.EnvironmentListener;
import org.prelle.telnet.option.TerminalType;
import org.prelle.telnet.option.TerminalType.TerminalTypeListener;

/**
 * @author prelle
 *
 */
public class PassiveUsage implements Runnable, TelnetConstants {

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
		serverSocket = new TelnetServerSocket(4000);
		serverSocket.setExtensionFactory( (socket) -> {
			return new TelnetOption[] { 
					new TerminalType((TerminalTypeListener)null), 
					new LineMode((LineModeListener)null) , 
					new TelnetCharset((CharsetListener)null, "UTF-8","CP437","ISO-8859-1","ASCII"), 
					new TelnetEnvironmentOption((EnvironmentListener) (variables) -> {
						logger.log(Level.INFO,"Environment variables received: {0}", variables);
					}) 
				};
		});

		thread = new Thread(this,"ThreadCheck");
		thread.start();
		System.getLogger("app").log(Level.INFO, "Waiting on port 4000");
	}

	//-----------------------------------------------------------------
	public void run() {
		while (true) {
			try {
				TelnetSocket socket = (TelnetSocket) serverSocket.accept();
				socket.addListener(  new TelnetListener() {
					@Override
					public void optionStateChanged(TelnetOption extension, boolean state) {
						logger.log(Level.INFO,"Extension {0} changed to {1}", extension.getName(), state);
					}

					@Override
					public void telnetCommandReceived(TelnetCommand command) {
						logger.log(Level.INFO,"Telnet command received: {0}", command);
					}

					@Override
					public void telnetReady() {
						logger.log(Level.INFO,"telnetReady");
					}});
				
				InputStream in = socket.getInputStream();

				OutputStream out = socket.getOutputStream();
				logger.log(Level.DEBUG,"Incoming connection via "+in.getClass()+" and output via "+out);
				PrintWriter pw = new PrintWriter(out);
				pw.print("Wie heisst Du? ");
//				socket.requestEcho();
				pw.flush();

				int data = -1;
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

}
