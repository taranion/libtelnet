package org.prelle.telnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 *
 */
public class TelnetOptionCapabilities {

	/** Options both sides agreed upon */
//	private List<Integer> active = new ArrayList<>();
	/** Configuration per option */
	private Map<Integer, Object> configData = new LinkedHashMap<>();
	List<Integer> capExchangeAwaitResponses = new ArrayList<>();
	List<Integer> capSubNegAwaitResponses = new ArrayList<>();
	Map<TelnetOption, TelnetConfigOption> capabilities;

	//-------------------------------------------------------------------
	/**
	 */
	public TelnetOptionCapabilities() {
		capabilities   = new HashMap<>();
	}

	//-----------------------------------------------------------------
	/**
	 * Retrieve data related to a specific config option on this connection
	 */
	public <E> E getOptionData(int code) {
		return (E) configData.get(code);
	}

	//-------------------------------------------------------------------
	public Set<Entry<Integer, Object>> getOptionData() {
		return configData.entrySet();
	}

	//-----------------------------------------------------------------
	/**
	 * Store data related to a specific config option on this connection
	 */
	public void setOptionData(int code, Object value) {
		configData.put(code, value);
	}

	//-------------------------------------------------------------------
	public TelnetConfigOption getConfigOption(TelnetOption key) {
		return capabilities.get(key);
	}

	//-------------------------------------------------------------------
	public Set<Entry<TelnetOption, TelnetConfigOption>> getCapabilities() {
		return capabilities.entrySet();
	}

}
