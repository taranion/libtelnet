package org.prelle.telnet;

/**
 * 
 */
public enum NegotiationState {
	
	DO_SENT,
	/** DO sent, WILL received */
	REMOTE_CONFIRMED,
	/** DO sent, WONT received */
	REMOTE_REJECTED,

}
