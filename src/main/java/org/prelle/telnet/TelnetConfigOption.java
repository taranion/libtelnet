package org.prelle.telnet;

/**
 *
 */
public class TelnetConfigOption {

	private Boolean configurable;
	private Boolean state;

	//-------------------------------------------------------------------
	public TelnetConfigOption() {
		// TODO Auto-generated constructor stub
	}

	public void setConfigurable(boolean value) { configurable = value; }
	public Boolean isConfigurable() { return configurable; }

	public void setActive(boolean value) { state = value; }
	public Boolean isActive() { return (state!=null)?state:false; }

	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (configurable==null)
			buf.append(String.format("%16s", "     UNKNOWN"));
		else if (configurable)
			buf.append(String.format("%16s", "CONFIGURABLE"));
		else
			buf.append(String.format("%16s", "NOT CONFIGURABLE"));
		buf.append(" / ");
		if (state==null)
			buf.append(String.format("%8s", "UNKNOWN"));
		else if (state)
			buf.append(String.format("%8s", "ACTIVE"));
		else
			buf.append(String.format("%8s", "INACTIVE"));

		return buf.toString();
	}
}
