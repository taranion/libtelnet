/**
 *
 */
package org.prelle.telnet;

import org.prelle.telnet.TelnetSocket.State;

/**
 * @author prelle
 *
 */
public interface TelnetSocketListener {

//	public default void telnetSupportedOptionsKnown(TelnetSocket nvt) {}

	public default void telnetOptionStatusChange(TelnetSocket nvt, TelnetOption option, boolean active) {}

	public default void telnetCommandReceived(TelnetSocket nvt, TelnetCommand command) {}

	public void telnetSocketChanged(TelnetSocket nvt, State oldState, State newState);

}
