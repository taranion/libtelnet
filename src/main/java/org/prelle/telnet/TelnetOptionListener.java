/**
 *
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public interface TelnetOptionListener {

	public default void telnetSupportedOptionsKnown(TelnetSocket nvt) {}

	public void telnetOptionStatusChange(TelnetSocket nvt, TelnetOptionHandler option, boolean active);

	public void telnetOptionDataChanged(TelnetSocket nvt, TelnetOptionHandler option, Object data);

	public default void telnetCommandReceived(TelnetSocket nvt, TelnetCommand command) {}

}
