/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetSocket;

/**
 * https://datatracker.ietf.org/doc/html/rfc2066
 * @author prelle
 *
 */
public class TelnetCharset extends TelnetOptionHandler {

    //-----------------------------------------------------------------
	public TelnetCharset() {
		super(42, "CHARSET");
	}

	//-----------------------------------------------------------------
	/**
	 * @throws IOException 
	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
	 */
	@Override
	public void initialize(TelnetSocket console) throws IOException {
		requestUsage(console);
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
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
	}

}
