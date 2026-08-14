/**
 *
 */
package org.prelle.telnet.option;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.protocol.SubnegotiationFinishedEvent;
import org.prelle.telnet.protocol.TelnetOptionEvent;
import org.prelle.telnet.protocol.TelnetOptionEventImpl;
import org.prelle.telnet.protocol.TelnetProtocol;

/**
 * https://datatracker.ietf.org/doc/html/rfc2066
 * @author prelle
 *
 */
public class TelnetCharset implements TelnetOption {
	
	public static class CharsetNegotiatedEvent extends TelnetOptionEventImpl {
		private Charset charset;
		public CharsetNegotiatedEvent(TelnetCharset option, Charset charset) {
			super(option);
			this.charset = charset;
		}
		public Charset getCharset() { return charset; }
	}

	protected final static Logger logger = System.getLogger("telnet.option.charset");

	public final static int CODE = 42;

	private final static int REQUEST  = 1;
	private final static int ACCEPTED = 2;
	private final static int REJECTED = 3;
	private final static int TTABLE_IS = 4;
	private final static int TTABLE_REJECTED = 5;
	private final static int TTABLE_ACK = 6;
	private final static int TTABLE_NAK = 8;
	
	
	private List<String> supportedCharsets = new ArrayList<>();
	private Charset consoleCharset;
	
	private boolean isNegotiationFinished = false;

	//-------------------------------------------------------------------
	public TelnetCharset(String ... charsets) {
		if (charsets!=null)
			supportedCharsets = Arrays.asList(charsets);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}
	
	//-------------------------------------------------------------------
	@Override
	public String getName() { return "CHARSET"; }
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	public boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}
	
	//-----------------------------------------------------------------
	public boolean isSubnegotiationFinished() {
		return isNegotiationFinished;
	}
	public void setSubnegotiationFinished(boolean finished) {
		this.isNegotiationFinished = finished;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public List<TelnetOptionEvent> handleSubnegotiation(TelnetSubnegotiationEvent event, TelnetProtocol stack) {
		byte[] values = event.getData();
		logger.log(Level.INFO, "RCV Subnegotiate for CHARSET: "+Arrays.toString(values));
		int operation = values[0];
		
		if (operation==ACCEPTED) {
			byte[] data = new byte[values.length-1];
			for (int i=0; i<data.length; i++) {
				data[i] = (byte)values[i+1];
			}
			String csName = new String(data).trim();
			logger.log(Level.INFO, "Remote party accepted charset: "+csName);
			if ("ISO 8859-15".equals(csName))
				consoleCharset = StandardCharsets.ISO_8859_1;
			else if ("ISO 8859-1".equals(csName))
				consoleCharset = StandardCharsets.ISO_8859_1;
			else if ("UTF-8".equals(csName) || "UTF8".equals(csName))
				consoleCharset = StandardCharsets.UTF_8;
			else
				consoleCharset = Charset.forName(csName);
			isNegotiationFinished = true;
			return List.of( 
					new CharsetNegotiatedEvent(this,consoleCharset),
					new SubnegotiationFinishedEvent(this));
		} else if (operation==REQUEST) {
			byte[] data = new byte[values.length-1];
			for (int i=0; i<data.length; i++) {
				data[i] = (byte)values[i+1];
			}
			String namesRaw = new String(data);
			logger.log(Level.INFO, "Remote party requested raw: "+namesRaw);
			// The first character is assumed to be the separator. It may be absent though, in which case we use some default
			char first = namesRaw.charAt(0);
			String sep = Character.isAlphabetic(first)?" ,;":String.valueOf(first);
			// Now tokenize
			List<String> csNames = new ArrayList<>();
			for (StringTokenizer tok=new StringTokenizer(namesRaw,sep); tok.hasMoreTokens(); ) {
				csNames.add(tok.nextToken());
			}
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
					logger.log(Level.WARNING, "Unknown charset "+csName);
				}
			});
			if (requested.contains(consoleCharset)) {
				byte[] append = consoleCharset.toString().getBytes(StandardCharsets.US_ASCII);
//				byte[] toSend = new byte[1+append.length];
//				toSend[0] = ACCEPTED;
//				System.arraycopy(append, 0, toSend, 0, append.length);
				logger.log(Level.WARNING, "Accept "+consoleCharset);
				
				byte[] payload = new byte[1+append.length];
				payload[0] = (byte)ACCEPTED;
				System.arraycopy(append, 0, payload, 1, append.length);
				stack.sendResponse(stack.factory().createTelnetSubnegotiationEvent(CODE, payload));
			}
			
		} else if (operation==REJECTED) {
			logger.log(Level.WARNING, "The remote party rejected our charset suggestions");
			isNegotiationFinished = true;
		}
		return List.of();
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol stack, CommunicationRole role) {
		logger.log(Level.INFO, "ENTER: negotiateDetails()------------------------------------");
		try {
			var line = (" "+String.join(" ", supportedCharsets));
			logger.log(Level.INFO, "Inform client that we support \"{0}\"", line);
			byte[] charsetData = line.getBytes(StandardCharsets.US_ASCII);
			byte[] send = new byte[charsetData.length+2];
			send[0] = (byte)REQUEST;
			send[1] = (byte)32;
			System.arraycopy(charsetData, 0, send, 2, charsetData.length);
			logger.log(Level.DEBUG, "SND {0}", Arrays.toString(send));
			stack.sendResponse(stack.factory().createTelnetSubnegotiationEvent(CODE, send));
			return true;
		} finally {
			logger.log(Level.DEBUG, "LEAVE: negotiateDetails()");
		}
	}

}
