/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.prelle.telnet.Role;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetOptionDeleteMe;
import org.prelle.telnet.TelnetSocket;

/**
 * RFC 857
 * @see http://tools.ietf.org/html/rfc857
 * @author prelle
 *
 */
public class TelnetEnvironmentOption extends TelnetOptionHandler {

	public final static int CODE = 39;

	private final static int	IS   = 0;
	private final static int	SEND = 1;
	private final static int	INFO = 2;

	private final static int	VAR     = 0;
	private final static int	VALUE   = 1;
	private final static int	ESC     = 2;
	private final static int	USERVAR = 3;

	//-------------------------------------------------------------------
	public TelnetEnvironmentOption() {
		super(CODE, "ENVIRON");
	}

//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
//	 */
//	@Override
//	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
//		logger.log(Level.DEBUG,"performSubNegotiation for "+getName());
//		in.setHigherLevelControl(true);
//
//		Map<String,String> variables = new HashMap<>();
//		StringBuffer keyBuf = new StringBuffer();
//		StringBuffer valBuf = new StringBuffer();
//		int mode = -1;
//		while (true) {
//			int dat = in.read();
//			if (dat==IAC) {
//				// End of list
//				if (keyBuf.length()>0) {
//					logger.log(Level.INFO, "Variable {0}={1}", keyBuf, valBuf);
//					variables.put(keyBuf.toString(), valBuf.toString());
//				}
//				break;
//			}
//
//			switch (dat) {
//			case VAR:
//			case USERVAR:
//				mode = dat;
//				if (keyBuf.length()>0) {
//					logger.log(Level.INFO, "Variable {0}={1}", keyBuf, valBuf);
//					variables.put(keyBuf.toString(), valBuf.toString());
//				}
//				keyBuf = new StringBuffer();
//				break;
//			case VALUE:
//				mode = dat;
//				valBuf = new StringBuffer();
//				break;
//			case ESC:
//				logger.log(Level.WARNING, "Not supported {0}", dat);
//				break;
//			default:
//				if (mode==VAR || mode==USERVAR) {
//					keyBuf.append( (char)dat );
//					break;
//				} else if (mode==VALUE) {
//					valBuf.append( (char)dat );
//				}
//			}
//
//			logger.log(Level.TRACE, "RCV {0} = {1}", dat, (char)dat);
//		}
//		in.read(); // SE
//		in.setHigherLevelControl(false);
//		logger.log(Level.DEBUG,"Telnet Environment done: {0}", variables);
//
//		nvt.fireOptionDataChanged(this, new TelnetEnvironmentData(variables));
//	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initializeAs(org.prelle.telnet.Role)
	 */
	@Override
	public void initializeAs(Role role, TelnetSocket nvt, TelnetOutputStream out) {
		if (role==Role.REQUESTER) {;
			logger.log(Level.DEBUG, "Ask remote party to send environment");
			try {
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
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.log(Level.WARNING, "Acting as PROVIDER not implemented");
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#handleSubnegotiation(org.prelle.telnet.Role, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public void handleSubnegotiation(Role role, int[] values, TelnetSocket origin, TelnetOutputStream out) {
		int operation = values[0];
		if (operation==SEND) {
			logger.log(Level.DEBUG, "Remote party requests environment information and we are {0}", role);
			if (role==Role.PROVIDER) {
//				sendNextFromList(out);
			} else {
				logger.log(Level.ERROR, "The client requested an environment info from us, but we are a server");
			}
		} else {
			logger.log(Level.DEBUG, "Remote party provides environment information and we are {0}", role);
			processAnswer(values, 1, origin);
		}

	}

	//-------------------------------------------------------------------
	private void processAnswer(int[] data, int offset, TelnetSocket nvt) {
		logger.log(Level.DEBUG, "ENVIRON: {0}", Arrays.toString(data));
		Map<String,String> variables = new HashMap<>();
		StringBuffer keyBuf = new StringBuffer();
		StringBuffer valBuf = new StringBuffer();
		int mode = -1;

		for (int i=offset; i<data.length; i++) {
			int dat = data[i];
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
			case USERVAR:
				mode = dat;
				if (keyBuf.length()>0) {
					logger.log(Level.INFO, "Variable {0}={1}", keyBuf, valBuf);
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

		TelnetEnvironmentData event = new TelnetEnvironmentData(variables);
		nvt.fireOptionDataChanged(this, event);
	}

}
