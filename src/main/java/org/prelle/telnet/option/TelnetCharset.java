/**
 *
 */
package org.prelle.telnet.option;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * https://datatracker.ietf.org/doc/html/rfc2066
 * @author prelle
 *
 */
public class TelnetCharset extends TelnetOptionHandler {

	private final static int REQUEST  = 1;
	private final static int ACCEPTED = 2;
	private final static int REJECTED = 3;
	private final static int TTABLE_IS = 4;
	private final static int TTABLE_REJECTED = 5;
	private final static int TTABLE_ACK = 6;
	private final static int TTABLE_NAK = 8;

    //-----------------------------------------------------------------
	public TelnetCharset() {
		super(42, "CHARSET");
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
//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#optionEnabled(org.prelle.telnet.TelnetSocket, boolean)
//	 */
//	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
//		logger.log(Level.INFO,getName()+" enabled, client={0}", nvt.isInClientMode());
//
//		// Can only be send by client side (that received the DO)
//		if (nvt.isInClientMode()) {
//			logger.log(Level.DEBUG,"Request charset");
//			OutputStream out = nvt.getOutputStream();
//			byte[] send = new byte[6];
//			send[0] = (byte)IAC;
//			send[1] = (byte)SB;
//			send[2] = (byte)code;
//			send[3] = (byte)REQUEST;
//			send[4] = (byte)IAC;
//			send[5] = (byte)SE;
//			out.write(send);
//			out.flush();
//		} else {
//			logger.log(Level.DEBUG, "I am server - wait for client to start");
//		}
//	}
//
//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
//	 */
//	@Override
//	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
//		logger.log(Level.DEBUG,"performSubNegotiation for "+getName());
//		in.setHigherLevelControl(true);
//		while (true) {
//			int dat = in.read();
//			if (dat==IAC) {
//				// End of list
//				break;
//			}
//			logger.log(Level.INFO, "RCV {0} = {1}", dat, (char)dat);
//		}
//
//		int dat = in.read(); //SE
//		if (dat!=SE) {
//			logger.log(Level.WARNING,"Expected subnegotiation end, but found "+dat);
//		}
//
//		in.setHigherLevelControl(false);
//	}

}
