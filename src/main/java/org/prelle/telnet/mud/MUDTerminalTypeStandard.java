/**
 *
 */
package org.prelle.telnet.mud;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.option.TerminalType;

/**
 * See http://tintin.sourceforge.net/mtts/
 * @see http://tintin.sourceforge.net/mtts/
 * @author prelle
 *
 *
 */
public class MUDTerminalTypeStandard extends TerminalType {

	static enum RequestState { DEFAULT, CLIENT_NAME, TERMINAL_TYPE, MUD_TERMINAL_TYPE, UNKNOWN}

	public String getName() { return "MTTS"; }

	//-------------------------------------------------------------------
	public MUDTerminalTypeStandard() {
		super();
	}
	
	//-------------------------------------------------------------------
	public String getClientName() {
		return options.isEmpty()?null:options.get(0);
	}

	public String getTerminalType() {
		return (options.size()<2)?null:options.get(1);
	}

	public String getCapabilities() {
		return options.size()<3?null:options.get(2);
	}
	
//	//-------------------------------------------------------------------
//	/**
//	 * Called when all information has been received
//	 */
//	protected void processResult(TelnetSocket nvt, List<String> received) {
//		MUDTerminalTypeData data = new MUDTerminalTypeData();
//		if (!received.isEmpty())
//			data.setClientName(received.get(0));
//		if (received.size()>1)
//			data.setTerminalType(received.get(1));
//		if (received.size()>2)
//			data.setMudTerminalType(received.get(2));
////		nvt.fireOptionDataChanged(this, data);
//	}

	public List<MUDTerminalTypeData.Capability> getCapabilitiesAsList() {
		if (getCapabilities()==null) return List.of();
		ArrayList<MUDTerminalTypeData.Capability> result = new ArrayList<>();
		// Parse terminal type
		switch (getTerminalType().toUpperCase()) {
		case "ANSI":
			result.add(MUDTerminalTypeData.Capability.ANSI);
			break;
		case "ANSI-256COLOR":
			result.add(MUDTerminalTypeData.Capability.ANSI);
			result.add(MUDTerminalTypeData.Capability.COLOR256);
			break;
		case "ANSI-TRUECOLOR":
			result.add(MUDTerminalTypeData.Capability.ANSI);
			result.add(MUDTerminalTypeData.Capability.COLOR256);
			result.add(MUDTerminalTypeData.Capability.TRUECOLOR);
			break;
		case "VT100":
		case "XTERM":
		case "XTERM-256COLOR":
		case "XTERM-TRUEOLOR":
			result.add(MUDTerminalTypeData.Capability.VT100);
			break;
		default:
			logger.log(Level.WARNING, "Unknown terminal type: "+getTerminalType());
		}
		
		if (options.get(1).equalsIgnoreCase("ANSI"))
			result.add(MUDTerminalTypeData.Capability.ANSI);
		String mtts = options.get(2);
		try {
			if (mtts.indexOf(' ')==-1) return MUDTerminalTypeData.convertToList(Integer.parseInt(mtts));
			return MUDTerminalTypeData.convertToList(Integer.parseInt(mtts.split(" ")[1]));
		} catch (NumberFormatException e) {
			logger.log(Level.ERROR, "Failed to parse MTTS capabilities ("+mtts+") of " + options);
			if (mtts.contains("256"))
				result.add(MUDTerminalTypeData.Capability.COLOR256);
			return result;
		}
	}
}
