package org.prelle.telnet;

public abstract class TelnetVariable {

	protected int code;
	protected boolean status;

	//-----------------------------------------------------------------
	/**
	 * @param name
	 * @param deflt
	 */
	public TelnetVariable(int code, boolean deflt) {
		this.code= code;
		this.status = deflt;
	}

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetVariable() {
		super();
	}

	//-----------------------------------------------------------------
	/**
	 * @return
	 */
	public int getName() {
		return code;
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