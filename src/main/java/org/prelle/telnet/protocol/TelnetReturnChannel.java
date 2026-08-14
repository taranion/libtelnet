package org.prelle.telnet.protocol;

import java.io.IOException;

import org.prelle.telnet.event.TelnetEvent;

@FunctionalInterface
public interface TelnetReturnChannel {

	void sendToRemote(TelnetEvent event) throws IOException;

}
