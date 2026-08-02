package org.prelle.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.prelle.telnet.TelnetConstants.ControlCode;
import org.prelle.telnet.TelnetInputStream.TelnetInputStreamListener;

/**
 * 
 */
public class TelnetProtocol implements TelnetInputStreamListener {

	private final static Logger logger = System.getLogger("telnet.lvl3");

	public static enum State {
		SUGGESTED,
		CONFIRMED,
		REJECTED,
		OPTION_SUBNEGOTIATION,
		READY,
		DISCONNECTED
		;
		public boolean isWaitState() {
			return this==SUGGESTED || this==OPTION_SUBNEGOTIATION;
		}
	}
	
	
	private static class NegotiatonState {
		private TelnetOption<?> extension;
		private State state;
		ControlCode lastSent;
		public NegotiatonState(TelnetOption<?> extension, State state, ControlCode lastSent) {
			this.extension = extension;
			this.state = state;
			this.lastSent = lastSent;
		}
		public String toString() {
			return state+"("+lastSent+")";
		}
		public void setState(State state) {
			if (this.state==state) return;
			logger.log(Level.INFO, "Change state of {0} from {1} to {2}", extension.getName(), this.state, state);
			this.state = state;
		}
	}
	
	private CommunicationRole role;
	private Map<TelnetOption, CommunicationRole> extensions = new HashMap<>();

	private Map<Integer, NegotiatonState> negotiationState;
	
	private TelnetInputStream inputStream;
	private TelnetOutputStream outputStream;
    
    private List<TelnetListener> listener;
    private List<Integer> processLater = new ArrayList<>();
    private boolean continueReading;
	
	//-------------------------------------------------------------------
	public TelnetProtocol(CommunicationRole role) {
		this.role = role;
		negotiationState = new HashMap<>();
		listener  = new ArrayList<>();
	}

	//-------------------------------------------------------------------
	public TelnetProtocol add(TelnetOption extension) {
		if (!extensions.containsKey(extension)) {
			extensions.put(extension, role);
		}
		return this;
	}

	//-----------------------------------------------------------------
	public void addListener(TelnetListener listener) {
		Objects.requireNonNull(listener, "Listener cannot be null");
		if (this.listener==null)
			this.listener = new ArrayList<>();
		this.listener.add(listener);
	}
	
