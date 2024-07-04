package org.prelle.telnet;

public enum Role {
	/** Start exchange with WILL */
	PROVIDER,
	/** Start exchange with DO */
	REQUESTER,
	/** Like PROVIDER but not sent upon initialization */
	PROVIDER_SILENT,
	/** Immediately send WONT - breaks regular option negotiation */
	REJECT_OUTRIGHT

}