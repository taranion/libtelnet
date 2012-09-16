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
	public WillVariable(int name, boolean deflt) {
		super(name, deflt);
	}

	//-----------------------------------------------------------------
	public boolean equals(Object o) {
		if (o instanceof WillVariable) {
			return code==((WillVariable)o).getName();
		}
		return false;
	}

	//-----------------------------------------------------------------
	public String toString() {
		return (status?"WILL":"WONT")+"_"+TelnetConfiguration.getOption(code).getName();
	}

}
