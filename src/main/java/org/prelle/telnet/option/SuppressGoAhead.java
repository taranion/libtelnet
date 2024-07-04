/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * @author prelle
 *
 */
public class SuppressGoAhead extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public SuppressGoAhead() {
		super(3, "SGA");
	}

}
