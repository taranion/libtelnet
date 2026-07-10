package org.prelle.telnet.option;

import java.lang.System.Logger;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.option.LineMode.ModeBit;

/**
 * 
 */
public class EchoMode implements TelnetOption<org.prelle.telnet.option.EchoMode.EchoModeListener> {

	protected final static Logger logger = System.getLogger("telnet.echo");

	public static interface EchoModeListener extends TelnetOptionListener {
		public void setRemoteEcho(boolean value);
	}

	//-------------------------------------------------------------------
	/**
	 */
	public EchoMode() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getOptionCode() {
		return 1;
	}

	@Override
	public String getName() {
		return "ECHO";
	}
	
	//-----------------------------------------------------------------
	@Override
	public boolean startCommunicationAs(CommunicationRole role) {
		return false;
	}

	@Override
	public boolean negotiateDetails(TelnetProtocol stack) {
		logger.log(Logger.Level.WARNING, "EchoMode.negotiateDetails");
		return false;
	}

	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		// TODO Auto-generated method stub
		logger.log(Logger.Level.WARNING, "EchoMode.handleSubnegotiation: "+values.length+" values: "+values);
	}

	@Override
	public void addListener(EchoModeListener listener) {
		// TODO Auto-generated method stub
		
	}

}
