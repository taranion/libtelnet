/**
 *
 */
package org.prelle.telnet.mud;

import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.option.TerminalType.TerminalTypeData;

/**
 * @author prelle
 *
 */
public class MUDTerminalTypeData extends TerminalTypeData {

	public static enum Capability {
		ANSI(1),
		VT100(2),
		UTF8(4),
		COLOR256(8),
		MOUSE_TRACKING(16),
		OSC_COLOR_PALETTE(32),
		SCREEN_READER(64),
		PROXY(128),
		TRUECOLOR(256),
		MNES(512),
		MSLP(1024),
		SSL(2048)
		;
		int bit;
		Capability(int bit) {
			this.bit = bit;
		}
		public int getBit() { return bit; }
	}

	private MUDTerminalTypeStandard.RequestState state;
	/**
	 *
	 *       1 "ANSI"              Client supports all common ANSI color codes.
	 *       2 "VT100"             Client supports all common VT100 codes.
	 *       4 "UTF-8"             Client is using UTF-8 character encoding.
	 *       8 "256 COLORS"        Client supports all 256 color codes.
	 *      16 "MOUSE TRACKING"    Client supports xterm mouse tracking.
	 *      32 "OSC COLOR PALETTE" Client supports OSC and the OSC color palette.
	 *      64 "SCREEN READER"     Client is using a screen reader.
	 *     128 "PROXY"             Client is a proxy allowing different users to connect from the same IP address.
	 *     256 "TRUECOLOR"         Client supports truecolor codes using semicolon notation.
	 *     512 "MNES"              Client supports the Mud New Environment Standard for information exchange.
	 *    1024 "MSLP"              Client supports the Mud Server Link Protocol for clickable link handling.
	 *    2048 "SSL"               Client supports SSL for data encryption, preferably TLS 1.3 or higher.
	 */
	private String mudTerminalType;

	public MUDTerminalTypeData() {
		options = new ArrayList<>();
		options.add("-");
		options.add("-");
		options.add("MTTS 0");
	}
	public MUDTerminalTypeData(String... values) {
		options = List.of(values);
	}

	//-----------------------------------------------------------------
	public String toString() {
		return String.format("Client: %s,  Terminal: %s, MTT: %s = %s", getClientName(), getTerminalType(), mudTerminalType, getCapabilities());
	}

	//-----------------------------------------------------------------
	/**
	 * @return the clientName
	 */
	public String getClientName() {
		return options.get(0);
	}
	//-----------------------------------------------------------------
	/**
	 * @param clientName the clientName to set
	 */
	public MUDTerminalTypeData setClientName(String clientName) {
		options.remove(0);
		options.add(0, clientName);
		return this;
	}
	//-----------------------------------------------------------------
	/**
	 * @return the terminalType
	 */
	public String getTerminalType() {
		return options.get(1);
	}
	//-----------------------------------------------------------------
	/**
	 * @param terminalType the terminalType to set
	 */
	public MUDTerminalTypeData setTerminalType(String terminalType) {
		options.remove(1);
		options.add(1, terminalType);
		return this;
	}
	//-----------------------------------------------------------------
	/**
	 * @return the state
	 */
	public MUDTerminalTypeStandard.RequestState getState() {
		return state;
	}
	//-----------------------------------------------------------------
	/**
	 * @param state the state to set
	 */
	public void setState(MUDTerminalTypeStandard.RequestState state) {
		this.state = state;
	}
	//-----------------------------------------------------------------
	/**
	 * @return the mudTerminalType
	 */
	public String getMudTerminalType() {
		return mudTerminalType;
	}

	//-----------------------------------------------------------------
	/**
	 * @param mudTerminalType the mudTerminalType to set
	 */
	public void setMudTerminalType(String mudTerminalType) {
		this.mudTerminalType = mudTerminalType;
	}

	//-----------------------------------------------------------------
	public List<Capability> getCapabilities() {
		if (mudTerminalType==null) return List.of();
		if (!mudTerminalType.startsWith("MTTS "))
			return List.of();
		List<Capability> ret = new ArrayList<MUDTerminalTypeData.Capability>();
		try {
			int vector = Integer.parseInt(mudTerminalType.substring(5));
			return convertToList(vector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	//-----------------------------------------------------------------
	public static List<Capability> convertToList(int vector) {
		List<Capability> ret = new ArrayList<MUDTerminalTypeData.Capability>();
		try {
			for (Capability cap : Capability.values()) {
				if ((vector & cap.getBit())>0)
					ret.add(cap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
