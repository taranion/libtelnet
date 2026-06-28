package org.prelle.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * 
 */
public class TelnetProtocol {

	public static enum State {
		SUGGESTED,
		CONFIRMED,
		REJECTED,
		OPTION_SUBNEGOTIATION,
		READY,
		DISCONNECTED
	}
	
	
	private static class NegotiatonState {
		State state;
		ControlCode lastSent;
		public NegotiatonState(State state, ControlCode lastSent) {
			this.state = state;
			this.lastSent = lastSent;
		}
		public String toString() {
			return state+"("+lastSent+")";
		}
	}

	private final static Logger logger = System.getLogger("telnet.lvl3");
	
	private CommunicationRole role;
	private Map<TelnetSubnegotiationHandler, CommunicationRole> extensions = new HashMap<>();

	private Map<Integer, NegotiatonState> negotiationState;
	/** Stores data related to a specific config option on this connection */
	private TelnetOptionCapabilities optionCaps = new TelnetOptionCapabilities();
	/** Stores callbacks for specific options */
	private Map<Integer, TelnetOptionListener> optionListener = new HashMap<>();
	
	private TelnetInputStreamNG inputStream;
	private TelnetOutputStream outputStream;
    
    private List<TelnetListener> listener;
	
	//-------------------------------------------------------------------
	public TelnetProtocol(CommunicationRole role) {
		this.role = role;
		negotiationState = new HashMap<>();
		listener  = new ArrayList<>();
	}

	//-------------------------------------------------------------------
	public TelnetProtocol add(TelnetSubnegotiationHandler extension) {
		if (!extensions.containsKey(extension)) {
			extensions.put(extension, role);
		}
		return this;
	}

	//-----------------------------------------------------------------
	public void addListener(TelnetListener listener) {
		if (this.listener==null)
			this.listener = new ArrayList<>();
		this.listener.add(listener);
	}
	
