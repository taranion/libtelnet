package org.prelle.telnet.protocol;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.prelle.telnet.WellKnownTelnetOptions;
import org.prelle.telnet.event.DataEvent;
import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.event.TelnetEventFactory;
import org.prelle.telnet.event.TelnetNegotiationEvent;
import org.prelle.telnet.event.TelnetParserListener;
import org.prelle.telnet.event.internal.DataEventImpl;
import org.prelle.telnet.event.internal.DefaultTelnetEventFactory;
import org.prelle.telnet.event.internal.TelnetCommandImpl;
import org.prelle.telnet.event.internal.TelnetNegotiationEventImpl;
import org.prelle.telnet.event.internal.TelnetSubnegotiationEventImpl;
import org.prelle.telnet.option.CommunicationRole;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.parser.TelnetConstants;
import org.prelle.telnet.parser.TelnetDecoder;

/**
 * 
 */
public class TelnetProtocol implements TelnetParserListener, TelnetConstants {

	private final static Logger logger = System.getLogger("telnet.lvl3");

	public static enum OptionState {
		UNKNOWN,
		UNKNOWN_QUERIED,
		INACTIVE_NOT_SUPPORTED,
		INACTIVE,
		/** Asked remote site to activate this option, waiting for response */
		INACTIVE_QUERIED,
		ACTIVE,
		/** Asked remote site to deactivate this option, waiting for response */
		ACTIVE_QUERIED,
		;
		public boolean isWaitState() {
			return this==UNKNOWN_QUERIED || this==UNKNOWN || this==INACTIVE_QUERIED;
		}
	}
	
	private static enum SubNegState {
		IDLE,
		RESPONSE_PENDING,
		FINISHED
		;
		public boolean isWaitState() {
			return this==RESPONSE_PENDING;
		}
	}
	
	private static record ProcessResult(Optional<ControlCode> answerWith, Optional<Boolean> stateChange) {
		public ProcessResult(Optional<ControlCode> answerWith) {
			this(answerWith, Optional.empty());
		}
		public ProcessResult(Optional<ControlCode> answerWith, Boolean newState) {
			this(answerWith, Optional.of(newState));
		}
	}
	
	
	public static class NegotiationState {
		private OptionState state = OptionState.UNKNOWN;
		private SubNegState subnegState = SubNegState.IDLE;
		private TelnetOption extension;
		
		//-------------------------------------------------------------------
		/**
		 * Constructor to use for unknown options
		 */
		public NegotiationState() {
		}
		
		//-------------------------------------------------------------------
		/**
		 * Constructor to use for known options
		 * @param extension
		 * @param listener
		 */
		public NegotiationState(TelnetOption extension) {
			this.extension = extension;
		}
		
		//-------------------------------------------------------------------
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return state+"/"+subnegState;
		}

		public void setState(OptionState value) {
			if (this.state!=value) {
				logger.log(Level.INFO, "Changing state for {0} from {1} to {2}", (extension!=null)?extension.getName():"UNKNOWN", this.state, value);
			}
			state = value;
		}
		
