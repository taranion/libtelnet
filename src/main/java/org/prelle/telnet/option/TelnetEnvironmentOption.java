/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSubnegotiationHandler;

/**
 * RFC 857
 * @see http://tools.ietf.org/html/rfc857
 * @author prelle
 *
 */
public class TelnetEnvironmentOption extends TelnetSubnegotiationHandler {

	protected final static Logger logger = System.getLogger("telnet.option.environ");

	public static interface EnvironmentListener extends TelnetOptionListener {

		public void telnetLearnedEnvironmentVariables(Map<String,String> variables);

	}

	public final static int CODE = 39;

	private final static int	IS   = 0;
	private final static int	SEND = 1;
	private final static int	INFO = 2;

	private final static int	VAR     = 0;
	private final static int	VALUE   = 1;
	private final static int	ESC     = 2;
	private final static int	USERVAR = 3;

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public void handleSubnegotiation(int code, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		logger.log(Level.DEBUG,"performSubNegotiation for ENVIRON");
		int operation = values[0];

		if (operation!=IS) {
			logger.log(Level.WARNING, "Other operations than IS not supported yet");
			return;
		}

		StringBuffer keyBuf = new StringBuffer();
		StringBuffer valBuf = new StringBuffer();
		Map<String,String> variables = origin.getOptionData(CODE);
		int mode = -1;
		int i=1;
		while (i<values.length) {
			int dat = values[i++];
			if (dat==IAC) {
				// End of list
				if (keyBuf.length()>0) {
					logger.log(Level.INFO, "Variable {0}={1}", keyBuf, valBuf);
					variables.put(keyBuf.toString(), valBuf.toString());
				}
				break;
			}

			switch (dat) {
			case VAR:
				mode = dat;
				if (keyBuf.length()>0) {
					logger.log(Level.DEBUG, "System Variable {0}={1}", keyBuf, valBuf);
					variables.put(keyBuf.toString(), valBuf.toString());
				}
				keyBuf = new StringBuffer();
				break;
			case USERVAR:
				mode = dat;
				if (keyBuf.length()>0) {
					logger.log(Level.DEBUG, "User Variable {0}={1}", keyBuf, valBuf);
					variables.put(keyBuf.toString(), valBuf.toString());
				}
				keyBuf = new StringBuffer();
				break;
			case VALUE:
				mode = dat;
				valBuf = new StringBuffer();
				break;
			case ESC:
				logger.log(Level.WARNING, "Not supported {0}", dat);
				break;
			default:
				if (mode==VAR || mode==USERVAR) {
					keyBuf.append( (char)dat );
					break;
				} else if (mode==VALUE) {
					valBuf.append( (char)dat );
				}
			}

			logger.log(Level.TRACE, "RCV {0} = {1}", dat, (char)dat);
		}
		logger.log(Level.DEBUG,"Telnet Environment done: {0}", variables);
		origin.subnegotiationEndedFor(code,variables);

		EnvironmentListener listener = origin.getOptionListener(code);
		if (listener!=null) {
			listener.telnetLearnedEnvironmentVariables(variables);
		} else {
			logger.log(Level.TRACE, "No EnvironmentListener");
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initializeAs(org.prelle.telnet.Role)
	 */
	@Override
	public boolean initializeAs(TelnetOption option, CommunicationRole role, TelnetSocket origin, TelnetOutputStream out) {
		if (role==CommunicationRole.SERVER) {
			logger.log(Level.DEBUG, "Ask remote party to send environment");
			try {
				origin.setOptionData(CODE, new HashMap<String,String>());
				byte[] send = new byte[8];
				send[0] = (byte)IAC;
				send[1] = (byte)SB;
				send[2] = (byte)CODE;
				send[3] = (byte)SEND;
				send[4] = (byte)VAR;
				send[5] = (byte)USERVAR;
				send[6] = (byte)IAC;
				send[7] = (byte)SE;
				out.writeCommand(send);
				send = new byte[7];
				send[0] = (byte)IAC;
				send[1] = (byte)SB;
				send[2] = (byte)CODE;
				send[3] = (byte)SEND;
				send[4] = (byte)VAR;
				send[5] = (byte)IAC;
				send[6] = (byte)SE;
				out.writeCommand(send);
				send = new byte[7];
				send[0] = (byte)IAC;
				send[1] = (byte)SB;
				send[2] = (byte)CODE;
				send[3] = (byte)SEND;
				send[4] = (byte)USERVAR;
				send[5] = (byte)IAC;
				send[6] = (byte)SE;
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
