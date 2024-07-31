/**
 *
 */
package org.prelle.telnet.option;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * @author prelle
 *
 */
public class SuppressGoAhead extends TelnetOptionHandler {

	public final static int CODE = 3;

	//-----------------------------------------------------------------
	public SuppressGoAhead() {
		super(CODE, "SGA");
	}

}
