package org.prelle.telnet.option;

import java.util.Map;

/**
 *
 */
public class TelnetEnvironmentData {

	private Map<String,String> variables;

	//-------------------------------------------------------------------
	public TelnetEnvironmentData(Map<String,String> variables) {
		this.variables = variables;
	}

	//-------------------------------------------------------------------
	public Map<String,String> getVariables() {
		return variables;
	}

}
