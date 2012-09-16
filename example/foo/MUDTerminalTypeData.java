/**
 * 
 */
package foo;

/**
 * @author prelle
 *
 */
public class MUDTerminalTypeData {

	private MUDTerminalTypeStandard.RequestState state;
	private String clientName;
	private String terminalType;
	private String mudTerminalType;
	
	//-----------------------------------------------------------------
	public String toString() {
		return String.format("Client: %s,  Terminal: %s, MTT: %s", clientName, terminalType, mudTerminalType);
	}
	
	//-----------------------------------------------------------------
	/**
	 * @return the clientName
	 */
	public String getClientName() {
		return clientName;
	}
	//-----------------------------------------------------------------
	/**
	 * @param clientName the clientName to set
	 */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	//-----------------------------------------------------------------
	/**
	 * @return the terminalType
	 */
	public String getTerminalType() {
		return terminalType;
	}
	//-----------------------------------------------------------------
	/**
	 * @param terminalType the terminalType to set
	 */
	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}
	//-----------------------------------------------------------------
	/**
	 * @return the state
	 */
	public MUDTerminalTypeStandard.RequestState getState() {
		return state;
	}
	//-----------------------------------------------------------------
	/**
	 * @param state the state to set
	 */
	public void setState(MUDTerminalTypeStandard.RequestState state) {
		this.state = state;
	}
	//-----------------------------------------------------------------
	/**
	 * @return the mudTerminalType
	 */
	public String getMudTerminalType() {
		return mudTerminalType;
	}
	//-----------------------------------------------------------------
	/**
	 * @param mudTerminalType the mudTerminalType to set
	 */
	public void setMudTerminalType(String mudTerminalType) {
		this.mudTerminalType = mudTerminalType;
	}
}
