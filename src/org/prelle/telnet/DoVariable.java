/**
 * 
 */
package org.prelle.telnet;

/**
 * @author prelle
 *
 */
public class DoVariable extends TelnetVariable {
	
	//-----------------------------------------------------------------
	/**
	 */
	public DoVariable(int name, boolean deflt) {
		super(name, deflt);
	}

	//-----------------------------------------------------------------
	public boolean equals(Object o) {
		if (o instanceof DoVariable) {
			return code==((DoVariable)o).getName();
		}
		return false;
	}

	//-----------------------------------------------------------------
	public String toString() {
		return (status?"Do":"DONT")+"_"+TelnetConfiguration.getOption(code).getName();
	}

}
