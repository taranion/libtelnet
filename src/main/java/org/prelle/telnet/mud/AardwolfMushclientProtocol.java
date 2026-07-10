package org.prelle.telnet.mud;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;

/**
 *
 */
public class AardwolfMushclientProtocol implements TelnetOption<AardwolfMushclientProtocol.AardwolfMushclientListener> {

	protected final static Logger logger = System.getLogger("telnet.aard");

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

	public static interface AardwolfMushclientListener extends TelnetOptionListener {
		public void telnetMudModeChanged(MUDMode mode);
		public void telnetTickReceived();
	}

	private List<AardwolfMushclientListener> listeners = new ArrayList<>();

	private final static int CODE = 102;

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getName()
	 */
	@Override
	public String getName() { return "AMP"; }

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#negotiateDetails(org.prelle.telnet.TelnetProtocol)
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol stack) {
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol nvt) {
		switch (values[0]) {
		case 100:
			MUDMode mode = MUDMode.valueOf(values[1]);
			logger.log(Level.DEBUG,"RCV: mode="+mode);
			if (mode!=null) {
				listeners.forEach(l -> l.telnetMudModeChanged(mode));
			}
		case 101:
			// Received MUD tick
			logger.log(Level.DEBUG,"RCV: TICK");
			listeners.forEach(l -> l.telnetTickReceived());
			break;
		default:
			logger.log(Level.WARNING,"TODO: we received2 : {0}", Arrays.toString(values));
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(AardwolfMushclientListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

}
