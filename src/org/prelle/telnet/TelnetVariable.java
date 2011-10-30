package org.prelle.telnet;

public abstract class TelnetVariable {

	protected String name;
	protected boolean status;

	//-----------------------------------------------------------------
	/**
	 * @param name
	 * @param deflt
	 */
	public TelnetVariable(String name, boolean deflt) {
		this.name= name;
		this.status = deflt;
	}

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetVariable() {
		super();
	}
	
	//-----------------------------------------------------------------
	public abstract String getKey();

	//-----------------------------------------------------------------
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	//-----------------------------------------------------------------
	/**
	 * @return
	 */
	public boolean getState() {
		return status;
	}

	//-----------------------------------------------------------------
	/**
	 * @param status the status to set
	 */
	public void setState(boolean status) {
		this.status = status;
	}

}