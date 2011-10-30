/**
 * 
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public class WillVariable extends TelnetVariable {

	//-----------------------------------------------------------------
	/**
	 */
	public WillVariable(String name, boolean deflt) {
		super(name, deflt);
	}

	//-----------------------------------------------------------------
	public boolean equals(Object o) {
		if (o instanceof WillVariable) {
			return name.equals(((WillVariable)o).getName());
		}
		return false;
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetVariable#getKey()
	 */
	@Override
	public String getKey() {
		return "WILL/WONT_"+name;
	}

	//-----------------------------------------------------------------
	public String toString() {
		return (status?"WILL":"WONT")+"_"+name;
	}

}
