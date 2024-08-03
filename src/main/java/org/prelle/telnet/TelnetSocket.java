/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author prelle
 *
 */
public class TelnetSocket extends Socket {

	private enum ModeState {
		UNKNOWN,
		OFFERED,
		REQUESTED,
		CONFIRMED,
		CONFIRMED_EXCHANGING,
		/** Subnegotiation finished */
		CONFIRMED_EXCHANGED,
		REJECTED
	}

	static class OptionEntry {
		TelnetOptionHandler handler;
		Role role;
		public OptionEntry(TelnetOptionHandler handler, Role role) {
			this.handler = handler;
			this.role    = role;
		}
		public String toString() { return handler.getName()+" as "+role; }
	}


	private final static Logger logger = System.getLogger("telnet.lvl3");

	private TelnetInputStream in;
	private TelnetOutputStream out;

	private Map<Integer, ModeState> modeStates = new HashMap<>();

	private Map<Integer,OptionEntry> options = new HashMap<>();

	private List<TelnetOptionListener> optionListener = new ArrayList<>();
	private Charset charset = StandardCharsets.ISO_8859_1;

	private TimerTask waitForOptions;
	private static Timer timer = new Timer("SocketOptionTimer");
	private static Instant exchangeStart;

	public static TelnetSocketBuilder builder() {
		return new TelnetSocketBuilder();
	}

	//-----------------------------------------------------------------
	/**
	 * @param passiveSupport
	 */
	public TelnetSocket(List<OptionEntry> options) {
		options.forEach(opt -> {this.options.put(opt.handler.code, opt); modeStates.put(opt.handler.code, ModeState.UNKNOWN);});
//		requesting.forEach(opt -> {requestOpt.put(opt.code, opt); modeStates.put(opt.code, new ModeState());});
//		willing   .forEach(opt -> {provideOpt.put(opt.code, opt); modeStates.put(opt.code, new ModeState());});
//		role = Role.SERVER;
	}

	//-----------------------------------------------------------------
	public TelnetSocket(TelnetSocketBuilder builder) throws UnknownHostException, IOException {
		super(builder.host, builder.port);
		this.optionListener = builder.optionListener;
		getInputStream();
		getOutputStream();
		builder.options.forEach( (i,opt) -> options.put(i, opt));
		initialize();
	}

	//-----------------------------------------------------------------
	/**
	 * @param host
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TelnetSocket(String host, int port) throws UnknownHostException, IOException {
		super(host, port);
		initialize();
	}

	//-----------------------------------------------------------------
	/**
	 * @see java.net.Socket#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (in==null)
			in = new TelnetInputStream( this,super.getInputStream());
		return in;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.net.Socket#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (out==null)
			out = new TelnetOutputStream(super.getOutputStream());
		return out;
	}

	//-----------------------------------------------------------------
	private TelnetOutputStream out() throws IOException {
		return (TelnetOutputStream) getOutputStream();
	}

	//-----------------------------------------------------------------
	private TelnetInputStream in() throws IOException {
		return (TelnetInputStream) getInputStream();
	}

	//-------------------------------------------------------------------
	private ModeState getModeState(int option) {
		ModeState state = modeStates.get(option);
		if (state==null) {
			state=ModeState.UNKNOWN;
			modeStates.put(option, state);
		}
		return state;
	}

	//-----------------------------------------------------------------
	private void changeModeState(int option, ModeState newState) {
		ModeState oldState = modeStates.get(option);
		modeStates.put(option, newState);

		if (oldState==newState)
			return;

		if (options.get(option)==null) {
			logger.log(Level.WARNING, "No config for option "+option);
		} else
			logger.log(Level.DEBUG, "Change state of {0} to {1}", options.get(option).handler.name, newState);
		// Check if this new mode finished something
		boolean finishedSomething = (newState==ModeState.REJECTED || newState==ModeState.CONFIRMED || newState==ModeState.CONFIRMED_EXCHANGED);
		if (!finishedSomething)
			return;

		// Check if all modes are handled
		boolean missing = false;
		for (Entry<Integer, ModeState> pair : modeStates.entrySet()) {
			if (pair.getValue()!=ModeState.CONFIRMED && pair.getValue()!=ModeState.REJECTED && pair.getValue()!=ModeState.CONFIRMED_EXCHANGED) {
				missing = true;
			}
		}
		if (!missing) {
			// All options have been answered
			logger.log(Level.DEBUG, "All option handling has been finished");
			try {waitForOptions.cancel();} catch (Exception e) {}
			fireOptionPhaseDone();
		}
	}

	//-------------------------------------------------------------------
	/**
	 * This method is called by a timer to make sure we are not indefinitely
	 * waiting for answers to option offers.
	 */
	private void stopWaitingForOptionAnswers() {
		for (Entry<Integer, ModeState> pair : modeStates.entrySet()) {
//			logger.log(Level.INFO, "Current state of {0} is {1}", pair.getKey(), pair.getValue());
			if (pair.getValue()!=ModeState.CONFIRMED && pair.getValue()!=ModeState.REJECTED && pair.getValue()!=ModeState.CONFIRMED_EXCHANGED) {
				logger.log(Level.WARNING, "No answer to option {0} - assume it REJECTED", pair.getKey());
				changeModeState(pair.getKey(), ModeState.REJECTED);
			}
		}
	}

