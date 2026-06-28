package org.prelle.telnet;

import java.lang.System.Logger.Level;

/**
 *
 */
@Deprecated
public interface TelnetOptionListener {

	public default void remotePartySent(TelnetSocket socket, int code, TelnetCommand command) {
		System.getLogger("telnet.option").log(Level.WARNING, "Do-nothing default handler called for {0} : {1}", command, this.getClass());
	}

}
