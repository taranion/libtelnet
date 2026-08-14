package org.prelle.telnet.protocol;

import org.prelle.telnet.event.TelnetEvent;

public interface TelnetReturnChannel {

	void sendToRemote(TelnetEvent event) throws java.io.IOException;

}
