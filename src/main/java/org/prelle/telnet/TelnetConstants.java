/**
 * 
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public interface TelnetConstants {
	
	public static enum ControlCode {
		SE(240), NOP(241),  DM(242), BREAK(243), IP(244),
		AE(245), AYT(246),  EC(247), EL(248), GA(249),
		SB(250), WILL(251), WONT(252), DO(253), DONT(254), IAC(255);
		
		private final int codeValue;

		ControlCode(int value) {this.codeValue = value;}
		public int code() { return codeValue; }
		public static ControlCode getCodeFor(int val) {
			for (ControlCode code : ControlCode.values())
				if (code.code()==val) return code;
			return null;
		}
		public static boolean isControlCode(int val) {
			for (ControlCode code : ControlCode.values())
				if (code.code()==val) return true;
			return false;
		}
	};

	public final static int SE   = 240;
    //    public final static int NOP  = 241;
    //    public final static int DM   = 242;
    //    public final static int BREAK= 243;
    public final static int IP   = 244;
    //    public final static int AO   = 245;
    //    public final static int AYT  = 246;
    //    public final static int EC   = 247;
    //    public final static int EL   = 248;
    //    public final static int GA   = 249;
    public final static int SB   = 250;
    public final static int WILL = 251;
    public final static int WONT = 252;
    public final static int DO   = 253;
    public final static int DONT = 254;
    public final static int IAC  = 255;

    public final static int SUB_BINARY = 0;
    public final static int SUB_ECHO   = 1;
    public final static int SUB_RECONNECTION = 2;
    public final static int SUB_SUPRESS_GO_AHEAD = 3;
    public final static int SUB_STATUS = 5;
    //    public final static int SUB_TIMING_MARK = 6;
    //    public final static int SUB_OUTPUT_LINE_WIDTH = 8;
    //    public final static int SUB_OUTPUT_PAGE_SIZE = 9;
    //    public final static int SUB_LOGOUT = 18;
    public final static int SUB_TERMTYPE = 24;
    public final static int SUB_NAWS = 31;
    public final static int SUB_LINEMODE = 34;
    //    public final static int SUB_XDISPLOC = 35;
    //    public final static int SUB_EXT_OPT_LIST = 255;

    //      public final static char INDICATE_TAB = (char)9;

}
