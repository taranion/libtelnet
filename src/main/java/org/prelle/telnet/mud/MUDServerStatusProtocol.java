/**
 *
 */
package org.prelle.telnet.mud;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.option.CommunicationRole;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.protocol.TelnetOptionEvent;
import org.prelle.telnet.protocol.TelnetOptionEventImpl;
import org.prelle.telnet.protocol.TelnetProtocol;

/**
 * https://tintin.mudhalla.net/protocols/mssp/
 * @author prelle
 *
 */
public class MUDServerStatusProtocol implements TelnetOption {

	protected final static Logger logger = System.getLogger("telnet.option.mssp");

	public static class MSSPDataEvent extends TelnetOptionEventImpl {
		private Map<String,String> data;
		protected MSSPDataEvent(TelnetOption option, Map<String,String> data) {
			super(option);
			this.data = data;
		}
		public Map<String,String> getData() { return data; }
	}
	
	public final static int CODE = 70;

	private final static int MSSP_VAR = 1;
	private final static int MSSP_VAL = 2;

	private Supplier<Map<String,String>> dataSupplier;
	
	//-------------------------------------------------------------------
	public MUDServerStatusProtocol() {
	}
	
	//-------------------------------------------------------------------
	public MUDServerStatusProtocol(Supplier<Map<String,String>> dataSupplier) {
		this.dataSupplier = dataSupplier;
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
	public String getName() { return "MSSP";}


	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#handleSubnegotiation(int, int[], org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetOutputStream)
	 */
	@Override
	public List<TelnetOptionEvent> handleSubnegotiation(TelnetSubnegotiationEvent event, TelnetProtocol stack) {
		String data = new String(event.getData(), StandardCharsets.UTF_8);
		logger.log(Level.ERROR, "Subnegotiate for MSSP: "+ data);
		
		int[] values = event.getAsIntArray();
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
			case MSSP_VAR:
				mode = dat;
				if (keyBuf.length()>0) {
					logger.log(Level.DEBUG, "System Variable {0}={1}", keyBuf, valBuf);
					variables.put(keyBuf.toString(), valBuf.toString());
				}
				keyBuf = new StringBuffer();
				break;
			case MSSP_VAL:
				mode = dat;
				valBuf = new StringBuffer();
				break;
			default:
				if (mode==MSSP_VAR) {
					keyBuf.append( (char)dat );
					break;
				} else if (mode==MSSP_VAL) {
					valBuf.append( (char)dat );
				}
			}

			logger.log(Level.TRACE, "RCV {0} = {1}", dat, (char)dat);
		}
		if (keyBuf.length()>0) {
			logger.log(Level.DEBUG, "System Variable {0}={1}", keyBuf, valBuf);
			variables.put(keyBuf.toString(), valBuf.toString());
		}
		logger.log(Level.WARNING,"MSSP done: {0}", variables);
		return List.of(new MSSPDataEvent(this, variables));
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 */
	@Override
	public boolean negotiateDetails(TelnetProtocol stack, CommunicationRole role) {
		Map<String,String> data = dataSupplier.get();
		StringBuilder buf = new StringBuilder();
		for (Entry<String,String> entry : data.entrySet()) {
			buf.append((char)MSSP_VAR);
			buf.append(entry.getKey());
			buf.append((char)MSSP_VAL);
			buf.append(entry.getValue());
		}
		stack.sendResponse( stack.factory().createTelnetSubnegotiationEvent(getOptionCode(), buf.toString().getBytes(StandardCharsets.UTF_8)) );
		return false;
	}

}
