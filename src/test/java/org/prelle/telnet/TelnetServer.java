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

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.event.internal.TelnetCommandImpl;
import org.prelle.telnet.option.LineMode;
import org.prelle.telnet.option.LineMode.LineModeListener;
import org.prelle.telnet.option.TelnetCharset;
import org.prelle.telnet.option.TelnetEnvironmentOption;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.option.TelnetWindowSize;
import org.prelle.telnet.option.TerminalType;
import org.prelle.telnet.parser.TelnetConstants;

/**
 * @author prelle
 *
 */
public class TelnetServer implements Runnable, TelnetConstants {

    private final static Logger logger = System.getLogger("app");

	private Thread thread;
	private TelnetServerSocket serverSocket;

	//-----------------------------------------------------------------
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new TelnetServer();
//		new ActiveUsage();
	}

	public TelnetServer() throws IOException {
		serverSocket = new TelnetServerSocket(4000);
		serverSocket.setExtensionFactory( (socket) -> {
			logger.log(Level.INFO,"Creating extensions for socket "+socket);
			return new TelnetOption[] { 
					new TerminalType(), 
					new LineMode() , 
					new TelnetCharset("UTF-8","CP437","ISO-8859-1","ASCII"), 
					new TelnetEnvironmentOption() ,
					new TelnetWindowSize()
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
				socket.addListener(  new TelnetSocketListener() {
					@Override
					public void optionStateChanged(TelnetOption extension, boolean state) {
						logger.log(Level.INFO,"Extension {0} changed to {1}", extension.getName(), state);
					}

					@Override
					public void onTelnetEvent(TelnetEvent command) {
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
					System.err.println("Read "+data+" as "+(char)data);
//					if (socket.isFeatureActive(TelnetOption.ECHO)) {
//						out.write(data);
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
