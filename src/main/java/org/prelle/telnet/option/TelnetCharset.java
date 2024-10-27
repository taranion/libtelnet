/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;
import org.prelle.telnet.option.TelnetEnvironmentOption.EnvironmentListener;

/**
 * https://datatracker.ietf.org/doc/html/rfc2066
 * @author prelle
 *
 */
public class TelnetCharset extends TelnetSubnegotiationHandler {

	protected final static Logger logger = System.getLogger("telnet.option.charset");

	public static interface CharsetListener extends TelnetOptionListener {

		public void telnetCharsetNegotiated(Charset charset);

	}

	public final static int CODE = 42;

	private final static int REQUEST  = 1;
	private final static int ACCEPTED = 2;
	private final static int REJECTED = 3;
	private final static int TTABLE_IS = 4;
	private final static int TTABLE_REJECTED = 5;
	private final static int TTABLE_ACK = 6;
	private final static int TTABLE_NAK = 8;

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSubnegotiationHandler#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "TODO: Subnegotiate for CHARSET: "+Arrays.toString(values));
		int operation = values[0];
		
		if (operation==ACCEPTED) {
			byte[] data = new byte[values.length-1];
			for (int i=0; i<data.length; i++) {
				data[i] = (byte)values[i+1];
			}
			String csName = new String(data);
			System.err.println("TelnetCharset: Remote party agreed to use "+csName);
			Charset charset = null;
			if ("ISO 8859-15".equals(csName))
				charset = StandardCharsets.ISO_8859_1;
			else if ("ISO 8859-1".equals(csName))
				charset = StandardCharsets.ISO_8859_1;
			else if ("UTF-8".equals(csName) || "UTF8".equals(csName))
				charset = StandardCharsets.UTF_8;
			else
				charset = Charset.forName(csName);
			
			origin.setOptionData(CODE, charset);
			try {
				CharsetListener listener = origin.getOptionListener(code);
				if (listener!=null) {
					listener.telnetCharsetNegotiated(charset);
				} else {
					logger.log(Level.TRACE, "No CharsetListener");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (operation==REJECTED) {
			logger.log(Level.WARNING, "The remote party rejected our charset suggestions");
			System.err.println("TelnetCharset: Remote party rejected charsets");
		}
		
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 */
	public boolean initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.WARNING, "TODO: initializeAs "+role);
		if (role==CommunicationRole.SERVER) {
			logger.log(Level.DEBUG, "Ask remote party to send environment");
			try {
				origin.setOptionData(CODE, new HashMap<String,String>());
				byte[] charsetData = "UTF-8 CP437 ASCII".getBytes(StandardCharsets.US_ASCII);
				byte[] send = new byte[charsetData.length+7];
				send[0] = (byte)IAC;
				send[1] = (byte)SB;
				send[2] = (byte)CODE;
				send[3] = (byte)REQUEST;
				send[4] = (byte)32;
				System.arraycopy(charsetData, 0, send, 5, charsetData.length);
				send[5+charsetData.length] = (byte)IAC;
				send[6+charsetData.length] = (byte)SE;
				out.writeCommand(send);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else {
			logger.log(Level.WARNING, "Acting as PROVIDER not implemented");
		}
		return false;
	}

}