		//-------------------------------------------------------------------
		@SuppressWarnings("incomplete-switch")
		private ProcessResult generateAnswer(ControlCode remoteSends) {
			switch (state) {
			case ACTIVE:
				// Option is currently active
				switch (remoteSends) {
				case DO:
				case WILL:
					// We are already active, so we ignore the WILL/DO command to prevent loops
					return new ProcessResult(Optional.empty());
				case DONT:
					// The remote side wants to stop doing this option, so we confirm with WONT
					setState(OptionState.INACTIVE);
					return new ProcessResult(Optional.of(ControlCode.WONT), false);
				case WONT:
					// The remote side wants to stop doing this option, so we confirm with DONT
					setState(OptionState.INACTIVE);
					return new ProcessResult(Optional.of(ControlCode.DONT), false);
				default:
					return new ProcessResult(Optional.empty());
				}
			case UNKNOWN_QUERIED:
				// This is the first response we ever got for this option
				boolean active = (remoteSends==ControlCode.DO || remoteSends==ControlCode.WILL);
				if (active) {
					setState(OptionState.ACTIVE);
				} else {
					setState(OptionState.INACTIVE_NOT_SUPPORTED);
				}
				return new ProcessResult(Optional.empty(), active);
			case INACTIVE_QUERIED:
				// We asked the remote side to activate this option - this is the response				
				active = (remoteSends==ControlCode.DO || remoteSends==ControlCode.WILL);
				if (active) {
					setState(OptionState.ACTIVE);
					return new ProcessResult(Optional.empty(), active);
				} else {
					// Remain inactive
					setState(OptionState.INACTIVE);
					return new ProcessResult(Optional.empty());
				}
			case UNKNOWN:
				// This is the very first request for an option.
				if (extension==null) {
					// We don't support this option, so we respond with WONT/DONT			
					setState(OptionState.INACTIVE_NOT_SUPPORTED);
					return new ProcessResult(Optional.of( (remoteSends==ControlCode.DO)?ControlCode.WONT:ControlCode.DONT));
				} else {
					// We support this option, so we respond with WILL/DO
					setState(OptionState.ACTIVE);
					return new ProcessResult(Optional.of((remoteSends==ControlCode.DO)?ControlCode.WILL:ControlCode.DO), true);
				}
			case INACTIVE:
				// This is a follow-up request for a supported option
				switch (remoteSends) {
				case  DO:
					setState(OptionState.ACTIVE);
					return new ProcessResult( Optional.of( ControlCode.WILL), true);
				case WILL:
					setState(OptionState.ACTIVE);
					return new ProcessResult( Optional.of( ControlCode.DO), true);
				case WONT:
				case DONT:
					// Trying to deactivate an option that is already inactive - ignore to prevent loops
					return new ProcessResult(Optional.empty());					
				}
			case INACTIVE_NOT_SUPPORTED:
				// We already told the remote side that we do not support this option.
				// Don't send any more responses to prevent loops
				return new ProcessResult(Optional.empty());
			case ACTIVE_QUERIED:
				// We asked the remote side to deactivate this option - this is the response				
				active = (remoteSends==ControlCode.DO || remoteSends==ControlCode.WILL);
				if (active) {
					// Remain active
					setState(OptionState.ACTIVE);
					return new ProcessResult(Optional.empty());
				} else {
					// Remote party agreed to deactivate this option
					setState(OptionState.INACTIVE);
					return new ProcessResult(Optional.empty(), false);
				}
			default:
				break;					
			}
			return new ProcessResult(Optional.empty());
		}

