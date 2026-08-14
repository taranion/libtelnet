/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.util.List;

import org.prelle.telnet.event.TelnetSubnegotiationEvent;
import org.prelle.telnet.parser.TelnetConstants;
import org.prelle.telnet.protocol.TelnetOptionEvent;
import org.prelle.telnet.protocol.TelnetProtocol;

/**
 * @author prelle
 *
 */
public interface TelnetOption extends TelnetConstants {
	
	//-----------------------------------------------------------------
	int getOptionCode();
	
	//-----------------------------------------------------------------
	String getName();
	
	//-----------------------------------------------------------------
	default boolean isSubnegotiationFinished() {
		return true;
	}
	public default void setSubnegotiationFinished(boolean finished) {
	}
	
	//-----------------------------------------------------------------
	default String resolveSubCommandName(int position, byte b) {
		return String.valueOf(b);
	}
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 * @param role Which role do we have in this negotiation
	 * @return TRUE if initiate() should be called.
	 */
	default boolean startNegotiationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed in <code>startCommunicationAs</code>.
	 * @param stack
	 * @param role 
	 * @return Telnet command that has been sent for this option.
	 */
	default void initiate(TelnetProtocol stack, CommunicationRole role) throws IOException {
		stack.sendResponse( stack.factory().createTelnetNegotiationEvent(ControlCode.DO, getOptionCode()) );
	}
	
	//-----------------------------------------------------------------
	default boolean startSubNegotiationAs(CommunicationRole role) {
		return false;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a sub-negotiation are expected
	 */
	default boolean negotiateDetails(TelnetProtocol stack, CommunicationRole role) {
		return false;
	}

	//-----------------------------------------------------------------
	/**
	 * Called when a subnegotiation for this option is received
	 */
	public default List<TelnetOptionEvent> handleSubnegotiation(TelnetSubnegotiationEvent event, TelnetProtocol stack) {
		return List.of();
	}

}