	//-----------------------------------------------------------------
	public TelnetSocket support(TelnetOptionHandler handler, Role role) {
		logger.log(Level.DEBUG, "offer option {0} ({1})", handler.name, handler.code);
		options.put(handler.code, new OptionEntry(handler, role));
		try {
			out().sendWill(handler.getCode());
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed sending WILL "+handler.name, e);
		}

		return this;
	}

	//-----------------------------------------------------------------
	void initialize() throws IOException {
		logger.log(Level.DEBUG, "initialize");
		List<OptionEntry> tmp = new ArrayList<>(options.values());
		Collections.sort(tmp, new Comparator<OptionEntry>() {
			@Override
			public int compare(OptionEntry o1, OptionEntry o2) {
				// TODO Auto-generated method stub
				return Integer.compare(o1.handler.code, o2.handler.code);
			}
		});

		exchangeStart = Instant.now();
		for (OptionEntry support : tmp) {
			if (support.role==Role.PROVIDER) {
				logger.log(Level.DEBUG," indicate support for {0}", support.handler);
				changeModeState(support.handler.code, ModeState.OFFERED);
				out().sendWill(support.handler.getCode());
			} else if (support.role==Role.PROVIDER_SILENT){
				logger.log(Level.DEBUG," do nothing to indicate support for {0}", support.handler);
			} else if (support.role==Role.REJECT_OUTRIGHT){
				logger.log(Level.DEBUG,"  Preemptively reject {0}", support.handler);
				out().sendWont(support.handler.getCode());
			} else {
				logger.log(Level.DEBUG,"  Request {0}", support.handler);
				changeModeState(support.handler.code, ModeState.REQUESTED);
				out().sendDo(support.handler.getCode());
			}
		}

		// Timer to stop deadlock should one or more Telnet option not
		// being answered (BeipMu and the ECHO option)
		waitForOptions = new TimerTask() {
			public void run() {
				System.err.println("END: "+Instant.now());
				stopWaitingForOptionAnswers();
			}};
		timer.schedule(waitForOptions, 500);
	}

//	//-----------------------------------------------------------------
//	public TelnetOption getFromSupportedOptions(int optionCode) {
//		for (TelnetOption opt : supportedOptions) {
//			if (opt.getCode()==optionCode)
//				return opt;
//		}
//		return null;
//	}
//
//	//-----------------------------------------------------------------
//	/* (non-Javadoc)
//	 * @see org.prelle.telnet.TelnetStreamListener#receivedWILL(int)
//	 */
//	@Override
//	public void receivedWILL(int optionCode) {
//		TelnetOption known = TelnetOption.valueOf(optionCode);
//		TelnetOption option = getFromSupportedOptions(optionCode);
//		try {
//			if (option==null && known==null) {
//				logger.log(Level.WARNING,"remote party offers unknown option {0} (WILL->DONT)", optionCode);
//				out().sendDont(optionCode);
//				return;
//			}
//			if (option==null && known!=null) {
//				logger.log(Level.WARNING,"remote party offers unsupported option {0} ({1}) (WILL->DONT)", known.name(), optionCode);
//				out().sendDont(optionCode);
//				return;
//			}
//
//			// Did we request this?
//			boolean wasRequested = answerExpected.contains(option);
//			TelnetOptionHandler handler = option.getOptionHandler();
//			if (wasRequested) {
//				logger.log(Level.WARNING,"remote party agrees to do {0} ({1})  (DO->WILL)", option.name(), optionCode);
//				answerExpected.remove(option);
//				handler.optionEnabled(this, true);
//				activeFeatures.add(option);
//			} else {
//				// We did not request this
//				logger.log(Level.WARNING,"remote party offers {0} ({1}), but we did not request this  (WILL)", handler.getName(), optionCode);
//				out().sendDo(optionCode);
//				// DOes it make sense to inform handler about this?
//				//handler.remotePartyOffered(this);
//			}
//		} catch (IOException e) {
//			logger.log(Level.ERROR,"Could not answer that WILL offer: "+e,e);
//		}
//	}