	//-------------------------------------------------------------------
	public void initializeExtensions() {
		logger.log(Level.DEBUG, "ENTER: initializeExtensions");
		for (TelnetSubnegotiationHandler ext : extensions.keySet()) {
			try {
				if (ext.startCommunicationAs(role)) {
					ControlCode code = ext.initiate(this, role);
					negotiationState.put(ext.getOptionCode(), new NegotiatonState(State.SUGGESTED, code));
				}
			} catch (Exception e) {
				logger.log(Level.ERROR, "Error initializing extension "+ext.getName(), e);
			}
		}
		try {
			outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.log(Level.DEBUG, "LEAVE: initializeExtensions");
	}

	//-----------------------------------------------------------------
	TelnetSubnegotiationHandler getExtensionForOption(int optionCode) {
		for (TelnetSubnegotiationHandler ext : extensions.keySet()) {
			if (ext.getOptionCode()==optionCode)
				return ext;
		}
		return null;
	}

	//-----------------------------------------------------------------
	String getExtensionName(int optionCode) {
		for (TelnetSubnegotiationHandler ext : extensions.keySet()) {
			if (ext.getOptionCode()==optionCode)
				return ext.getName();
		}
		return (WellKnownTelnetOptions.valueOf(optionCode)!=null)?WellKnownTelnetOptions.valueOf(optionCode).name():"UNKNOWN("+optionCode+")";
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
	private void handleDoDontWillWontResponse(TelnetInputStreamNG from, TelnetCommand command, NegotiatonState state) throws IOException {
		TelnetSubnegotiationHandler extension = getExtensionForOption(command.getData());
		switch (state.state) {
		case SUGGESTED:
			// We already sent a suggestion, so this is a response to that
			if (state.lastSent==ControlCode.WILL && command.getCode()==ControlCode.DO) {
				// We suggested WILL, and the other side said DO - confirmed
				state.state = State.CONFIRMED;
				if (extension.startCommunicationAs(role)) {
					extension.negotiateDetails(this);
					state.state = State.OPTION_SUBNEGOTIATION;
				}
				listener.forEach(callback -> callback.optionStateChanged(extension, true));
			} else if (state.lastSent==ControlCode.DO && command.getCode()==ControlCode.WILL) {
				// We suggested DO, and the other side said WILL - confirmed
				state.state = State.CONFIRMED;
				if (extension.startCommunicationAs(role)) {
					extension.negotiateDetails(this);
					state.state = State.OPTION_SUBNEGOTIATION;
				}
				listener.forEach(callback -> callback.optionStateChanged(extension, true));
			} else if (state.lastSent==ControlCode.WILL && command.getCode()==ControlCode.DONT) {
				// We suggested WILL, and the other side said DONT - rejected
				state.state = State.REJECTED;
				listener.forEach(callback -> callback.optionStateChanged(extension, false));
			} else if (state.lastSent==ControlCode.DO && command.getCode()==ControlCode.WONT) {
				// We suggested DO, and the other side said WONT - rejected
				state.state = State.REJECTED;
				listener.forEach(callback -> callback.optionStateChanged(extension, false));
		}
			break;
		default:
			logger.log(Level.WARNING, "Received {0} while state is {1} - ignoring", command, state);
		}
	}

	//-----------------------------------------------------------------
	private void handleDoDontWillWont(TelnetInputStreamNG from, TelnetCommand command) throws IOException {
		int optionCode = command.getData();
		var option = WellKnownTelnetOptions.valueOf(optionCode);
		NegotiatonState state = negotiationState.get(optionCode);
		logger.log(Level.INFO, "Received {0} while state is {1}", command, state);
		// Did we already negotiate this option? If so, don't respond to prevent loops
		if (state!=null) {
			handleDoDontWillWontResponse(from, command, state);
			return;
		}
		// No, this is a new option
		TelnetSubnegotiationHandler extension = getExtensionForOption(optionCode);
		if (extension!=null) {
			// This is an extension we support
			logger.log(Level.DEBUG, "{0} {1}", command, extension.getName());
			confirm(from.getReverseStream(), command);
		} else {
			logger.log(Level.WARNING, "No extension found for {0} - rejecting", option);
			reject(from.getReverseStream(), command);
		}
	}

	//-----------------------------------------------------------------
	void processCommand(TelnetInputStreamNG from, TelnetCommand command) throws IOException {
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
	private void handleNewlyConfirmed(NegotiatonState oldState, int code, ControlCode confirmedWith) {
		TelnetSubnegotiationHandler extension = getExtensionForOption(code);
		if (oldState==null || oldState.state==State.SUGGESTED) {
			// Inform the listener that the option is now confirmed
			listener.forEach(callback -> callback.optionStateChanged(extension, true));
		}
	}

	//-------------------------------------------------------------------
	private void handleNewlyRejected(NegotiatonState oldState, int code, ControlCode confirmedWith) {
		TelnetSubnegotiationHandler extension = getExtensionForOption(code);
		if (oldState==null || oldState.state==State.SUGGESTED) {
			// Inform the listener that the option is now rejected
			listener.forEach(callback -> callback.optionStateChanged(extension, false));
		}
		
	}

	//-------------------------------------------------------------------
	private void confirm(TelnetOutputStream out, TelnetCommand command) throws IOException {
		if (out==null) {
			logger.log(Level.ERROR, "Cannot confirm {0} because output stream is null", command);
			return;
		}
		logger.log(Level.DEBUG, "confirm "+command);

		NegotiatonState state = negotiationState.get(command.getData());
		switch (command.getCode()) {
		case DO  : 
			out.sendWill(command.getData());
			if (state==null) {
				handleNewlyConfirmed(state, command.getData(), command.getCode());
				state = new NegotiatonState(State.CONFIRMED, ControlCode.WILL);
				negotiationState.put(command.getData(), state);
			} else {
				handleNewlyConfirmed(state, command.getData(), command.getCode());
				state.lastSent = ControlCode.WILL;
				state.state = State.CONFIRMED;
			}
			break;
		case WILL: 
			out.sendDo(command.getData()); 
			handleNewlyRejected(state, command.getData(), command.getCode());
			if (state==null) {
				state = new NegotiatonState(State.CONFIRMED, ControlCode.DO);
				negotiationState.put(command.getData(), state);
			} else {
				state.lastSent = ControlCode.DO;
				state.state = State.CONFIRMED;
			}
			break;
		}
	}

	//-------------------------------------------------------------------
	private void reject(TelnetOutputStream out, TelnetCommand command) throws IOException {
		if (out==null) {
			logger.log(Level.ERROR, "Cannot reject {0} because output stream is null", command);
			return;
		}
		NegotiatonState state = negotiationState.get(command.getData());
		switch (command.getCode()) {
		case DO  : 
			out.sendWont(command.getData()); 
			if (state==null) {
				state = new NegotiatonState(State.REJECTED, ControlCode.WONT);
				negotiationState.put(command.getData(), state);
			} else {
				state.lastSent = ControlCode.WONT;
				state.state = State.REJECTED;
			}
			break;
		case WILL: 
			out.sendDont(command.getData());
			if (state==null) {
				state = new NegotiatonState(State.REJECTED, ControlCode.DONT);
				negotiationState.put(command.getData(), state);
			} else {
				state.lastSent = ControlCode.DONT;
				state.state = State.REJECTED;
			}
			break;
		}
	}

	//-------------------------------------------------------------------
	public void processSubnegotiation(TelnetInputStreamNG from, int code, int[] values) {
		logger.log(Level.TRACE, "RCV Subnegotiation for {0}: {1}", code, Arrays.toString((values)));

		TelnetSubnegotiationHandler handler = getExtensionForOption(code);
		if (handler==null) {
			logger.log(Level.WARNING, "Received {2} bytes subnegotiation for {0}/{1}, but cannot find a TelnetOptionHandler", code, WellKnownTelnetOptions.valueOf(code), values.length);
			return;
		}

		handler.handleSubnegotiation(values, this);
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
		logger.log(Level.INFO, "Negotiation for option {0}/{2} received {1}", optionCode, data, WellKnownTelnetOptions.valueOf(optionCode));
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

	//-------------------------------------------------------------------
	/**
	 * @return the inputStream
	 */
	public TelnetInputStreamNG getInputStream() {
		return inputStream;
	}

	//-------------------------------------------------------------------
	/**
	 * @param inputStream the inputStream to set
	 */
	public void setInputStream(TelnetInputStreamNG inputStream) {
		this.inputStream = inputStream;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the outputStream
	 */
	public TelnetOutputStream getOutputStream() {
		return outputStream;
	}

	//-------------------------------------------------------------------
	/**
	 * @param outputStream the outputStream to set
	 */
	public void setOutputStream(TelnetOutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
}
