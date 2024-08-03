package org.prelle.telnet.mud;

import java.lang.System.Logger.Level;
import java.util.Arrays;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 *
 */
public class AardwolfMushclientProtocol extends TelnetOptionHandler {

	public static enum MUDMode {
		LOGIN_SCREEN(1),
		START_SCREEN(2),
		FULL_ACTIVE(3),
		AFK(4),
		NOTE_MODE(5),
		BUILDING(6),
		PAGED_OUTPUT(7)
		;
		int code;
		MUDMode(int val) {
			this.code=val;
		}
		static MUDMode valueOf(int i) {
			for (MUDMode tmp : MUDMode.values()) {
				if (tmp.code==i)
					return tmp;
			}
			return null;
		}
	}


	private final static int CODE = 102;

	//-------------------------------------------------------------------
	public AardwolfMushclientProtocol() {
		super(CODE, "AARDWOLF");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket nvt, TelnetOutputStream out) {
		logger.log(Level.INFO,"As {0} we received2 : {1}", role, Arrays.toString(values));
		switch (values[0]) {
		case 100:
			MUDMode mode = MUDMode.valueOf(values[1]);
			if (mode!=null) {
				nvt.fireOptionDataChanged(this, mode);
			} else {
				logger.log(Level.ERROR, "Unknown mode {0} for Aardwolf protocol 102", values[1]);
			}
		case 101:
			// Received MUD tick
			logger.log(Level.WARNING,"TICK received");
			break;
		default:
			logger.log(Level.WARNING,"TODO: As {0} we received2 : {1}", role, Arrays.toString(values));
		}
	}

}
