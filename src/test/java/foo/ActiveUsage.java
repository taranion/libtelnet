/**
 * 
 */
package foo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetConfiguration;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.mud.GenericMUDCommunicationProtocol;
import org.prelle.telnet.mud.MUDExtensionProtocol;
import org.prelle.telnet.mud.MUDServerDataProtocol;
import org.prelle.telnet.mud.MUDServerStatusProtocol;
import org.prelle.telnet.mud.MUDSoundProtocol;
import org.prelle.telnet.mud.MUDTerminalTypeStandard;
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
		TelnetConfiguration.registerOption(new MUDExtensionProtocol());
		TelnetConfiguration.registerOption(new MUDServerDataProtocol());
		TelnetConfiguration.registerOption(new GenericMUDCommunicationProtocol());

		TelnetSocket socket = new TelnetSocket("rom.mud.de", 4000);
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
			TelnetOption option, Object data) {
		logger.log(Level.INFO,"Telnet Option Data Changed: "+option.getClass().getSimpleName()+" = "+data);
		
		switch (option.getCode()) {
		case MUDTerminalTypeStandard.CODE:
		}
	}
}