	//-------------------------------------------------------------------
	public void initializeExtensions() {
		logger.log(Level.INFO, "ENTER: initializeExtensions() with {0} extensions", extensions.size());
		try {
			if (!extensions.isEmpty())
				Objects.requireNonNull(outputStream, "Output stream must be set before initializing extensions");
			for (TelnetOption<?> ext : extensions.keySet()) {
				try {
					if (ext.startCommunicationAs(role)) {
						ControlCode code = ext.initiate(this, role);
						negotiationState.put(ext.getOptionCode(), new NegotiatonState(ext, State.SUGGESTED, code));
					}
				} catch (Exception e) {
					logger.log(Level.ERROR, "Error initializing extension "+ext.getName(), e);
				}
			}
			try {
				if (outputStream!=null)
					outputStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			logger.log(Level.INFO, "LEAVE: initializeExtensions");
		}
	}

	//-----------------------------------------------------------------
	private void startReadFromSocketThread() {
		Runnable run = () -> {
			continueReading = true;
			while (continueReading) {
				try {
//					logger.log(Level.INFO, "calling inputStream.read() with {0}", continueReading);
					int data = inputStream.read();
					if (data==-1) {
						logger.log(Level.ERROR, "End of stream reached");
						continueReading = false;
						break;
					}
					processLater.add(data);
					if (processLater.size()>4096) {
						logger.log(Level.ERROR, "Receiving more than 4K data already in Telnet negotiation.... closing connection.");
						processLater.clear();
						continueReading = false;
						outputStream.close();
						inputStream.close();
						return;
					}
				} catch (SocketTimeoutException timeout) {
					// Ignore, this is expected
				} catch (IOException e) {
					logger.log(Level.ERROR, "Error reading from socket", e);
					continueReading = false;
				}
			}
			logger.log(Level.ERROR, "Stopping read from socket thread with {0} pre-read bytes", processLater.size());
		};
		Thread.startVirtualThread(run);
	}

	//-----------------------------------------------------------------
	private void verifyAllOptionsReady(TelnetOption extension) {
		logger.log(Level.DEBUG, "verifyAllOptionsReady after response for {0}", extension.getName());
		// Dump all states 
		if (logger.isLoggable(Level.DEBUG)) {
			negotiationState
			.entrySet()
			.stream()
			.filter( entry -> entry.getValue().state==State.OPTION_SUBNEGOTIATION)
			.forEach( entry -> logger.log(Level.DEBUG, "Waiting for {0} ({1}) state: {2}", entry.getKey(), getExtensionName(entry.getKey()), entry.getValue()));
		}
		boolean allReady = negotiationState.values().stream().allMatch(s -> !s.state.isWaitState());
		if (allReady && continueReading) {
			logger.log(Level.WARNING, "All subnegotiations finished");
			continueReading = false;
			listener.forEach(cb -> cb.telnetReady());
		}
	}
	
	//-----------------------------------------------------------------
	public void waitUntilSubnegotiationDone(int timeoutMS) {
		logger.log(Level.INFO, "ENTER: waitUntilSubnegotiationDone()");
		
		startReadFromSocketThread();
		
		Instant start = Instant.now();
		Instant waitUntil = start.plusMillis(timeoutMS);
		try {
			while (negotiationState.values().stream().anyMatch(s -> s.state.isWaitState()) && Instant.now().isBefore(waitUntil)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Interrupted while waiting for subnegotiation to finish", e);
					return;
				}
			}
		} finally {
			continueReading = false;
			inputStream.addPreReadData(processLater);
			processLater.clear();
			// close all unanswered subnegotiations
			for (NegotiatonState state : negotiationState.values()) {
				if (state.state==State.SUGGESTED) {
					logger.log(Level.WARNING, "Subnegotiation for {0} did not finish - closing", state.extension.getName());
					state.setState(State.REJECTED);
					listener.forEach(callback -> callback.optionStateChanged(state.extension, false));			
				}
			}
			logger.log(Level.ERROR, "LEAVE: waitUntilSubnegotiationDone() for {0}ms - with {1} pre-read bytes", Duration.between(start, Instant.now()).toMillis(), processLater.size());
		}
	}

	//-----------------------------------------------------------------
	public TelnetOption getExtensionForOption(int optionCode) {
		for (TelnetOption<?> ext : extensions.keySet()) {
			if (ext.getOptionCode()==optionCode)
				return ext;
		}
		return null;
	}

	//-----------------------------------------------------------------
	String getExtensionName(int optionCode) {
		for (TelnetOption ext : extensions.keySet()) {
			if (ext.getOptionCode()==optionCode)
				return ext.getName();
		}
		return (WellKnownTelnetOptions.valueOf(optionCode)!=null)?WellKnownTelnetOptions.valueOf(optionCode).name():"UNKNOWN("+optionCode+")";
	}

	//-----------------------------------------------------------------
	public void fireTelnetCommand(TelnetCommand command) {
		logger.log(Level.DEBUG, "fire {0} to {1} listener", command, listener.size());
		for (TelnetListener list : listener)
			try {
				if (list==null) {
					logger.log(Level.ERROR, "Listener is null");
					continue;
				}
				list.telnetCommandReceived(command);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetCommandReceived: "+e,e);
			}	
	}

