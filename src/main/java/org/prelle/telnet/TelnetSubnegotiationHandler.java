/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;

/**
 * @author prelle
 *
 */
public interface TelnetSubnegotiationHandler<E extends  TelnetOptionListener> extends TelnetConstants {
	
	//-----------------------------------------------------------------
	int getOptionCode();
	
	//-----------------------------------------------------------------
	String getName();
	
	//-----------------------------------------------------------------
	default String resolveSubCommandName(int position, byte b) {
		return String.valueOf(b);
	}
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	default boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @param role 
	 * @return TRUE when answers to a sub-negotiation are expected
	 */
	default ControlCode initiate(TelnetProtocol stack, CommunicationRole role) throws IOException {
		stack.getOutputStream().sendDo(getOptionCode());
		return ControlCode.DO;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a sub-negotiation are expected
	 */
	boolean negotiateDetails(TelnetProtocol stack);

	//-----------------------------------------------------------------
	/**
	 * Called when a subnegotiation for this option is received
	 */
	void handleSubnegotiation(int[] values, TelnetProtocol stack);
	
	//-----------------------------------------------------------------
	public void addListener(E listener);

}
