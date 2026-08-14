package org.prelle.telnet.mud;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.protocol.TelnetOptionEvent;
import org.prelle.telnet.protocol.TelnetOptionEventImpl;
import org.prelle.telnet.protocol.TelnetProtocol;

/**
 *
 */
public class AardwolfMushclientProtocol implements TelnetOption {

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
	
	public static class AardwolfMushclientModeEvent extends TelnetOptionEventImpl {
		private MUDMode mode;
		public AardwolfMushclientModeEvent(TelnetOption option, MUDMode mode) {
			super(option);
			this.mode = mode;
		}
		public MUDMode getMudMode() { return mode; }
	}
	
	public static class AardwolfMushclientTickEvent extends TelnetOptionEventImpl {
		public AardwolfMushclientTickEvent(TelnetOption option) {
			super(option);
		}
	}

	private final static int CODE = 102;

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getName()
	 */
	@Override
	public String getName() { return "AMP"; }

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public List<TelnetOptionEvent> handleSubnegotiation(TelnetSubnegotiationEvent event, TelnetProtocol stack) {
		byte[] values = event.getData();
		switch (values[0]) {
		case 100:
			MUDMode mode = MUDMode.valueOf(values[1]);
			logger.log(Level.WARNING,"RCV: mode="+mode);
			return List.of(new AardwolfMushclientModeEvent(this, mode));
		case 101:
			// Received MUD tick
			logger.log(Level.WARNING,"RCV: TICK");
			return List.of(new AardwolfMushclientTickEvent(this));
		default:
			logger.log(Level.WARNING,"TODO: we received2 : {0}", Arrays.toString(values));
		}
		return List.of();
	}

}
