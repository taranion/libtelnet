/**
 * 
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public interface NetworkVirtualConsoleListener {

	public void windowSizeDetermined(NetworkVirtualConsole console, int width, int height);

	public void terminalTypeDetermined(NetworkVirtualConsole console, String termType);

	public void interruptProcessRequested(NetworkVirtualConsole console);
	
}
