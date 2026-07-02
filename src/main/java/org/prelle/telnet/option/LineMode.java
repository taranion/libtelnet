/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetSubnegotiationHandler;
import org.prelle.telnet.WellKnownTelnetOptions;

/**
 * @author prelle
 *
 */
public class LineMode implements TelnetSubnegotiationHandler<LineMode.LineModeListener> {

	protected final static Logger logger = System.getLogger("telnet.linemode");

	public static class LineModeConfig {
		LineModeListener listener;
		List<ModeBit> flags = new ArrayList<>();
		List<Integer> flushCodes = new ArrayList<>();

		public LineModeConfig(List<ModeBit> flags, List<Integer> flushCodes) {
			this.flags.addAll(flags);
			this.flushCodes.addAll(flushCodes);
		}
		public LineModeConfig() {
			this.flags.add(ModeBit.EDIT);
			this.flushCodes.add(10);
			this.flushCodes.add(13);
		}
	}

	public static interface LineModeListener extends TelnetOptionListener {
		//-------------------------------------------------------------------
		/**
		 * @param suggested
		 * @return Return with confirmed flags
		 */
		public List<ModeBit> linemodeFlagsSuggested(List<ModeBit> suggested);
		public void linemodeFlagsAcknowledged(List<ModeBit> acknowledged);
		public void sendFlushOn(List<Integer> flushCodes);
	}

	public static enum ModeBit {
		/**
		 * When set, the client side of the connection should process all
		 * input lines, performing any editing functions, and only send
		 * completed lines to the remote side.  When unset, client side
		 * should not process any input from the user, and the server side
		 * should take care of all character processing that needs to be
		 * done.
		 */
		EDIT(1),
		TRAPSIG(2),
		MODE_ACK(4),
		SOFT_TAB(8),
		LIT_ECHO(16)
		;
		int value;
		ModeBit(int value) {
			this.value = value;
		}
		public static ModeBit valueOf(int val) {
			for (ModeBit tmp : ModeBit.values()) {
				if (tmp.value==val) return tmp;
			}
			return null;
		}
		public static List<ModeBit> toModeList(int val) {
			List<ModeBit> ret = new ArrayList<>();
			for (ModeBit tmp : ModeBit.values()) {
				if ((val & tmp.value)>0) ret.add(tmp) ;
			}
			return ret;
		}
	}

	static enum Operation {
		MODE(1),
		FORWARDMASK(2),
		SLC(3)
		;
		int value;
		Operation(int value) {
			this.value = value;
		}
		public static Operation valueOf(int val) {
			for (Operation tmp : Operation.values()) {
				if (tmp.value==val) return tmp;
			}
			logger.log(Level.ERROR, "Unknown operation code {0}",val);
			return null;
		}
	}

	static enum SupportLevel {
		DEFAULT(3),
		VALUE(2),
		CANTCHANGE(1),
		NOSUPPORT(0)
		;
		int value;
		SupportLevel(int value) {
			this.value = value;
		}
		public static SupportLevel valueOf(int val) {
			for (SupportLevel tmp : SupportLevel.values()) {
				if (tmp.value==val) return tmp;
			}
			return null;
		}
	}

	static enum SLCType {
		SYNCH(1),
		BRK  (2),
		IP   (3),
		AO   (4),
		AYT  (5),
		EOR  (6),
		ABORT(7),
		EOF  (8),
		SUSP (9),
		EC   (10),
		EL   (11),
		EW   (12),
		RP   (13),
		LNEXT(14),
		XON  (15),
		XOFF (16),
		FORW1(17),
		FORW2(18),
		MCL  (19),
		MCR  (20),
	    MCWL (21),
	    MCWR (22),
	    MCBOL(23),
	    MCEOL(24),
	    INSRT(25),
	    OVER (26),
	    ECR  (27),
	    EWR  (28),
	    EBOL (29),
	    EEOL ( 30)
		;
		int value;
		SLCType(int value) {
			this.value = value;
		}
		//-------------------------------------------------------------------
		public static SLCType valueOf(int code) {
			for (SLCType func : SLCType.values()) {
				if (func.value==code)
					return func;
			}
			throw new IllegalArgumentException("Not a SLCcode: "+code);
		}
	}

	public static class LineModesChanged {
		private List<ModeBit> lineModes;
		public LineModesChanged(List<ModeBit> modes) {
			 this.lineModes = modes;
		}
		public void setModes(List<ModeBit> modes) { this.lineModes = modes; }
		public List<ModeBit> getModes() { return this.lineModes; }
	}

	public static class SendBufferedDataOn {
		private TelnetConstants.ControlCode request;
		private List<Integer> codes;
		public SendBufferedDataOn(TelnetConstants.ControlCode request, List<Integer> codes) {
			 this.codes = codes;
			 this.request = request;
		}
		public List<Integer> getCodes() { return this.codes; }
	}

	private List<LineModeListener> listeners = new ArrayList<>();
	private CommunicationRole role;

	//-----------------------------------------------------------------
   public LineMode() {
//    	super(TelnetOption.LINEMODE.getCode(), "LINEMODE");
    }

