package org.prelle.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.telnet.TelnetSocket.State;

/**
 * 
 */
public class TelnetProtocol {

	public static enum State {
		CREATED,
		OPTION_NEGOTIATION,
		OPTION_SUBNEGOTIATION,
		READY,
		DISCONNECTED
	}

	private final static Logger logger = System.getLogger("telnet.lvl3");
	
	private Map<TelnetSubnegotiationHandler, CommunicationRole> extensions = new HashMap<>();

	private Map<Integer, Boolean> negotiationState;
	/** Stores data related to a specific config option on this connection */
	private TelnetOptionCapabilities optionCaps = new TelnetOptionCapabilities();
	/** Stores callbacks for specific options */
	private Map<Integer, TelnetOptionListener> optionListener = new HashMap<>();
	
	//-------------------------------------------------------------------
	public TelnetProtocol() {
		negotiationState = new HashMap<>();
	}

	//-------------------------------------------------------------------
	public TelnetProtocol add(TelnetSubnegotiationHandler extension, CommunicationRole role) {
		if (!extensions.containsKey(extension)) {
			extensions.put(extension, role);
		}
		return this;
	}

	//-----------------------------------------------------------------
	private TelnetSubnegotiationHandler getExtensionForOption(int optionCode) {
		for (TelnetSubnegotiationHandler ext : extensions.keySet()) {
			if (ext.getOptionCode()==optionCode)
				return ext;
		}
		return null;
	}

	//-----------------------------------------------------------------
	public static void fireTelnetCommand(TelnetCommand command) {
		logger.log(Level.ERROR, "fire {0}", command);
//		for (TelnetSocketListener list : socketListener)
//			try {
//				list.telnetCommandReceived(this, command);
//			} catch (Exception e) {
//				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetCommandReceived: "+e,e);
//			}
	
	}

	//-----------------------------------------------------------------
	private void handleDoDontWillWont(TelnetInputStreamNG from, TelnetCommand command) throws IOException {
		int optionCode = command.getData();
		TelnetOption option = TelnetOption.valueOf(optionCode);
		// Did we already negotiate this option? If so, don't respond to prevent loops
		if (negotiationState.containsKey(optionCode)) {
			logger.log(Level.DEBUG, "Already negotiated {0} - ignoring", option);
			return;
		}
		// No, this is a new option
		TelnetSubnegotiationHandler extension = getExtensionForOption(optionCode);
		if (extension!=null) {
			// This is an extension we support
			logger.log(Level.DEBUG, "{0} {1}", command, extension.getName());
			// Which role are we?
			CommunicationRole role = extensions.get(extension);
			confirm(from.getReverseStream(), command);
			extension.initializeAs(role, from, from.getReverseStream());
		} else {
			logger.log(Level.WARNING, "No extension found for {0} - rejecting", option);
			reject(from.getReverseStream(), command);
		}
	}

	//-----------------------------------------------------------------
	void processCommand(TelnetInputStreamNG from, TelnetCommand command) throws IOException {
		logger.log(Level.WARNING, "RCV "+command);
		switch (command.getCode()) {
		case DO  : case DONT:
		case WILL: case WONT:
			handleDoDontWillWont(from, command);
			return;
		default:
			logger.log(Level.DEBUG, "fire "+command);
			fireTelnetCommand(command);
			return;
		}
	}

	//-------------------------------------------------------------------
	private void confirm(TelnetOutputStream out, TelnetCommand command) throws IOException {
		negotiationState.put(command.getData(), true);
		if (out==null) {
			logger.log(Level.ERROR, "Cannot confirm {0} because output stream is null", command);
			return;
		}
		logger.log(Level.INFO, "confirm "+command);
		switch (command.getCode()) {
		case DO  : out.sendWill(command.getData()); break;
		case WILL: out.sendDo(command.getData()); break;
		}
	}

	//-------------------------------------------------------------------
	private void reject(TelnetOutputStream out, TelnetCommand command) throws IOException {
		negotiationState.put(command.getData(), false);
		if (out==null) {
			logger.log(Level.ERROR, "Cannot reject {0} because output stream is null", command);
			return;
		}
		switch (command.getCode()) {
		case DO  : out.sendWont(command.getData()); break;
		case WILL: out.sendDont(command.getData()); break;
		}
	}

	//-------------------------------------------------------------------
	public void processSubnegotiation(TelnetInputStreamNG from, int code, int[] values) {
		logger.log(Level.WARNING, "RCV Subnegotiation for {0}: {1}", code, Arrays.toString((values)));

		TelnetSubnegotiationHandler handler = getExtensionForOption(code);
		if (handler==null) {
			logger.log(Level.WARNING, "Received {2} bytes subnegotiation for {0}/{1}, but cannot find a TelnetOptionHandler", code, TelnetOption.valueOf(code), values.length);
			return;
		}

		handler.handleSubnegotiation(code, values, from, from.getReverseStream());
	}
	
	//-----------------------------------------------------------------
	public TelnetProtocol setOptionListener(int code, TelnetOptionListener callback) {
		logger.log(Level.DEBUG, "Send events for option {0} to {1}", code, callback);
		if (callback==null)
			throw new NullPointerException();
		optionListener.put(code, callback);
		return this;
	}

	//-----------------------------------------------------------------
	public <E extends TelnetOptionListener> E getOptionListener(int code) {
		return (E) optionListener.get(code);
	}

	//-----------------------------------------------------------------
	/**
	 * Retrieve data related to a specific config option on this connection
	 */
	public <E> E getOptionData(int code) {
		return optionCaps.getOptionData(code);
	}

	//-----------------------------------------------------------------
	/**
	 * Store data related to a specific config option on this connection
	 */
	public void setOptionData(int code, Object value) {
		optionCaps.setOptionData(code, value);
	}

	//-------------------------------------------------------------------
	/**
	 * Called by TelnetSubnegotiationHandler implementor classes
	 */
	public void subnegotiationEndedFor(int optionCode, Object data) {
		setOptionData(optionCode, data);
		logger.log(Level.INFO, "Negotiation for option {0}/{2} received {1}", optionCode, data, TelnetOption.valueOf(optionCode));
		logger.log(Level.DEBUG, "expected are "+optionCaps.capSubNegAwaitResponses);
//		logger.log(Level.DEBUG, "status is "+state);
//
//		if (state==State.OPTION_SUBNEGOTIATION) {
//			synchronized (optionCaps.capSubNegAwaitResponses) {
//				if (optionCaps.capSubNegAwaitResponses.contains( (Integer)optionCode)) {
//					optionCaps.capSubNegAwaitResponses.remove((Integer)optionCode);
//					if (optionCaps.capSubNegAwaitResponses.isEmpty()) {
//						logger.log(Level.WARNING, "DONE SUBNEG------------------------------");
//						optionCaps.capSubNegAwaitResponses.notify();
//					}
//				}
//			}
//		}
	}

	//-------------------------------------------------------------------
	public TelnetOptionCapabilities getNegotiationResult() {
		return optionCaps;
	}
	
}
