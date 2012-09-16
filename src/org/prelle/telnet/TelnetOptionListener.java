/**
 * 
 */
package org.prelle.telnet;

import org.prelle.telnet.option.TelnetOption;

/**
 * @author prelle
 *
 */
public interface TelnetOptionListener {

	public void telnetOptionDataChanged(TelnetSocket nvt, TelnetOption option, Object data);
	
}