	//-----------------------------------------------------------------
	public void addOptionListener(TelnetOptionListener optList) {
		if (!optionListener.contains(optList))
			optionListener.add(optList);
	}

	//-----------------------------------------------------------------
	/**
	 * All WILL and DOs have been exchanged
	 */
	public void fireOptionPhaseDone() {
		logger.log(Level.INFO, "All telnet options are known");
		// Measure how long Telnet option exchange took
		Duration dur = Duration.between(exchangeStart, Instant.now());
		logger.log(Level.WARNING, "Option exchange required {0} milliseconds", dur.toMillis());

		for (TelnetOptionListener list : optionListener)
			try {
//				System.err.println("STOP: "+Instant.now()+"  in TelnetSocket.fireOptionPhaseDone");
				list.telnetSupportedOptionsKnown(this);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetSupportedOptionsKnown: "+e,e);
			}
	}

	//-----------------------------------------------------------------
	public void fireOptionDataChanged(TelnetOptionHandler option,Object data) {
		logger.log(Level.DEBUG, "fireOptionDataChange({0})", data.getClass().getSimpleName());
		for (TelnetOptionListener list : optionListener) {
			try {
				list.telnetOptionDataChanged(this, option, data);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetOptionDataChanged: "+e,e);
			}
		}
		changeModeState(option.code, ModeState.CONFIRMED_EXCHANGED);
	}

	//-----------------------------------------------------------------
	public void fireFeatureActive(TelnetOptionHandler option, boolean state) {
		logger.log(Level.DEBUG, "fireFeatureActive({0},{1})", option.code,state);
		for (TelnetOptionListener list : optionListener)
			try {
				list.telnetOptionStatusChange(this, option, state);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetOptionStatusChange: "+e,e);
			}
	}

	//-----------------------------------------------------------------
	public void fireTelnetCommand(TelnetCommand command) {
		for (TelnetOptionListener list : optionListener)
			try {
				list.telnetCommandReceived(this, command);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetCommandReceived: "+e,e);
			}
	}

	//-------------------------------------------------------------------
	public boolean isFeatureActive(int code) {
		OptionEntry support = options.get(code);
		if (support==null) return false;
		ModeState state = getModeState(code);
		return state==ModeState.CONFIRMED || state==ModeState.CONFIRMED_EXCHANGED;
	}

	//-------------------------------------------------------------------
	public static class TelnetSocketBuilder {

		private String host;
		private int port;
		private Map<Integer,OptionEntry> options = new HashMap<>();
		private List<TelnetOptionListener> optionListener = new ArrayList<>();

		//-----------------------------------------------------------------
	    private TelnetSocketBuilder() {
	    }
		//-----------------------------------------------------------------
	    public TelnetSocketBuilder connectTo(String host, int port) {
	    	this.host = host;
	    	this.port = port;
	    	return this;
	    }
		//-----------------------------------------------------------------
		public TelnetSocketBuilder support(TelnetOptionHandler handler, Role role) {
			options.put(handler.code, new OptionEntry(handler, role));
			return this;
		}

		//-----------------------------------------------------------------
		public TelnetSocketBuilder addOptionListener(TelnetOptionListener optList) {
			if (!optionListener.contains(optList))
				optionListener.add(optList);
			return this;
		}

//		//-----------------------------------------------------------------
//		public TelnetSocketBuilder withMTP() {
//			activelyRequest(TelnetOptionDeleteMe.MTP);
//			return this;
//		}