	//-----------------------------------------------------------------
  public LineMode(LineModeListener callback) {
	  listeners.add(callback);
   }

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return 34;
	}
	
	//-------------------------------------------------------------------
	@Override
	public String getName() { return "LINEMODE"; }
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	public boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE if a subnegotiation is needed
	 */
    @Override
	public boolean negotiateDetails(TelnetProtocol stack) {
 		try {
			if (role==CommunicationRole.SERVER) {
				logger.log(Level.INFO, "Start character-a-time mode by clearing EDIT flag");
				setFlags(stack.getOutputStream(), List.of(ModeBit.TRAPSIG));
				return true;
			}
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed requesting terminal type",e);
		}
		return false;
	}


	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
    @Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		logger.log(Level.DEBUG, "LineMode sub {0} as {1}",Arrays.toString(values), role);
		
		Operation op = null;
		if (values[0]>=TelnetConstants.WILL) {
			ControlCode c0 = ControlCode.getCodeFor(values[0]);
			op = Operation.valueOf(values[1]);
			logger.log(Level.INFO, "IAC SB LINEMODE {0} {1}", c0, op);
			int[] remain = new int[values.length-2];
			System.arraycopy(values, 2, remain, 0, remain.length);
			forwardMask(stack, c0, remain);
			return;
		} else {
			op = Operation.valueOf(values[0]);
		}
		switch (op) {
		case MODE:
			int mask = values[1];
			List<ModeBit> modes = ModeBit.toModeList(mask);
			logger.log(Level.DEBUG, "IAC SB LINEMODE MODE "+modes);
			if (role==CommunicationRole.SERVER && !modes.contains(ModeBit.MODE_ACK)) {
				logger.log(Level.WARNING, "Ignore, because we are server - noone tells us what to do");
				return;
			}
			LineModeListener lmList = stack.getOptionListener(WellKnownTelnetOptions.LINEMODE.getCode());
			if (lmList!=null) {
				if (modes.contains(ModeBit.MODE_ACK)) {
					lmList.linemodeFlagsAcknowledged(modes);					
				} else {
					List<ModeBit> confirmed = lmList.linemodeFlagsSuggested(modes);
					confirmMode(stack.getOutputStream(), confirmed);
				}
			} else
				logger.log(Level.WARNING, "No LineModeListener configured");
			break;
		case SLC:
			logger.log(Level.INFO, "IAC SB LINEMODE SLC {0}", Arrays.toString(values));
			for (int i=1; i<values.length;) {
				int funct = values[i++];
				int modif = values[i++];
				int chara = values[i++];
				SLCType type = SLCType.valueOf(funct);
				SupportLevel level = SupportLevel.valueOf( modif&7);
				boolean ack = (modif&128)>0;
				boolean flushIn = (modif&64)>0;
				boolean flushOut = (modif&32)>0;
				List<String> tmp = new ArrayList<>();
				tmp.add(level.name());
				if (ack) tmp.add("ACK");
				if (flushIn) tmp.add("FLUSH_IN");
				if (flushOut) tmp.add("FLUSH_OUT");
				switch (level) {
				case DEFAULT:
					logger.log(Level.DEBUG, "Operation {0} is supported and should use default characters - {2}", type, Integer.toHexString(chara), tmp);
					break;
				case VALUE:
					logger.log(Level.DEBUG, "Operation {0} is supported and uses character 0x{1} - which can be changed - {2}", type, Integer.toHexString(chara), tmp);
					break;
				case CANTCHANGE:
					logger.log(Level.DEBUG, "Operation {0} is supported and uses character 0x{1} - this cannot be changed - {2}", type, Integer.toHexString(chara), tmp);
					break;
				case NOSUPPORT:
					logger.log(Level.DEBUG, "Operation {0} is not supported ",type);
					break;
				}
			}
			break;
		default:
			logger.log(Level.INFO, "IAC SB LINEMODE "+op);
			logger.log(Level.WARNING, "Unhandled "+op);
		}

	}

	//-------------------------------------------------------------------
	private static void confirmMode(TelnetOutputStream out, List<ModeBit> confirmed) {
		int mask = ModeBit.MODE_ACK.value;
		for (ModeBit flag : confirmed) {
			mask |= flag.value;
		}
		byte[] values = new byte[2];
		values[0] = (byte) Operation.MODE.value;
		values[1] = (byte) mask;
		try {
			out.sendSubNegotiation(WellKnownTelnetOptions.LINEMODE.getCode(), values);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error sending subnegotiation",e);
		}
	}

	//-------------------------------------------------------------------
	private void forwardMask(TelnetProtocol stack, ControlCode c0, int[] values) {
		logger.log(Level.WARNING, "Forwardmask {0}: {1}",c0,Arrays.toString(values));
		List<Integer> codes = new ArrayList<>();
		for (int i=0; i<values.length; i++) {
			for (int b=0; b<8; b++) {
				int code = i*8 + b +1;
				int mask = 1<<(7-b);
				if ((values[i] & mask)>0)
					codes.add(code);
			}
		}

		LineModeListener lmList = stack.getOptionListener(WellKnownTelnetOptions.LINEMODE.getCode());
		if (lmList!=null)
			lmList.sendFlushOn(codes);
	}

	//-------------------------------------------------------------------
	public static void setFlags(TelnetOutputStream out, List<ModeBit> flags) throws IOException {
		int flagMask = 0;
		for (ModeBit flag : flags) {
			flagMask |= flag.value;
		}

		out.sendSubNegotiation(WellKnownTelnetOptions.LINEMODE.getCode(), new byte[] {
				(byte)Operation.MODE.value,
				(byte)flagMask
				});
	}

	@Override
	public void addListener(LineModeListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

}
