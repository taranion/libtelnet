package org.prelle.telnet;

import org.prelle.telnet.mud.GenericMUDCommunicationProtocol;
import org.prelle.telnet.mud.MUDTerminalTypeStandard;
import org.prelle.telnet.mud.MUDTilemapProtocol;
import org.prelle.telnet.option.LineMode;
import org.prelle.telnet.option.MXPOption;
import org.prelle.telnet.option.StatusOption;
import org.prelle.telnet.option.TelnetCharset;
import org.prelle.telnet.option.TelnetEnvironmentOption;
import org.prelle.telnet.option.TelnetWindowSize;
import org.prelle.telnet.option.TerminalType;

/**
 *
 */
public enum TelnetOption {

	TRANSMIT_BINARY( 0, null),
	ECHO       ( 1, null),
	SGA        ( 3, null),
	STATUS     ( 5, new StatusOption()),
	TIMING_MARK( 6, null),
	NOCARD     (10, null),
	TERMINAL_TYPE(24, new TerminalType()),
	MTT        (24, new MUDTerminalTypeStandard()),
	EOR        (25, null),
	NAWS       (31, new TelnetWindowSize()),
	TERMINAL_SPEED(32, null),
	LINEMODE   (34, new LineMode()),
	NEW_ENVIRON(39, new TelnetEnvironmentOption()),
	CHARSET    (42, new TelnetCharset()),
	STARTTLS   (46, null),

	MSDP       ( 69, null),
	MSSP       ( 70, null),
	COMPRESS   ( 85, null),
	COMPRESS2  ( 86, null),
	MCCP3      ( 87, null),
	MCCPX      ( 88, null),
	MSP        ( 90, null),
	MXP        ( 91, new MXPOption()),
	ZMP        ( 93, null),
	MTP        (100, new MUDTilemapProtocol()),
	MUSHCLIENT (102, null),
	ATCP       (200, null),
	GMCP       (201, new GenericMUDCommunicationProtocol()),
	;

	private int code;
	private TelnetSubnegotiationHandler handler;

	//-------------------------------------------------------------------
	TelnetOption(int code, TelnetSubnegotiationHandler handler) {
		this.code = code;
		this.handler = handler;
	}

	public int getCode() { return code; }
	public TelnetSubnegotiationHandler getOptionHandler() { return handler; }

	//-------------------------------------------------------------------
	public static TelnetOption valueOf(int code) {
		for (TelnetOption opt : TelnetOption.values()) {
			if (opt.code==code) return opt;
		}
		return null;
	}
}
