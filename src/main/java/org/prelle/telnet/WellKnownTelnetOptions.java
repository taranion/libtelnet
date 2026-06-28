package org.prelle.telnet;

/**
 *
 */
public enum WellKnownTelnetOptions {

	TRANSMIT_BINARY( 0),
	ECHO       ( 1),
	SGA        ( 3),
	STATUS     ( 5),
	TIMING_MARK( 6),
	NOCARD     (10),
	TERMINAL_TYPE(24),
	MTT        (24),
	EOR        (25),
	NAWS       (31),
	TERMINAL_SPEED(32),
	LINEMODE   (34),
	NEW_ENVIRON(39),
	CHARSET    (42),
	STARTTLS   (46),

	MSDP       ( 69),
	MSSP       ( 70),
	COMPRESS   ( 85),
	COMPRESS2  ( 86),
	MCCP3      ( 87),
	MCCPX      ( 88),
	MSP        ( 90),
	MXP        ( 91),
	ZMP        ( 93),
	MUSHCLIENT (102),
	ATCP       (200),
	GMCP       (201),
	;

	private int code;

	//-------------------------------------------------------------------
	WellKnownTelnetOptions(int code) {
		this.code = code;
	}

	public int getCode() { return code; }

	//-------------------------------------------------------------------
	public static WellKnownTelnetOptions valueOf(int code) {
		for (WellKnownTelnetOptions opt : WellKnownTelnetOptions.values()) {
			if (opt.code==code) return opt;
		}
		return null;
	}
}
