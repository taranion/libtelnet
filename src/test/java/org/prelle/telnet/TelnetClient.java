/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.mud.AardwolfMushclientProtocol;
import org.prelle.telnet.option.EndOfRecord;
import org.prelle.telnet.option.LineMode;
import org.prelle.telnet.option.TelnetCharset;
import org.prelle.telnet.option.TelnetEnvironmentOption;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.option.TerminalType;

/**
 * @author prelle
 *
 */
public class TelnetClient implements TelnetSocketListener {

    private final static Logger logger = System.getLogger("app");

	//-----------------------------------------------------------------
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new TelnetClient();
	}

	public TelnetClient() throws IOException {
//		TelnetSocket socket = new TelnetSocket("rom.mud.de", 4000);
//		TelnetSocket socket = new TelnetSocket("mg.mud.de", 4711);
//		TelnetSocket socket = new TelnetSocket("lost.wishes.net", 5555);
//		TelnetSocket socket = new TelnetSocket("windmud.web-games.net", 4040);
//		TelnetSocket socket = new TelnetSocket("bat.org", 23)
		TelnetSocket socket = new TelnetSocket("eden-test.rpgframework.de", 4000);
		socket.addListener(this);
		socket.negotiateOptionsAsync(
				new TerminalType(),
				new LineMode(),
				new EndOfRecord(),
				new AardwolfMushclientProtocol(),
				new TelnetCharset(null, "UTF-8","ISO-8859-1","CP837","ASCII"),
				new TelnetEnvironmentOption(Map.of("USER","prelle","PASSWORD","geheim"), Map.of("OS","Linux"))
				);
		InputStream in = socket.getInputStream();
		System.out.println("Start reading data from telnet server\n\n");
		while (true) {
			int foo = in.read();
			System.out.print((char)foo);
			if (foo==-1)
				break;
			System.out.flush();
		}
	}

	@Override
	public void onTelnetEvent(TelnetEvent event) {
		logger.log(Level.INFO,"Telnet event: "+event);
	}

	@Override
	public void optionStateChanged(TelnetOption extension, boolean active) {
		logger.log(Level.INFO,"Option "+extension.getName()+" is now "+(active?"active":"inactive"));
	}

	@Override
	public void telnetReady() {
		logger.log(Level.INFO,"Telnet is ready");
	}

}
