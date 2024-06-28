package org.prelle.telnet;

import org.prelle.telnet.mud.GenericMUDCommunicationProtocol;
import org.prelle.telnet.mud.MUDClientCompression1;
import org.prelle.telnet.mud.MUDClientCompression2;
import org.prelle.telnet.mud.MUDExtensionProtocol;
import org.prelle.telnet.mud.MUDServerDataProtocol;
import org.prelle.telnet.mud.MUDServerStatusProtocol;
import org.prelle.telnet.mud.MUDSoundProtocol;
import org.prelle.telnet.mud.MUDTerminalTypeStandard;
import org.prelle.telnet.mud.MUDTilemapProtocol;
import org.prelle.telnet.mud.ZenithMUDProtocol;
import org.prelle.telnet.option.CarriageReturnDisposition;
import org.prelle.telnet.option.EndOfRecord;
import org.prelle.telnet.option.LineMode;
import org.prelle.telnet.option.StatusOption;
import org.prelle.telnet.option.SuppressGoAhead;
import org.prelle.telnet.option.TelnetCharset;
import org.prelle.telnet.option.TelnetEcho;
import org.prelle.telnet.option.TelnetEnvironmentOption;
import org.prelle.telnet.option.TelnetTLS;
import org.prelle.telnet.option.TelnetWindowSize;
import org.prelle.telnet.option.TerminalType;
import org.prelle.telnet.option.TimingMark;
import org.prelle.telnet.option.TransmitBinary;

/**
 *
 */
public enum TelnetOptionDeleteMe {

	TRANSMIT_BINARY( 0, new TransmitBinary(), true),
	ECHO       ( 1, new TelnetEcho()),
	SGA        ( 3, new SuppressGoAhead()),
	STATUS     ( 5, new StatusOption()),
	TIMING_MARK( 6, new TimingMark()),
	NOCARD     (10, new CarriageReturnDisposition()),
	TERMINAL_TYPE(24, new TerminalType()),
	MTT        (24, new MUDTerminalTypeStandard()),
	EOR        (25, new EndOfRecord()),
	NAWS       (31, new TelnetWindowSize(31, "NAWS")),
	LINEMODE   (34, new LineMode()),
	NEW_ENVIRON(39, new TelnetEnvironmentOption()),
	CHARSET    (42, new TelnetCharset()),
	STARTTLS   (46, new TelnetTLS()),

	MSDP       ( 69, new MUDServerDataProtocol()),
	MSSP       ( 70, new MUDServerStatusProtocol()),
	COMPRESS   ( 85, new MUDClientCompression1()),
	COMPRESS2  ( 86, new MUDClientCompression2()),
	MSP        ( 90, new MUDSoundProtocol()),
	MXP        ( 91, new MUDExtensionProtocol()),
	ZMP        ( 93, new ZenithMUDProtocol()),
	MTP        (100, new MUDTilemapProtocol()),
	GMCP       (201, new GenericMUDCommunicationProtocol()),
	;

	private int code;
	private boolean isMode;
	private TelnetOptionHandler handler;
	private boolean serverInitiated;

	TelnetOptionDeleteMe(int code, TelnetOptionHandler handler) {
		this.code = code;
		this.handler = handler;
		this.isMode  = false;
	}
	TelnetOptionDeleteMe(int code, TelnetOptionHandler handler, boolean mode) {
		this.code = code;
		this.handler = handler;
		this.isMode  = mode;
	}

	public int getCode() { return code; }
	public TelnetOptionHandler getOptionHandler() { return handler; }
	public boolean isServerInitiated() { return serverInitiated; }

	//-------------------------------------------------------------------
	/**
	 * @param optionCode
	 * @return
	 */
	public static TelnetOptionDeleteMe valueOf(int optionCode) {
		for (TelnetOptionDeleteMe tmp : TelnetOptionDeleteMe.values()) {
			if (tmp.getCode()==optionCode)
				return tmp;
		}
		return null;
	}
}
