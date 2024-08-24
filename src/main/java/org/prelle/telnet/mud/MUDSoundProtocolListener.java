package org.prelle.telnet.mud;

import org.prelle.telnet.TelnetOptionListener;

/**
 *
 */
public interface MUDSoundProtocolListener extends TelnetOptionListener {

	public void mspReceivedCommand(String mspCommand);

}
