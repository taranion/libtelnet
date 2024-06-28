/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.mud.MUDTerminalTypeStandard;
import org.prelle.telnet.option.TerminalType;

/**
 * @author prelle
 *
 */
public class ActiveUsage implements TelnetOptionListener {

    private final static Logger logger = System.getLogger("app");

	private Thread thread;

	//-----------------------------------------------------------------
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new ActiveUsage();
	}

	public ActiveUsage() throws IOException {
//		TelnetSocket socket = new TelnetSocket("rom.mud.de", 4000)
		TelnetSocket socket = new TelnetSocket("mg.mud.de", 4711)
//		TelnetSocket socket = new TelnetSocket("lost.wishes.net", 5555)
				.support(new TelnetOptionHandler(0,"TRANSMIT_BINARY"), Role.REQUESTER)
				.support(new TelnetOptionHandler(1,"ECHO"), Role.REQUESTER)
				.support(new TerminalType("ActiveUsage","XTERM","MTTS 0"), Role.PROVIDER)
				;
		InputStream in = socket.getInputStream();
		while (true) {
			int foo = in.read();
			System.out.print((char)foo);
			if (foo==-1)
				break;
			System.out.flush();
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
}
