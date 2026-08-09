/**
 *
 */
package org.prelle.telnet.option;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.option.TelnetOptionEvent.SubnegotiationFinishedEvent;

/**
 * RFC 857
 * @see http://tools.ietf.org/html/rfc857
 * @author prelle
 *
 */
public class TelnetEnvironmentOption implements TelnetOption {
	
	public static class TelnetEnvironmentVariablesEvent extends TelnetOptionEvent {
		private Map<String,String> variables;
		public TelnetEnvironmentVariablesEvent(TelnetEnvironmentOption option, Map<String,String> variables) {
			super(option);
			this.variables = variables;
		}
		public Map<String,String> getVariables() { return variables; }
		
	}

	protected final static Logger logger = System.getLogger("telnet.option.environ");

	public final static int CODE = 39;

	private final static int	IS   = 0;
	private final static int	SEND = 1;
	private final static int	INFO = 2;

	private final static int	VAR     = 0;
	private final static int	VALUE   = 1;
	private final static int	ESC     = 2;
	private final static int	USERVAR = 3;

	private Map<String,String> userVariables = new HashMap<>();
	private Map<String,String> systemVariables = new HashMap<>();
	
	private int answersExpected = 0;
	
	//-------------------------------------------------------------------
	public TelnetEnvironmentOption(String[] ...data) {
		for (String[] pair : data) {
			if (pair.length==2) {
				userVariables.put(pair[0], pair[1]);
			}
		}
	}
	
	//-------------------------------------------------------------------
	public TelnetEnvironmentOption(List<String[]> data, List<String[]> systemData) {
		for (String[] pair : data) {
			if (pair.length==2) {
				userVariables.put(pair[0], pair[1]);
			}
		}
		systemData.forEach( pair -> {
			if (pair.length==2) {
				systemVariables.put(pair[0], pair[1]);
			}
		});
	}
	
	//-------------------------------------------------------------------
	public TelnetEnvironmentOption(Map<String,String> data, Map<String,String> systemData) {
		if (data!=null)
			userVariables = data;
		if (systemData!=null)
			systemVariables = systemData;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return CODE;
	}

	@Override
	public String getName() { return "MNES";}
	
