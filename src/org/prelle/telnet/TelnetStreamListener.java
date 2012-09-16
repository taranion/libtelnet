/**
 * 
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public interface TelnetStreamListener {

	public void receivedGoAheadSignal();

	public void receivedWILL(int optionCode);

	public void receivedWONT(int optionCode);

	public void receivedDO(int optionCode);

	public void receivedDONT(int optionCode);

	public void receivedSubnegotiationBegin(int next);

	public void receivedInterruptProcess();
	
}