	//-----------------------------------------------------------------
	private void handleDoDontWillWontResponse(TelnetInputStream from, TelnetCommand command, NegotiatonState state) throws IOException {
		TelnetOption<?> extension = getExtensionForOption(command.getData());
		logger.log(Level.INFO, "Handle {0} response while in state {1} and we sent {2}", command, state, state.lastSent);
		switch (state.state) {
		case SUGGESTED:
			// We already sent a suggestion, so this is a response to that
			if (state.lastSent==ControlCode.WILL && command.getCode()==ControlCode.DO) {
				// We suggested WILL, and the other side said DO - confirmed
				state.setState(State.CONFIRMED);
				logger.log(Level.INFO, "Confirmed {0} with {1}", extension.getName(), command);
				if (extension.startCommunicationAs(role)) {
					logger.log(Level.INFO, "Start subnegotiation for {0}", extension.getName());
					if (extension.negotiateDetails(this))
						state.setState(State.OPTION_SUBNEGOTIATION);
				}
				listener.forEach(callback -> callback.optionStateChanged(extension, true));
			} else if (state.lastSent==ControlCode.DO && command.getCode()==ControlCode.WILL) {
				// We suggested DO, and the other side said WILL - confirmed
				state.setState(State.CONFIRMED);
				logger.log(Level.INFO, "Confirmed {0} with {1}", extension.getName(), command);
				if (extension.startCommunicationAs(role)) {
					logger.log(Level.INFO, "Start subnegotiation for {0}", extension.getName());
					if (extension.negotiateDetails(this))
						state.setState(State.OPTION_SUBNEGOTIATION);
				}
				listener.forEach(callback -> callback.optionStateChanged(extension, true));
			} else if (state.lastSent==ControlCode.WILL && command.getCode()==ControlCode.DONT) {
				// We suggested WILL, and the other side said DONT - rejected
				state.setState(State.REJECTED);
				logger.log(Level.INFO, "Rejected {0} with {1}", extension.getName(), command);
				listener.forEach(callback -> callback.optionStateChanged(extension, false));
			} else if (state.lastSent==ControlCode.DO && command.getCode()==ControlCode.WONT) {
				// We suggested DO, and the other side said WONT - rejected
				state.setState(State.REJECTED);
				logger.log(Level.INFO, "Rejected {0} with {1}", extension.getName(), command);
				listener.forEach(callback -> callback.optionStateChanged(extension, false));
			} else {
				logger.log(Level.WARNING, "Received {0} while state is {1} - ignoring", command, state);
				// Handle telnet clients that fail to respond correctly to DO/DONT/WILL/WONT commands. Some clients will send a DO command in response to a DO command, or a WILL command in response to a WILL command. In that case, we can ignore the command and continue with the negotiation.
				if (state.lastSent==ControlCode.DO && command.getCode()==ControlCode.DO) {
					// We suggested DO, and the other side said DO too - assume confirmed
					state.setState(State.CONFIRMED);
					if (extension.startCommunicationAs(role)) {
						logger.log(Level.INFO, "Start subnegotiation for {0}", extension.getName());
						if (extension.negotiateDetails(this))
							state.setState(State.OPTION_SUBNEGOTIATION);
					}
					listener.forEach(callback -> callback.optionStateChanged(extension, true));
				}
			}
			break;
		case CONFIRMED:
			if (command.getCode()==ControlCode.WONT || state.lastSent==ControlCode.DO) {
				// The remote site wants to stop doing this option
				confirm(from.getReverseStream(), command);
				listener.forEach(callback -> callback.optionStateChanged(extension, false));
			}
			break;
		case REJECTED:
			// E.g. when disabling an option
			if (command.getCode()==ControlCode.WILL && state.lastSent==ControlCode.DONT) {
				// The remote site wants to start doing this option again
				confirm(from.getReverseStream(), command);
				listener.forEach(callback -> callback.optionStateChanged(extension, true));
				return;
			}
			// If server sends WONT ECHO and we are already in REJECTED / DONT state:
		    if (command.getCode() == ControlCode.WONT ) {
		        // Suppress sending IAC DONT ECHO to prevent infinite Telnet option loop
		        return;
		    }
			if (command.getCode()==ControlCode.WONT || command.getCode()==ControlCode.DONT) {
				if (state.lastSent==ControlCode.DONT) return;
				// The remote site wants to stop doing this option
				confirm(from.getReverseStream(), command);
				listener.forEach(callback -> callback.optionStateChanged(extension, false));
			}
			break;
		case READY:
		default:
			logger.log(Level.WARNING, "Received {0} while state is {1} - ignoring", command, state);
		}
		
		if (extension!=null)
			verifyAllOptionsReady(extension);
	}