	//-----------------------------------------------------------------
	public boolean isSubnegotiationFinished() {
		return answersExpected==0;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#resolveSubCommandName(byte)
	 */
	@Override
	public String resolveSubCommandName(int position, byte value) {
		if (position==3) {
			switch (value) {
			case IS: return "IS";
			case SEND: return "SEND";
			case INFO: return "INFO";
			default: return ""+value;
			}
		} else {
			switch (value) {
			case VAR: return "VAR";
			case VALUE: return "VALUE";
			case ESC: return "ESC";
			case USERVAR: return "USERVAR";
			default: return ""+value;
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#startNegotiationAs(org.prelle.telnet.option.CommunicationRole)
	 */
	@Override
	public boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
	 */
	@Override
	public List<TelnetOptionEvent> handleSubnegotiation(TelnetSubnegotiationEvent event, TelnetProtocol stack) {
		byte[] values = event.getData();
		logger.log(Level.DEBUG, "Subnegotiate for ENVIRON with {0} answers expected "+answersExpected);
		int operation = values[0];

		int expectedBefore = answersExpected;
		if (answersExpected>0) answersExpected--;
		switch (operation) {
		case IS: handleIS(values, stack); break;
		case INFO: handleINFO(values, stack); break;
		case SEND: handleSEND(values, stack); break;
		default:
			logger.log(Level.WARNING, "Operation {0} not supported yet", resolveSubCommandName(3, (byte)operation));
		}
		
		List<TelnetOptionEvent> result = new ArrayList<>();
		result.add(new TelnetEnvironmentVariablesEvent(this, userVariables));
		logger.log(Level.DEBUG, "Answers expected now {0} and before {1}", answersExpected, expectedBefore);
		if (answersExpected==0 && expectedBefore>0) {
			result.add(new SubnegotiationFinishedEvent(this));
		}
		return result;
	}

	//-------------------------------------------------------------------
	private void handleIS(byte[] values, TelnetProtocol origin) {
		logger.log(Level.TRACE, "handleIS");
		StringBuffer keyBuf = new StringBuffer();
		StringBuffer valBuf = new StringBuffer();
		Map<String,String> variables = new HashMap<String,String>();
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
					logger.log(Level.INFO, "System Variable {0}={1}", keyBuf, valBuf);
					variables.put(keyBuf.toString(), valBuf.toString());
					systemVariables.put(keyBuf.toString(), valBuf.toString());
				}
				keyBuf = new StringBuffer();
				break;
			case USERVAR:
				mode = dat;
				if (keyBuf.length()>0) {
					logger.log(Level.INFO, "User Variable {0} = {1}", keyBuf, valBuf);
					variables.put(keyBuf.toString(), valBuf.toString());
					userVariables.put(keyBuf.toString(), valBuf.toString());
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
		// If there is a rest
		if (keyBuf.length()>0) {
			logger.log(Level.INFO, "Variable {0}={1}", keyBuf, valBuf);
			variables.put(keyBuf.toString(), valBuf.toString());
			systemVariables.put(keyBuf.toString(), valBuf.toString());
		}

		
		logger.log(Level.DEBUG,"Telnet Environment done: {0}", variables);
	}

	//-------------------------------------------------------------------
	private void handleINFO(byte[] values, TelnetProtocol origin) {
		logger.log(Level.DEBUG, "handleINFO "+Arrays.toString(values));
		StringBuffer keyBuf = new StringBuffer();
		StringBuffer valBuf = new StringBuffer();
		Map<String,String> variables = new HashMap<>();
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
		if (keyBuf.length()>0) {
			logger.log(Level.DEBUG, "System Variable {0}={1}", keyBuf, valBuf);
			variables.put(keyBuf.toString(), valBuf.toString());
		}
		logger.log(Level.WARNING,"Telnet Environment done: {0}", variables);
	}

	//-------------------------------------------------------------------
	private void handleSEND(byte[] values, TelnetProtocol origin) {
		logger.log(Level.INFO, "handleSEND "+Arrays.toString(values));
		
		List<String> requestedUser = null;
		List<String> requestedSystem = null;
		
		int mode = ESC;
		StringBuilder buf = new StringBuilder();
		for (int i=1; i<values.length; i++) {
			int dat = values[i];
			switch (mode) {
			case ESC:
				switch (dat) {
				case VAR:
					mode = VAR;
					if (requestedSystem==null) requestedSystem = new ArrayList<>();
					buf = new StringBuilder();
					break;
				case USERVAR:
					mode = USERVAR;
					if (requestedUser==null) requestedUser = new ArrayList<>();
					buf = new StringBuilder();
					break;
				default:
					logger.log(Level.WARNING, "Unexpected {0} in ESC mode", dat);
				}
				break;
			case VAR:
			case USERVAR:
				if (dat>=32 && dat<=126) {
					buf.append((char)dat);
				} else {
					// Buffer is complete, store it
					if (mode==VAR) {
						requestedSystem.add(buf.toString());
					} else {
						requestedUser.add(buf.toString());
					}
					mode = ESC;
				}
				break;
			default:
				logger.log(Level.WARNING, "Unexpected {0} in mode {1}", dat, mode);
			}
		}
		
		logger.log(Level.INFO, "Requested system variables: {0}", requestedSystem);
		logger.log(Level.INFO, "Requested user variables: {0}", requestedUser);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(IAC);
		baos.write(SB);
		baos.write(CODE);
		baos.write(IS);
		if (requestedSystem!=null) {
			Collection<String> keys = (requestedSystem.isEmpty())? systemVariables.keySet(): requestedSystem;
			for (String key : keys) {
				baos.write(VAR);
				baos.write(key.getBytes(StandardCharsets.US_ASCII), 0, key.getBytes(StandardCharsets.US_ASCII).length);
				baos.write(VALUE);
				String value = systemVariables.get(key);
				if (value!=null) {
					baos.write(value.getBytes(StandardCharsets.US_ASCII), 0, value.getBytes(StandardCharsets.US_ASCII).length);
				}
			}
		}
		if (requestedUser!=null) {
			Collection<String> keys = (requestedUser.isEmpty())? userVariables.keySet(): requestedUser;
			for (String key : keys) {
				baos.write(USERVAR);
				baos.write(key.getBytes(StandardCharsets.US_ASCII), 0, key.getBytes(StandardCharsets.US_ASCII).length);
				baos.write(VALUE);
				String value = userVariables.get(key);
				if (value!=null) {
					baos.write(value.getBytes(StandardCharsets.US_ASCII), 0, value.getBytes(StandardCharsets.US_ASCII).length);
				}
			}
		}
		baos.write(IAC);
		baos.write(SE);
		
		try {
			origin.getOutputStream().writeCommand(baos.toByteArray());
			origin.getOutputStream().flush();
			baos.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed sending telnet option",e);
		}
	}
	
	//-----------------------------------------------------------------
	public boolean startSubNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOptionHandler#initializeAs(org.prelle.telnet.Role)
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol origin, CommunicationRole role) {
		if (role==CommunicationRole.CLIENT) {
			logger.log(Level.DEBUG, "Client role, no negotiation needed");
			return false;
		}
		logger.log(Level.DEBUG, "Ask remote party to send environment");
		TelnetOutputStream out = origin.getOutputStream();
		answersExpected = 3;
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
	}

	//-------------------------------------------------------------------
	/**
	 * @return the userVariables
	 */
	public Map<String, String> getUserVariables() {
		return userVariables;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the systemVariables
	 */
	public Map<String, String> getSystemVariables() {
		return systemVariables;
	}

}
