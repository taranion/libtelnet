/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.DoVariable;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.WillVariable;

/**
 * @author prelle
 *
 */
public class LineMode extends TelnetOption {

	private final static int    CODE = 34;
	private final static String NAME = "LINEMODE";

	private final static int MODE        = 1;
	private final static int FORWARDMASK = 2;
	private final static int SLC         = 3;
	
	private final static int EDIT     = 1;
	private final static int TRAPSIG  = 2;
	private final static int MODE_ACK = 4;
	private final static int SOFT_TAB = 8;
	private final static int LIT_ECHO = 16;

	private final static int SLC_SYNCH = 1;
	private final static int SLC_BRK   = 2;
	private final static int SLC_IP    = 3;
	private final static int SLC_AO    = 4;
	private final static int SLC_AYT   = 5;
	private final static int SLC_EOR   = 6;
	private final static int SLC_ABORT = 7;
	private final static int SLC_EOF   = 8;
	private final static int SLC_SUSP  = 9;
	private final static int SLC_EC    = 10;
	private final static int SLC_EL    = 11;
	private final static int SLC_EW    = 12;
	private final static int SLC_RP    = 13;
	private final static int SLC_LNEXT = 14;
	private final static int SLC_XON   = 15;
	private final static int SLC_XOFF  = 16;
	private final static int SLC_FORW1 = 17;
	private final static int SLC_FORW2 = 18;
	private final static int SLC_MCL   = 19;
	private final static int SLC_MCR   = 20;
    private final static int SLC_MCWL  = 21;
    private final static int SLC_MCWR  = 22;
    private final static int SLC_MCBOL = 23;
    private final static int SLC_MCEOL = 24;
    private final static int SLC_INSRT = 25;
    private final static int SLC_OVER  = 26;
    private final static int SLC_ECR   = 27;
    private final static int SLC_EWR   = 28;
    private final static int SLC_EBOL  = 29;
    private final static int SLC_EEOL  =  30;

    private final static String FUNCTIONS[] = {
    	"-0-",
		"SLC_SYNCH",
		"SLC_BRK",
		"SLC_IP",
		"SLC_AO",
		"SLC_AYT",
		"SLC_EOR",
		"SLC_ABORT",
		"SLC_EOF",
		"SLC_SUSP",
		
		"SLC_EC",		
		"SLC_EL",
		"SLC_EW",
		"SLC_RP",
		"SLC_LNEXT",
		"SLC_XON",
		"SLC_XOFF",
		"SLC_FORW1",
		"SLC_FORW2",
		"SLC_MCL",

		"SLC_MCR",
		"SLC_MCWL",
		"SLC_MCBOL",
		"SLC_MCEOL",
		"SLC_INSRT",
		"SLC_OVER",
		"SLC_ECR",
		"SLC_EWR",
		"SLC_EBOL",
		
		"SLC_EEOL",
    };



    //-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#setDefaults(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void setDefaults(TelnetSocket nvt) {
		nvt.setOptionVariable(new WillVariable(CODE, false));
		nvt.setOptionVariable(new DoVariable(CODE, false));
	}
	
	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getCode()
	 */
	@Override
	public int getCode() {
		return CODE;
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	//-----------------------------------------------------------------
	/**
	 * @throws IOException 
	 * @see org.prelle.telnet.option.TelnetOption#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
		in.setHigherLevelControl(true);
		int type = in.read();
		switch (type) {
		case MODE:
			logger.debug("Client requests mode mask");
			int mask = in.read();
			in.read();  // IAC
			in.read();  // SE
			break;
		case SLC:
			while (true) {
				int funct = in.read();
				int modif = in.read();
				if (funct==TelnetConstants.IAC && modif==TelnetConstants.SE) break;
				int ascii = in.read();
				if (ascii==255)
					ascii = ascii*in.read();
				logger.debug("  Function="+funct+"  Modifier="+modif+"   ASCII="+ascii);
				logger.debug("  Function="+FUNCTIONS[funct]+"  Modifier="+modif+"   ASCII="+ascii);
			}
			break;
		default:
			logger.debug("??? "+type);
			in.read(); // IAC
			in.read(); // SB
		}
		
		in.setHigherLevelControl(false);
	}

}