	//-----------------------------------------------------------------
	private void handleDoDontWillWont(TelnetInputStream from, TelnetCommand command) throws IOException {
		int optionCode = command.getData();
		var option = WellKnownTelnetOptions.valueOf(optionCode);
		NegotiatonState state = negotiationState.get(optionCode);
		logger.log(Level.DEBUG, "Received {0} while state is {1}", command, state);
		// Did we already negotiate this option? If so, don't respond to prevent loops
		if (state!=null) {
			handleDoDontWillWontResponse(from, command, state);
			return;
		}
		// No, this is a new option
		TelnetOption extension = getExtensionForOption(optionCode);
		if (extension!=null) {
			// This is an extension we support
			logger.log(Level.DEBUG, "{0} {1}", command, extension.getName());
			confirm(from.getReverseStream(), command);
		} else {
			logger.log(Level.WARNING, "No extension found for {0} - rejecting", option);
			reject(from.getReverseStream(), command);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetInputStream.TelnetInputStreamListener#processCommand(org.prelle.telnet.TelnetInputStream, org.prelle.telnet.TelnetCommand)
	 */
	public void processCommand(TelnetInputStream from, TelnetCommand command) throws IOException {
		logger.log(Level.DEBUG, "processCommand {0}", command);
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
		TelnetOption<?> extension = getExtensionForOption(code);
		if (oldState==null || oldState.state==State.SUGGESTED) {
			logger.log(Level.INFO, "CONFIRMED {0} with {1}", extension.getName(), confirmedWith);
			extension.negotiateDetails(this);
			// Inform the listener that the option is now confirmed
			listener.forEach(callback -> callback.optionStateChanged(extension, true));
		}
	}

	//-------------------------------------------------------------------
	private void handleNewlyRejected(NegotiatonState oldState, int code, ControlCode confirmedWith) {
		TelnetOption<?> extension = getExtensionForOption(code);
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

		TelnetOption<?> extension = getExtensionForOption(command.getData());
		NegotiatonState state = negotiationState.get(command.getData());
		logger.log(Level.INFO, "confirm "+command+" in state "+state);
		switch (command.getCode()) {
		case DO  : 
			ControlCode respondWith = ControlCode.WILL;
			out.sendWill(command.getData());
			if (state==null) {
				handleNewlyConfirmed(state, command.getData(), respondWith);
				state = new NegotiatonState(extension, State.CONFIRMED, respondWith);
				negotiationState.put(command.getData(), state);
			} else {
				handleNewlyConfirmed(state, command.getData(), respondWith);
				state.lastSent = ControlCode.WILL;
				state.setState(State.CONFIRMED);
			}
			break;
		case WILL: 
			respondWith = ControlCode.DO;
			out.sendDo(command.getData()); 
			handleNewlyConfirmed(state, command.getData(), respondWith);
			if (state==null) {
				state = new NegotiatonState(extension, State.CONFIRMED, respondWith);
				negotiationState.put(command.getData(), state);
			} else {
				state.lastSent = ControlCode.DO;
				state.setState(State.CONFIRMED);
			}
			break;
		case WONT: 
			respondWith = ControlCode.DONT;
			out.sendDont(command.getData()); 
			handleNewlyConfirmed(state, command.getData(), respondWith);
			if (state==null) {
				state = new NegotiatonState(extension, State.REJECTED, respondWith);
				negotiationState.put(command.getData(), state);
			} else {
				state.lastSent = respondWith;
				state.setState(State.REJECTED);
			}
			break;
		case DONT: 
			respondWith = ControlCode.WONT;
			if (state==null) {
				state = new NegotiatonState(extension, State.REJECTED, respondWith);
				negotiationState.put(command.getData(), state);
				out.sendWont(command.getData()); 
				handleNewlyConfirmed(state, command.getData(), respondWith);
			} else {
				if (state.lastSent==respondWith) return;
				out.sendWont(command.getData()); 
				handleNewlyConfirmed(state, command.getData(), respondWith);
				state.lastSent = respondWith;
				state.setState(State.REJECTED);
			}
			break;
		default:
			logger.log(Level.ERROR, "Don't know how to confirm a {0} command", command);
		}
	}

	//-------------------------------------------------------------------
	private void reject(TelnetOutputStream out, TelnetCommand command) throws IOException {
		if (out==null) {
			logger.log(Level.DEBUG, "Cannot reject {0} because output stream is null", command);
			return;
		}
		TelnetOption<?> extension = getExtensionForOption(command.getData());
		NegotiatonState state = negotiationState.get(command.getData());
		switch (command.getCode()) {
		case DO  : 
			out.sendWont(command.getData()); 
			if (state==null) {
				state = new NegotiatonState(extension, State.REJECTED, ControlCode.WONT);
				negotiationState.put(command.getData(), state);
			} else {
				state.lastSent = ControlCode.WONT;
				state.setState(State.REJECTED);
			}
			break;
		case WILL: 
			out.sendDont(command.getData());
			if (state==null) {
				state = new NegotiatonState(extension, State.REJECTED, ControlCode.DONT);
				negotiationState.put(command.getData(), state);
			} else {
				state.lastSent = ControlCode.DONT;
				state.setState(State.REJECTED);
			}
			break;
		default:
			logger.log(Level.ERROR, "Don''t know how to reject a {0} command", command);
		}
	}

	//-------------------------------------------------------------------
	public void processSubnegotiation(TelnetInputStream from, int code, int[] values) {
		logger.log(Level.INFO, "RCV Subnegotiation for {0}: {1}", code, Arrays.toString((values)));

		TelnetOption handler = getExtensionForOption(code);
		if (handler==null) {
			logger.log(Level.WARNING, "Received {2} bytes subnegotiation for {0}/{1}, but cannot find a TelnetOptionHandler", code, WellKnownTelnetOptions.valueOf(code), values.length);
			String strVal = new String(values, 0, values.length);
			logger.log(Level.INFO, strVal);
			return;
		}

//		// Evnetually update state
//		NegotiatonState state = negotiationState.get(code);
//		if (state!=null) {
//			if (state.state==State.OPTION_SUBNEGOTIATION) {
//				state.setState(State.READY);
//			}
//		}
		handler.handleSubnegotiation(values, this);
		verifyAllOptionsReady(handler);
	}

	//-------------------------------------------------------------------
	public void fireSubnegotiationFinished(TelnetOption<?> option) {
		logger.log(Level.DEBUG, "fireSubnegotiationFinished for {0}", option.getName());
		boolean allReadyBefore = negotiationState.values().stream().allMatch(s -> s.state==State.READY || s.state==State.CONFIRMED || s.state==State.REJECTED);
		negotiationState.get(option.getOptionCode()).setState(State.READY);
		
		// Check if all expected subnegotiations are finished. This is the case when all negotiation states are either CONFIRMED or READY
		boolean allReady = negotiationState.values().stream().allMatch(s -> s.state==State.READY || s.state==State.CONFIRMED || s.state==State.REJECTED);
		logger.log(Level.DEBUG, "allReady = {0}", allReady);
		if (allReady && !allReadyBefore) {
			logger.log(Level.INFO, "All subnegotiations finished");
			listener.forEach(cb -> cb.telnetReady());
		} else {
			// Dump all states to System.err
			negotiationState
				.entrySet()
				.stream()
				.filter( entry -> entry.getValue().state==State.OPTION_SUBNEGOTIATION)
				.forEach( entry -> logger.log(Level.INFO, "Waiting for {0} ({1}) state: {2}", entry.getKey(), getExtensionName(entry.getKey()), entry.getValue()));
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @return the inputStream
	 */
	public TelnetInputStream getInputStream() {
		return inputStream;
	}

	//-------------------------------------------------------------------
	/**
	 * @param inputStream the inputStream to set
	 */
	public void setInputStream(TelnetInputStream inputStream) {
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

	//-------------------------------------------------------------------
	public boolean isFeatureActive(Integer code) {
		return negotiationState.containsKey(code) 
				&& 
				(
				negotiationState.get(code).state==State.CONFIRMED 
				|| 
				negotiationState.get(code).state==State.OPTION_SUBNEGOTIATION 
				||
				negotiationState.get(code).state==State.READY	
				);
	}
	
}
