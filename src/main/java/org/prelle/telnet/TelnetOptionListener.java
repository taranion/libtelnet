/**
 * 
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public interface TelnetOptionListener {

	public void telnetOptionDataChanged(TelnetSocket nvt, TelnetOptionHandler option, Object data);
	
}
