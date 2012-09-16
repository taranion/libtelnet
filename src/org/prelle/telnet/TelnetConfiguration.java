/**
 * 
 */
package org.prelle.telnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prelle.telnet.option.TelnetOption;

/**
 * @author prelle
 *
 */
public class TelnetConfiguration {

	private final static Logger logger = Logger.getLogger("telnet");

	private static Map<Integer, TelnetOption> options = new HashMap<Integer, TelnetOption>();

	//-----------------------------------------------------------------
	public static Collection<TelnetOption> getKnownOptions() {
		List<TelnetOption> ret = new ArrayList<TelnetOption>(options.values());
		Collections.sort(ret, new Comparator<TelnetOption>() {
			public int compare(TelnetOption o1, TelnetOption o2) {
				return ((Integer)o1.getCode()).compareTo(o2.getCode());
			}
		});
		return options.values();
	}

	//-----------------------------------------------------------------
	public static void registerOption(TelnetOption option) {
		logger.debug(String.format("Register option %d/%s with %s", option.getCode(), option.getName(), option.getClass().getName()));
		options.put(option.getCode(), option);
		
//		setEnabled(option.getCode(), option.isEnabledByDefault());
	}

//	//-----------------------------------------------------------------
//	public static void registerOption(TelnetOption option, boolean enable) {
//		logger.debug(String.format("Register option %d/%s with %s", option.getCode(), option.getName(), option.getClass().getName()));
//		options.put(option.getCode(), option);
//		
//		setEnabled(option.getCode(), enable);
//	}

	//-----------------------------------------------------------------
	public static TelnetOption getOption(int optionCode) {
		return options.get(optionCode);
	}

//	//--------------------------------------------------------------
//	public static void setEnabled(int optionCode, boolean enabled) {
//		TelnetOption option = options.get(optionCode);
//		assert option!=null;
//		
//		isEnabled.put(optionCode, enabled);
//		logger.debug((enabled?"Enable":"Disable")+" "+option.getName());
//	}
//		
//	//--------------------------------------------------------------
//	public static boolean isOptionEnabled(int optionCode) {
//		Boolean enabled = isEnabled.get(optionCode);
//		if (enabled==null)
//			return false;
//		return enabled;
//	}
//
//	//-----------------------------------------------------------------
//	public static Collection<TelnetOption> getDefaultOptions() {
//		List<TelnetOption> ret = new ArrayList<TelnetOption>();
//		for (Entry<Integer, Boolean> entry : isEnabled.entrySet())
//			if (entry.getValue()) {
//				ret.add(getOption(entry.getKey()));
//			}
//		return ret;
//	}

}