		//-----------------------------------------------------------------
		public TelnetSocket build() throws UnknownHostException, IOException {
			return new TelnetSocket(this);
		}
	}

	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetStreamListener#processCommand(org.prelle.telnet.TelnetCommand)
//	 */
//	@Override
	void processCommand(TelnetCommand command) throws IOException {
		logger.log(Level.DEBUG, "processCommand {0}",command);

		switch (command.getCode()) {
		case DO:
			int option = command.getData();
			TelnetOptionHandler optCls = (options.containsKey(option))?options.get(option).handler:null;
			String name = (optCls!=null)?optCls.getName():"UNKNOWN";
			OptionEntry supported = options.get(option);
			if (supported!=null && supported.role!=Role.REJECT_OUTRIGHT) {
				// Generally we do support to send this option
				ModeState state = getModeState(option);
				if (state==ModeState.CONFIRMED || state==ModeState.CONFIRMED_EXCHANGING || state==ModeState.CONFIRMED_EXCHANGED) {
					/*
					 * From RFC 854
					 * b. If a party receives what appears to be a request to enter some
  					 * mode it is already in, the request should not be acknowledged.
  					 * This non-response is essential to prevent endless loops in the
  					 * negotiation.
					 */
					logger.log(Level.DEBUG, "Remote party asks us to do {0} ({1}), but we already confirmed that", name, option);
				} else {
					logger.log(Level.INFO, "Remote party asks us to do {0} ({1}) and will do that", name, option);
					changeModeState(option, ModeState.CONFIRMED);
					if (option==0)
						out().setBinaryMode(true);
					out().sendWill(command.getData());
					if (supported.role==Role.PROVIDER) {
						boolean needsSubNeg = optCls.initializeAs(supported.role, this, out());
						if (needsSubNeg)
							changeModeState(option, ModeState.CONFIRMED_EXCHANGING);
						fireFeatureActive(optCls,true);
					}
				}
			} else {
				logger.log(Level.INFO, "Remote party asks us to do {0} ({1}) but we don't support that", name, option);
				out().sendWont(command.getData());
			}
			break;
		case WILL:
			option = command.getData();
			optCls = (options.containsKey(option))?options.get(option).handler:null;
			name = (optCls!=null)?optCls.getName():"UNKNOWN";
			supported = options.get(option);
			if (supported!=null && supported.role!=Role.REJECT_OUTRIGHT) {
				// Generally we do support to receive this option
				ModeState state = getModeState(option);
				if (state==ModeState.CONFIRMED || state==ModeState.CONFIRMED_EXCHANGING || state==ModeState.CONFIRMED_EXCHANGED) {
					/*
					 * From RFC 854
					 * b. If a party receives what appears to be a request to enter some
  					 * mode it is already in, the request should not be acknowledged.
  					 * This non-response is essential to prevent endless loops in the
  					 * negotiation.
					 */
				} else {
					changeModeState(option, ModeState.CONFIRMED);
					if (option==0)
						in().setBinaryMode(true);
					logger.log(Level.INFO, "Remote party offers to do {0} ({1}) and we will let it do that", name, option);
					out().sendDo(command.getData());
//					if (supported.role==Role.REQUESTER) {
						boolean needsSubNeg = optCls.initializeAs(supported.role, this, out());
						if (needsSubNeg)
							changeModeState(option, ModeState.CONFIRMED_EXCHANGING);
						fireFeatureActive(optCls,true);
//					}
				}
			} else {
				logger.log(Level.INFO, "Remote party offers to do {0} ({1}) but we won't let it do that", name, option);
				out().sendDont(command.getData());
			}
			break;
		case WONT:
			option = command.getData();
			optCls = (options.containsKey(option))?options.get(option).handler:new TelnetOptionHandler(option, "UNKNOWN");
			name = optCls.getName();
			changeModeState(option, ModeState.REJECTED);
			logger.log(Level.INFO, "Remote party does not support {0} ({1})", name, option);
			fireFeatureActive(optCls,false);
			break;
		case DONT:
			option = command.getData();
			optCls = (options.containsKey(option))?options.get(option).handler:new TelnetOptionHandler(option, "UNKNOWN");
			name = (optCls!=null)?optCls.getName():"UNKNOWN";
			changeModeState(option, ModeState.REJECTED);
			logger.log(Level.INFO, "Remote party does not want us to do {0} ({1})", name, option);
			fireFeatureActive(optCls,false);
			break;
		case IP:
			// Interrupt process
			logger.log(Level.WARNING, "Connection interrupted by remote party");
			in().close();
			out().close();
			break;
		default:
			fireTelnetCommand(command);
		}

	}

	//-------------------------------------------------------------------
	public void processSubnegotiation(int code, int[] values) {
		logger.log(Level.TRACE, "Subnegotiation for {0}: {1}", code, Arrays.toString((values)));

		OptionEntry support = options.get(code);
		if (support!=null) {
			support.handler.handleSubnegotiation(support.role,values, this, out);
		}
	}

	//-------------------------------------------------------------------
	public Charset getCharset() {
		return charset;
	}

}