		//-------------------------------------------------------------------
		public boolean isActive() {
			return state==OptionState.ACTIVE;
		}
	}
	
	private CommunicationRole role;
	private TelnetEventFactory factory;
	private TelnetDecoder parser;
	private TelnetReturnChannel returnChannel;
	private TelnetProtocolListener listener;
	
	private Map<TelnetOption, CommunicationRole> extensions = new HashMap<>();
	private Map<Integer, TelnetOption> extensionsByCode = new HashMap<>();

	private Map<Integer, NegotiationState> negotiationState;
    
    private boolean initialHandshakeDone = false;
    private Consumer<DataEvent> dataConsumer; 
	
	//-------------------------------------------------------------------
    public static TelnetProtocolBuilder builder(CommunicationRole role) {
		return new TelnetProtocolBuilder(role);
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocol(CommunicationRole role, TelnetProtocolListener listener) {
		this(role, new DefaultTelnetEventFactory(), listener, null, new ArrayList<>(), null);
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocol(CommunicationRole role, TelnetEventFactory factory, TelnetProtocolListener listener, Consumer<DataEvent> dataListener, List<TelnetOption> options, TelnetReturnChannel returnChannel) {
		parser    = new TelnetDecoder(this);
		negotiationState = new HashMap<>();
		this.role = role;
		this.factory  = factory;
		this.listener = listener;
		this.dataConsumer = dataListener;
		options.forEach( opt -> add(opt));
		this.returnChannel = returnChannel;
	}
	
	//-------------------------------------------------------------------
	public TelnetEventFactory factory() {
		return factory;
	}
	
	//-------------------------------------------------------------------
	TelnetProtocolListener getListener() {
		return listener;
	}
	
	//-------------------------------------------------------------------
	public CommunicationRole getRole() {
		return role;
	}
	
	//-------------------------------------------------------------------
	public void setReturnChannel(TelnetReturnChannel sink) {
		this.returnChannel = sink;
	}
	
	//-------------------------------------------------------------------
	public void setDataListener(Consumer<DataEvent> consumer) {
		this.dataConsumer = consumer;
	}
	
	public boolean isSendGoAheadAsANSISepator() {
		return parser.isSendGoAheadAsANSISepator();
	}

	public void setSendGoAheadAsANSISepator(boolean sendGoAheadAsANSISepator) {
		parser.setSendGoAheadAsANSISepator(sendGoAheadAsANSISepator);
	}

	//-------------------------------------------------------------------
	public void process(byte[] data) {
		if (data == null || data.length == 0) return;
		// Pass data to parser which will return events via TelnetParserListener interface
		parser.process(data);
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.event.TelnetParserListener#onTelnetEvent(org.prelle.telnet.event.TelnetEvent)
	 */
	@Override
	public void onTelnetEvent(TelnetEvent event) {
		logger.log(Level.INFO, "onTelnetEvent({0})", event);
		switch (event) {
		case DataEventImpl data -> {
			if (dataConsumer!=null)
				dataConsumer.accept((DataEventImpl)event);
			else
				listener.onTelnetEvent(event);
		}
		case TelnetCommandImpl co -> listener.onTelnetEvent(event);
		case TelnetNegotiationEventImpl option -> handleDoDontWillWont(option);
		case TelnetSubnegotiationEventImpl subneg -> {
			TelnetOption option = extensionsByCode.get(subneg.getOption());
			if (option!=null) {
				for (TelnetOptionEvent ev : option.handleSubnegotiation(subneg, this)) {
					if  (ev.getOption()==null) ev.setOption(option);
					if (ev instanceof SubnegotiationFinishedEvent) {
						// Mark the option as ready
						negotiationState.get(subneg.getOption()).subnegState = SubNegState.FINISHED;
						verifyAllOptionsReady();
					} else
						listener.onTelnetEvent(ev);
				}
			} else 
				logger.log(Level.WARNING, "Received subnegotiation for unknown option {0}", subneg.getOption());
		}
		default -> logger.log(Level.WARNING, "Unknown TelnetEvent type: {0}", event);
		}
	}

//	//-------------------------------------------------------------------
//	public void sendResponse(byte[] toSend) {
//		if (outputStream!=null) {
//			try {
//				outputStream.sendToRemote(toSend);
//			} catch (IOException e) {
//				logger.log(Level.ERROR, "Error sending response", e);
//			}
//		} else {
//			logger.log(Level.WARNING, "No output stream set - cannot send response ");
//		}
//	}

	//-------------------------------------------------------------------
	public void sendResponse(TelnetEvent response) {
		if (returnChannel!=null) {
			try {
				returnChannel.sendToRemote(response);
			} catch (IOException e) {
				logger.log(Level.ERROR, "Error sending response "+response, e);
			}
		} else {
			logger.log(Level.WARNING, "No output stream set - cannot send response {0}", response);
		}
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocol add(TelnetOption extension) {
		Objects.requireNonNull(extension, "Cannot add null extension");
		if (!extensions.containsKey(extension)) {
			extensions.put(extension, role);
		}
		extensionsByCode.put(extension.getOptionCode(), extension);
		negotiationState.put(extension.getOptionCode(), new NegotiationState(extension));
		return this;
	}
	
	//-------------------------------------------------------------------
	public void initializeExtensions() {
		logger.log(Level.INFO, "ENTER: initializeExtensions() with {0} extensions", extensions.size());
		try {
			initialHandshakeDone = false;
			for (TelnetOption ext : extensions.keySet()) {
				try {
					if (ext.startNegotiationAs(role)) {
						ext.initiate(this, role);
						negotiationState.get(ext.getOptionCode()).setState(OptionState.INACTIVE_QUERIED);
					} else
						negotiationState.get(ext.getOptionCode()).setState(OptionState.INACTIVE);
				} catch (Exception e) {
					logger.log(Level.ERROR, "Error initializing extension "+ext.getName(), e);
				}
			}
		} finally {
			logger.log(Level.INFO, "LEAVE: initializeExtensions");
		}
	}


	//-----------------------------------------------------------------
	private void verifyAllOptionsReady() {
		if (initialHandshakeDone) return;
		logger.log(Level.DEBUG, "verifyAllOptionsReady");
		// Dump all states 
		if (logger.isLoggable(Level.DEBUG)) {
			negotiationState.values().stream()
				.filter( ext -> ext.extension!=null)
				.forEach( entry -> logger.log(Level.DEBUG, "--- {0} \t= {1}", entry.extension.getName(), entry));
		}
		// Dump wait states 
		if (logger.isLoggable(Level.DEBUG)) {
			negotiationState.values().stream()
				.filter( ext -> ext.state.isWaitState())
				.forEach( entry -> logger.log(Level.DEBUG, "***Waiting for {0} ({1})", entry.extension.getOptionCode(), entry.extension.getName()));
		}
		boolean allReady = negotiationState.values().stream()
				.allMatch( state -> !state.state.isWaitState());
		boolean allSubReady = negotiationState.values().stream()
				.allMatch( state -> !state.subnegState.isWaitState());
		logger.log(Level.DEBUG, "All subnegotiations finished? {0} + {1}", allReady, allSubReady);
		if (allReady & allSubReady && !initialHandshakeDone) {
			logger.log(Level.INFO, "All subnegotiations finished");
			initialHandshakeDone = true;
			listener.telnetReady();
		}
	}
	
//	//-----------------------------------------------------------------
//	public void waitUntilSubnegotiationDone(int timeoutMS) {
//		logger.log(Level.INFO, "ENTER: waitUntilSubnegotiationDone()");
//		
//		startReadFromSocketThread();
//		
//		Instant start = Instant.now();
//		Instant waitUntil = start.plusMillis(timeoutMS);
//		try {
//			while (negotiationState.values().stream().anyMatch(s -> s.state.isWaitState()) && Instant.now().isBefore(waitUntil)) {
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					logger.log(Level.WARNING, "Interrupted while waiting for subnegotiation to finish", e);
//					return;
//				}
//			}
//		} finally {
//			continueReading = false;
//			inputStream.receiveData(processLater);
//			processLater.clear();
//			// close all unanswered subnegotiations
//			for (NegotiatonState state : negotiationState.values()) {
//				if (state.state==State.SUGGESTED) {
//					logger.log(Level.WARNING, "Subnegotiation for {0} did not finish - closing", state.extension.getName());
//					state.setState(State.REJECTED);
//					listener.optionStateChanged(state.extension, false);			
//				}
//			}
//			logger.log(Level.ERROR, "LEAVE: waitUntilSubnegotiationDone() for {0}ms - with {1} pre-read bytes", Duration.between(start, Instant.now()).toMillis(), processLater.size());
//		}
//	}

	//-----------------------------------------------------------------
	public Collection<TelnetOption> getExtensions() {
		return extensions.keySet();
	}

	//-----------------------------------------------------------------
	public TelnetOption getExtensionForOption(int optionCode) {
		for (TelnetOption ext : extensions.keySet()) {
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
	public NegotiationState getNegotiationState(int optionCode) {
		return negotiationState.get(optionCode);
	}

	//-----------------------------------------------------------------
	private void handleDoDontWillWont(TelnetNegotiationEvent command) {
		int optionCode = command.getOption();
		NegotiationState state = negotiationState.get(optionCode);
		if (state==null) {
			// Unknown option - create a state for unsupported options 
			state = new NegotiationState();
			negotiationState.put(optionCode, state);
		}
		logger.log(Level.WARNING, "---------------------------Received {0} while state is {1}", command, state);
		TelnetOption extension = getExtensionForOption(optionCode);
		
		ProcessResult result = state.generateAnswer(command.getType());
		// Do we need to send an answer
		result.answerWith.ifPresent( answer -> {
			logger.log(Level.INFO, "Responding to {0} with {1}", command, answer);
			sendResponse(factory.createTelnetNegotiationEvent(command, answer));
		});
		
		// If it isn't active, assume subnegotiation is finished
		if (state.state==OptionState.ACTIVE) {			
			extension.setSubnegotiationFinished(true);
		}
		
		// Did the state change somehow?
		result.stateChange().ifPresent(newState -> {
			// If this was a known option, we can update the state accordingly
			logger.log(Level.INFO, "Changing state to {0} for option {1}", newState, (extension!=null)?extension.getName():"UNKNOWN("+optionCode+")");
			if (extension!=null) {
				if (newState) {
					optionBecameActive(extension);
				} else {
					optionBecameInactive(extension);
				}
			}
		});

		// Check if all options are ready now
		verifyAllOptionsReady();
		
	}

	//-------------------------------------------------------------------
	private void optionBecameActive(TelnetOption extension) {
		logger.log(Level.INFO, "Option {0} became active", extension.getName());
		NegotiationState state = negotiationState.get(extension.getOptionCode());
		listener.optionStateChanged(extension, true);
		listener.onTelnetEvent( factory.createOptionStateEvent(extension, true) );
		
		boolean mustSubnegotiate = extension.startSubNegotiationAs(role);
		if (mustSubnegotiate) {
			logger.log(Level.INFO, "It is our turn to start a subnegotiation for {0}", extension.getName());
			state.subnegState = SubNegState.RESPONSE_PENDING;
			extension.negotiateDetails(this, role);
		} else {
			state.subnegState = SubNegState.FINISHED;
		}
		
		negotiationState.forEach( (code,s) -> {
			if (s.state.isWaitState()) {
				logger.log(Level.INFO, "Option {0} is still waiting for a response", getExtensionName(code));
			}
			if (s.subnegState.isWaitState()) {
				logger.log(Level.INFO, "Option {0} is still waiting for subnegotiation to finish", getExtensionName(code));
			}
		});
	}

	//-------------------------------------------------------------------
	private void optionBecameInactive(TelnetOption extension) {
		logger.log(Level.INFO, "Option {0} became inactive", extension.getName());
		listener.optionStateChanged(extension, false);
		listener.onTelnetEvent( factory.createOptionStateEvent(extension, false) );
	}


	//-------------------------------------------------------------------
	public boolean isFeatureActive(Integer code) {
		return negotiationState.containsKey(code) 
				&& negotiationState.get(code).isActive();
	}

	//-------------------------------------------------------------------
	public void setEventFactory(TelnetEventFactory value) {
		factory = value;
	}
	
}
