/**
 *
 */
package org.prelle.telnet.option;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * @author prelle
 *
 */
public class LineMode extends TelnetOptionHandler {

	static enum ModeBit {
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

    public LineMode() {
    	super(34, "LINEMODE");
    }

//	//-----------------------------------------------------------------
//	/**
//	 * @throws IOException
//	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
//	 */
//	@Override
//	public void initialize(TelnetSocket console) throws IOException {
//		requestUsage(console);
//	}
//
//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
//	 */
//	@Override
//	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
//		in.setHigherLevelControl(true);
//		int type = in.read();
//		switch (type) {
//		case MODE:
//			logger.log(Level.DEBUG,"Client requests mode mask");
//			int mask = in.read();
//			in.read();  // IAC
//			in.read();  // SE
//			break;
//		case SLC:
//			while (true) {
//				int funct = in.read();
//				int modif = in.read();
//				if (funct==TelnetConstants.IAC && modif==TelnetConstants.SE) break;
//				int ascii = in.read();
//				if (ascii==255)
//					ascii = ascii*in.read();
//				logger.log(Level.DEBUG,"  Function="+funct+"  Modifier="+modif+"   ASCII="+ascii);
//				logger.log(Level.DEBUG,"  Function="+FUNCTIONS[funct]+"  Modifier="+modif+"   ASCII="+ascii);
//			}
//			break;
//		default:
//			logger.log(Level.DEBUG,"??? "+type);
//			in.read(); // IAC
//			in.read(); // SB
//		}
//
//		in.setHigherLevelControl(false);
//	}

	//-----------------------------------------------------------------
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "LineMode sub\n"+Arrays.toString(values));

		Operation op = Operation.valueOf(values[0]);
		logger.log(Level.INFO, "IAC SB LINEMODE "+op);
		switch (op) {
		case MODE:
			int mask = values[1];
			logger.log(Level.INFO, "IAC SB LINEMODE MODE "+mask);
			break;
		case SLC:
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
			logger.log(Level.WARNING, "Unhandled "+op);
		}

	}

}
