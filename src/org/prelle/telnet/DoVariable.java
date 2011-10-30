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
	public DoVariable(String name, boolean deflt) {
		super(name, deflt);
	}

	//-----------------------------------------------------------------
	public boolean equals(Object o) {
		if (o instanceof DoVariable) {
			return name.equals(((DoVariable)o).getName());
		}
		return false;
	}

	//-----------------------------------------------------------------
	@Override
	public String getKey() {
		return "DO/DONT_"+name;
	}

	//-----------------------------------------------------------------
	public String toString() {
		return (status?"DO":"DONT")+"_"+name;
	}

}
