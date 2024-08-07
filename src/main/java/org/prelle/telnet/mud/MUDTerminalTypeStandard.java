/**
 *
 */
package org.prelle.telnet.mud;

import java.util.List;

import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.option.TerminalType;

/**
 * See http://tintin.sourceforge.net/mtts/
 * @see http://tintin.sourceforge.net/mtts/
 * @author prelle
 *
 *
 */
public class MUDTerminalTypeStandard extends TerminalType {

	static enum RequestState { DEFAULT, CLIENT_NAME, TERMINAL_TYPE, MUD_TERMINAL_TYPE, UNKNOWN}

	//-------------------------------------------------------------------
	/**
	 * Called when all information has been received
	 */
	protected void processResult(TelnetSocket nvt, List<String> received) {
		MUDTerminalTypeData data = new MUDTerminalTypeData();
		if (!received.isEmpty())
			data.setClientName(received.get(0));
		if (received.size()>1)
			data.setTerminalType(received.get(1));
		if (received.size()>2)
			data.setMudTerminalType(received.get(2));
//		nvt.fireOptionDataChanged(this, data);
	}

}
