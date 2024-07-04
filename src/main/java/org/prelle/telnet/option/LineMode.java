/**
 *
 */
package org.prelle.telnet.option;

import java.lang.System.Logger.Level;
import java.util.Arrays;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * @author prelle
 *
 */
public class LineMode extends TelnetOptionHandler {

	private final static int MODE        = 1;
	private final static int FORWARDMASK = 2;
	private final static int SLC         = 3;

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

		for (int i=0; i<values.length; i++) {
			switch (values[i]) {
			case MODE:
				logger.log(Level.DEBUG,"Client requests mode mask");
				logger.log(Level.DEBUG, "MODE");
				break;
			case FORWARDMASK:
				logger.log(Level.DEBUG, "FORWARDMASK");
				break;
			case SLC:
				SLCType type = SLCType.valueOf(values[++i]);
				int value1 = values[++i];
				int value2 = values[++i];
				int code = values[++i];
				logger.log(Level.DEBUG, "SLC {0}: {1}, {2}, {3}",type, value1, value2, code);
				break;
			}
		}
	}

}
