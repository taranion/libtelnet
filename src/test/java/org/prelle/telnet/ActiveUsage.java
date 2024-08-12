/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import org.prelle.telnet.TelnetConstants.ControlCode;
import org.prelle.telnet.TelnetSocket.State;
import org.prelle.telnet.option.LineMode.LineModeConfig;
import org.prelle.telnet.option.LineMode.LineModeListener;
import org.prelle.telnet.option.LineMode.ModeBit;

/**
 * @author prelle
 *
 */
public class ActiveUsage implements TelnetSocketListener, LineModeListener {

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
//		TelnetSocket socket = new TelnetSocket("mg.mud.de", 4711)
//		TelnetSocket socket = new TelnetSocket("lost.wishes.net", 5555)
//		TelnetSocket socket = new TelnetSocket("backrooms.net", 4000)
		TelnetSocket socket = new TelnetSocket("bat.org", 23)
				.support(TelnetOption.LINEMODE.getCode(), ControlCode.WILL, new LineModeConfig())
				.support(TelnetOption.NAWS.getCode(), ControlCode.WILL)
				.support(TelnetOption.GMCP.getCode(), ControlCode.WILL)
//				.support(new TelnetOptionHandler(0,"TRANSMIT_BINARY"), Role.REQUESTER)
//				.support(new TelnetOptionHandler(1,"ECHO"), Role.REQUESTER)
//				.support(new SuppressGoAhead(), Role.REQUESTER)
//				.support(new TelnetWindowSize(), Role.REJECT_OUTRIGHT)
//				.support(new TerminalType("ActiveUsage","XTERM","MTTS 0"), Role.PROVIDER)
//				.support(new MUDSoundProtocol(), Role.PROVIDER_SILENT)
//				.support(new MUDServerDataProtocol(), Role.REQUESTER)
//				.support(new MUDServerStatusProtocol(), Role.REQUESTER)
//				.support(new GenericMUDCommunicationProtocol(), Role.REQUESTER)
				.setOptionListener(TelnetOption.LINEMODE.getCode(), (LineModeListener)this)
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

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSocketListener#telnetOptionStatusChange(org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetSubnegotiationHandler, boolean)
	 */
	@Override
	public void telnetOptionStatusChange(TelnetSocket nvt, TelnetOption option, boolean active) {
		logger.log(Level.INFO, "Feature {0} is {1}", option.name(), active?"enabled":"disabled");
	}

	@Override
	public List<ModeBit> linemodeFlagsSuggested(List<ModeBit> suggested) {
		System.out.println("Linemode is now "+suggested);
		return suggested;
	}

	@Override
	public void sendFlushOn(List<Integer> flushCodes) {
		System.err.println("We should flush on "+flushCodes);
	}

	@Override
	public void telnetSocketChanged(TelnetSocket nvt, State oldState, State newState) {
		// TODO Auto-generated method stub

	}

}
