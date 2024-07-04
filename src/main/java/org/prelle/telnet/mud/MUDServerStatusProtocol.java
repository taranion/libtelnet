/**
 *
 */
package org.prelle.telnet.mud;

import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * See http://tintin.sourceforge.net/mssp/
 * @see http://tintin.sourceforge.net/mssp/
 * @author prelle
 *
 */
public class MUDServerStatusProtocol extends TelnetOptionHandler {

	//-----------------------------------------------------------------
	public MUDServerStatusProtocol() {
		super(70, "MSSP");
	}

	private final static int MSSP_VAR = 1;
	private final static int MSSP_VAL = 2;


	//-----------------------------------------------------------------
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "MSSP sub\n"+Arrays.toString(values));

		Map<String,String> variables = new HashMap<>();
		boolean valueMode = false;
		StringBuffer var = new StringBuffer();
		StringBuffer val = new StringBuffer();
		for (int i : values) {
			switch (i) {
			case MSSP_VAR:
				logger.log(Level.INFO, var+"="+val);
				variables.put(var.toString(), val.toString());
				var.delete(0, var.length());
				valueMode = false;
				break;
			case MSSP_VAL:
				val.delete(0, val.length());
				valueMode = true;
				break;
			default:
				if (valueMode)
					val.append( (char)i);
				else
					var.append( (char)i);
			}
		}
	}

}
