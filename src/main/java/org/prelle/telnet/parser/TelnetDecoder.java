package org.prelle.telnet.parser;

import java.io.ByteArrayOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.event.TelnetEventFactory;
import org.prelle.telnet.event.TelnetParserListener;
import org.prelle.telnet.event.internal.DefaultTelnetEventFactory;
import org.prelle.telnet.parser.TelnetConstants.ControlCode;

/**
 * Pure push-based Telnet protocol byte-slicer and state decoder.
 * Buffers incomplete Telnet commands and subnegotiation frames across chunk boundaries,
 * delegates all Telnet commands and subnegotiations to TelnetProtocol,
 * and forwards clean display bytes downstream.
 */
public class TelnetDecoder {

	private final static Logger logger = System.getLogger("telnet.lvl3");
	private final static DefaultTelnetEventFactory DEFAULT_FACTORY = new DefaultTelnetEventFactory();

	// Telnet protocol constants
	public static final int IAC  = 255;
	public static final int DONT = 254;
	public static final int DO   = 253;
	public static final int WONT = 252;
	public static final int WILL = 251;
	public static final int SB   = 250;
	public static final int GA   = 249;
	public static final int EOR  = 239;
	public static final int SE   = 240;

	private enum State {
		DATA,
		IAC_RECEIVED,
		WILL_RECEIVED,
		WONT_RECEIVED,
		DO_RECEIVED,
		DONT_RECEIVED,
		SB_OPTION_RECEIVED,
		SUBNEG_DATA,
		SUBNEG_IAC_RECEIVED
	}

	private TelnetEventFactory factory;
	private TelnetParserListener listener;

	private State currentState = State.DATA;
	private int currentOptionCode = -1;
	private final ByteArrayOutputStream subnegBuffer = new ByteArrayOutputStream();
	private final ByteArrayOutputStream cleanBuffer = new ByteArrayOutputStream();
	private boolean sendGoAheadAsANSISeparator = false;

	//-------------------------------------------------------------------
	public TelnetDecoder(TelnetParserListener listener) {
		this(listener, DEFAULT_FACTORY);
	}

	//-------------------------------------------------------------------
	public TelnetDecoder(TelnetParserListener listener, TelnetEventFactory factory) {
		this.factory = factory;
		this.listener = listener;
	}

	//-------------------------------------------------------------------
	public boolean isSendGoAheadAsANSISepator() {
		return sendGoAheadAsANSISeparator;
	}

	//-------------------------------------------------------------------
	public void setSendGoAheadAsANSISepator(boolean sendGoAheadAsANSISepator) {
		this.sendGoAheadAsANSISeparator = sendGoAheadAsANSISepator;
	}

	private void releaseCleanBuffer() {
		if (cleanBuffer.size() > 0 && listener != null) {
			listener.onTelnetEvent(factory.createDataEvent(cleanBuffer.toByteArray()));
			cleanBuffer.reset();
		}
	}
	
	//-------------------------------------------------------------------
	public synchronized void process(byte[] data) {
		if (data == null || data.length == 0) return;

		cleanBuffer.reset();

		for (byte b : data) {
			int bUnsigned = b & 0xFF;
			processByte(bUnsigned);
		}

		releaseCleanBuffer();
	}

	//-------------------------------------------------------------------
	private void processByte(int b) {
		switch (currentState) {
		case DATA:
			if (b == IAC) {
				currentState = State.IAC_RECEIVED;
			} else {
				cleanBuffer.write(b);
			}
			break;

		case IAC_RECEIVED:
			switch (b) {
			case IAC:
				// Escaped IAC byte (255 255) -> literal 255 byte in data stream
				cleanBuffer.write(IAC);
				currentState = State.DATA;
				break;
			case WILL:
				releaseCleanBuffer();
				currentState = State.WILL_RECEIVED;
				break;
			case WONT:
				releaseCleanBuffer();
				currentState = State.WONT_RECEIVED;
				break;
			case DO:
				releaseCleanBuffer();
				currentState = State.DO_RECEIVED;
				break;
			case DONT:
				releaseCleanBuffer();
				currentState = State.DONT_RECEIVED;
				break;
			case SB:
				releaseCleanBuffer();
				subnegBuffer.reset();
				currentState = State.SB_OPTION_RECEIVED;
				break;
			case GA:
				if (sendGoAheadAsANSISeparator) {
					cleanBuffer.write(0x1E); // ASCII Record Separator (RS) for GA
					releaseCleanBuffer();
					currentState = State.DATA;
					break;
				} // Fall through to default if not sending GA as ANSI separator
			case EOR:
				// Line/Prompt delimiter in some MUDs - pass command to TelnetProtocol
				releaseCleanBuffer();
				listener.onTelnetEvent(factory.createTelnetCommand(ControlCode.getCodeFor(b)));
				currentState = State.DATA;
				break;
			default:
				// Other Telnet control command (NOP, BREAK, etc.)
				releaseCleanBuffer();
				listener.onTelnetEvent(factory.createTelnetCommand(ControlCode.getCodeFor(b)));
				currentState = State.DATA;
				break;
			}
			break;

		case WILL_RECEIVED:
			listener.onTelnetEvent( factory.createTelnetNegotiationEvent(ControlCode.WILL, b));
			currentState = State.DATA;
			break;

		case WONT_RECEIVED:
			listener.onTelnetEvent( factory.createTelnetNegotiationEvent(ControlCode.WONT, b));
			currentState = State.DATA;
			break;

		case DO_RECEIVED:
			listener.onTelnetEvent( factory.createTelnetNegotiationEvent(ControlCode.DO, b));
			currentState = State.DATA;
			break;

		case DONT_RECEIVED:
			listener.onTelnetEvent( factory.createTelnetNegotiationEvent(ControlCode.DONT, b));
			currentState = State.DATA;
			break;

		case SB_OPTION_RECEIVED:
			currentOptionCode = b;
			currentState = State.SUBNEG_DATA;
			break;

		case SUBNEG_DATA:
			if (b == IAC) {
				currentState = State.SUBNEG_IAC_RECEIVED;
			} else {
				subnegBuffer.write(b);
			}
			break;

		case SUBNEG_IAC_RECEIVED:
			if (b == SE) {
				// Subnegotiation complete!
				listener.onTelnetEvent(factory.createTelnetSubnegotiationEvent(currentOptionCode, subnegBuffer.toByteArray()));
				currentState = State.DATA;
			} else if (b == IAC) {
				// Escaped IAC (255 255) inside subnegotiation data
				subnegBuffer.write(IAC);
				currentState = State.SUBNEG_DATA;
			} else {
				// Unexpected byte following IAC inside subnegotiation
				logger.log(Level.WARNING, "Unexpected byte {0} following IAC inside subnegotiation", b);
				currentState = State.DATA;
			}
			break;
		}
	}

	//-------------------------------------------------------------------
	public synchronized void reset() {
		currentState = State.DATA;
		currentOptionCode = -1;
		subnegBuffer.reset();
		cleanBuffer.reset();
	}
}
