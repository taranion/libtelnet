/**
 *
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public interface TelnetSocketListener {

	public default void telnetSupportedOptionsKnown(TelnetSocket nvt) {}

	public void telnetOptionStatusChange(TelnetSocket nvt, TelnetOption option, boolean active);

	public default void telnetCommandReceived(TelnetSocket nvt, TelnetCommand command) {}

}
