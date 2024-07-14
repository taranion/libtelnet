/**
 *
 */
package org.prelle.telnet.option;

import org.prelle.telnet.TelnetOptionHandler;

/**
 * RFC 857
 * @see http://tools.ietf.org/html/rfc857
 * @author prelle
 *
 */
public class TelnetEcho extends TelnetOptionHandler {

	/**
	 * How shall data typed locally be echoed?
	 */
	public static enum SentDataEchoMode {
		// No echo at all
		NO_ECHO,
		// Echo is generated locally
		LOCAL_ECHO,
		// Echo is expected to be sent from remote party
		REMOTE_ECHO
	}

	public final static String VAR_ECHO_SENT = "ECHO_SENT";
	public final static String VAR_ECHO_RCVD = "ECHO_RCVD";

	//-----------------------------------------------------------------
	public TelnetEcho() {
		super(1,"ECHO");
	}

	//-----------------------------------------------------------------
	public TelnetEcho(int code, String name) {
		super(code,name);
	}

//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#initialize(org.prelle.telnet.TelnetSocket)
//	 */
//	@Override
//	public void initialize(TelnetSocket nvt) throws IOException {
//		// Not sending and not awaiting echo
//		nvt.setOptionVariable(TelnetEcho.class, VAR_ECHO_SENT, SentDataEchoMode.NO_ECHO);
//		nvt.setOptionVariable(TelnetEcho.class, VAR_ECHO_RCVD, false);
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
//			logger.log(Level.DEBUG,"Request echo");
//			OutputStream out = nvt.getOutputStream();
//			byte[] send = new byte[6];
//			send[0] = (byte)IAC;
//			send[1] = (byte)SB;
//			send[2] = (byte)TelnetOption.ECHO.getCode();
//			send[3] = (byte)SentDataEchoMode.LOCAL_ECHO.ordinal();
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
//	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in)
//			throws IOException {
//	}
//
//
////	//-----------------------------------------------------------------
////	/**
////	 * @see org.prelle.telnet.option.TelnetOptionHandler#requestUsage(org.prelle.telnet.TelnetSocket)
////	 */
////	public void requestUsage(TelnetSocket nvt) throws IOException {
////		logger.log(Level.DEBUG,"Suggest "+getName());
////		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
////		if (!nvt.isInClientMode()) {
////			nvt.getWillVariable(getCode()).setState(true);
////			out.sendWill(getCode());
////		} else {
////			nvt.getDoVariable(getCode()).setState(true);
////			out.sendDo(getCode());
////		}
////	}
////
////	//-----------------------------------------------------------------
////	public void requestStop(TelnetSocket nvt) throws IOException {
////		logger.log(Level.DEBUG,"Stop "+getName());
////		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
////		if (!nvt.isInClientMode()) {
////			nvt.getWillVariable(getCode()).setState(false);
////			out.sendWont(getCode());
////		} else {
////			nvt.getDoVariable(getCode()).setState(false);
////			out.sendDont(getCode());
////		}
////	}
//
//	//-------------------------------------------------------------------
//	public static boolean shouldGenerateLocalEcho(TelnetSocket nvt) {
//		SentDataEchoMode  mode = (SentDataEchoMode) nvt.getOptionVariable(TelnetEcho.class, VAR_ECHO_SENT);
//		if (mode!=null && mode==SentDataEchoMode.LOCAL_ECHO)
//			return true;
//		return false;
//	}
//
//	//-------------------------------------------------------------------
//	public static boolean shouldGenerateRemoteEcho(TelnetSocket nvt) {
//		Boolean remoteEcho= (Boolean) nvt.getOptionVariable(TelnetEcho.class, VAR_ECHO_RCVD);
//		if (remoteEcho!=null && remoteEcho)
//			return true;
//		return false;
//	}
}
