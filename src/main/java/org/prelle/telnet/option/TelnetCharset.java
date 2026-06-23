/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;

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
		logger.log(Level.DEBUG, "Subnegotiate for CHARSET: "+Arrays.toString(values));
		int operation = values[0];
		
		if (operation==ACCEPTED) {
			byte[] data = new byte[values.length-1];
			for (int i=0; i<data.length; i++) {
				data[i] = (byte)values[i+1];
			}
			String csName = new String(data).trim();
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
		} else if (operation==REQUEST) {
			byte[] data = new byte[values.length-1];
			for (int i=0; i<data.length; i++) {
				data[i] = (byte)values[i+1];
			}
			String namesRaw = new String(data);
			logger.log(Level.WARNING, "Remote party requested raw: "+namesRaw);
			// The first character is assumed to be the separator. It may be absent though, in which case we use some default
			char first = namesRaw.charAt(0);
			String sep = Character.isAlphabetic(first)?" ,;":String.valueOf(first);
			// Now tokenize
			List<String> csNames = new ArrayList<>();
			for (StringTokenizer tok=new StringTokenizer(namesRaw,sep); tok.hasMoreTokens(); ) {
				csNames.add(tok.nextToken());
			}
			logger.log(Level.WARNING, "Remote party requested "+csNames);
			List<Charset> requested = new ArrayList<>();
			csNames.forEach(csName -> {
				try {
					Charset charset = null;
					if ("ISO 8859-15".equals(csName))
						charset = StandardCharsets.ISO_8859_1;
					else if ("ISO 8859-1".equals(csName))
						charset = StandardCharsets.ISO_8859_1;
					else if ("UTF-8".equals(csName) || "UTF8".equals(csName))
						charset = StandardCharsets.UTF_8;
					else
						charset = Charset.forName(csName);
					requested.add(charset);
				} catch (Exception e) {
				}
			});
			logger.log(Level.WARNING, "TODO: What do I do? "+requested);
			System.exit(1);
			Charset consoleCharset = origin.getOptionData(CODE);
			if (requested.contains(consoleCharset)) {
				byte[] append = consoleCharset.toString().getBytes(StandardCharsets.US_ASCII);
//				byte[] toSend = new byte[1+append.length];
//				toSend[0] = ACCEPTED;
//				System.arraycopy(append, 0, toSend, 0, append.length);
				logger.log(Level.WARNING, "Accept "+consoleCharset);
				try {
					out.sendSubNegotiation(CODE, ACCEPTED, append);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
				logger.log(Level.DEBUG, "SND {0}", Arrays.toString(send));
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
