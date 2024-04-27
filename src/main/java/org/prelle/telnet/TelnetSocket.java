/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author prelle
 *
 */
public class TelnetSocket extends Socket implements TelnetStreamListener {

	private final static Logger logger = System.getLogger("telnet.lvl3");

	private TelnetInputStream debugIn;
	private TelnetOutputStream debugOut;
	private boolean inClientMode;
	
	/**
	 * These options should be supported on this socket
	 */
	private List<TelnetOptions> supportedOptions = new ArrayList<TelnetOptions>();
	private Map<Class<? extends TelnetOptionHandler>, Map<String, Object>> optionVariables = new HashMap<>();
//	private Map<Integer,WillVariable> willVariables = new HashMap<Integer, WillVariable>();
////	/** Has DO been sent? */
//	private Map<Integer,DoVariable>   doVariables   = new HashMap<Integer, DoVariable>();
	private Map<TelnetOptionHandler,Object> optionState = new HashMap<TelnetOptionHandler, Object>();
	private List<TelnetOptionListener> optionListener = new ArrayList<TelnetOptionListener>();
	private List<TelnetOptions> answerExpected = new ArrayList<TelnetOptions>();
	private List<TelnetOptions> activeFeatures = new ArrayList<TelnetOptions>();
	
	public static TelnetSocketBuilder builder() {
		return new TelnetSocketBuilder();
	}

	//-----------------------------------------------------------------
	/**
	 * @param passiveSupport 
	 */
	public TelnetSocket(List<TelnetOptions> supported, List<TelnetOptions> passiveSupport) {
		inClientMode = false;
		this.supportedOptions = new ArrayList<TelnetOptions>(supported);
		this.supportedOptions.addAll(passiveSupport);
	}

	//-----------------------------------------------------------------
	public TelnetSocket(TelnetSocketBuilder builder) throws UnknownHostException, IOException {
		super(builder.host, builder.port);
		inClientMode = true;	
		this.activeFeatures   = new ArrayList<TelnetOptions>(builder.activelyRequest);
		this.supportedOptions = new ArrayList<TelnetOptions>(builder.passiveSupport);
		getInputStream();
		getOutputStream();
	}

	//-----------------------------------------------------------------
	/**
	 * @param proxy
	 */
	public TelnetSocket(Proxy proxy) {
		super(proxy);
		inClientMode = false;
		loadVariableDefaults();
	}

	//-----------------------------------------------------------------
	/**
	 * @param impl
	 * @throws SocketException
	 */
	public TelnetSocket(SocketImpl impl) throws SocketException {
		super(impl);
		loadVariableDefaults();
	}

	//-----------------------------------------------------------------
	/**
	 * @param host
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TelnetSocket(String host, int port) throws UnknownHostException,
	IOException {
		super(host, port);
		inClientMode = true;
		loadVariableDefaults();
	}

	//-----------------------------------------------------------------
	/**
	 * @param address
	 * @param port
	 * @throws IOException
	 */
	public TelnetSocket(InetAddress address, int port) throws IOException {
		super(address, port);
		inClientMode = true;
		loadVariableDefaults();
	}

	//-----------------------------------------------------------------
	/**
	 * @param host
	 * @param port
	 * @param localAddr
	 * @param localPort
	 * @throws IOException
	 */
	public TelnetSocket(String host, int port, InetAddress localAddr,
			int localPort) throws IOException {
		super(host, port, localAddr, localPort);
		inClientMode = true;
		loadVariableDefaults();
	}

	//-----------------------------------------------------------------
	/**
	 * @param address
	 * @param port
	 * @param localAddr
	 * @param localPort
	 * @throws IOException
	 */
	public TelnetSocket(InetAddress address, int port, InetAddress localAddr,
			int localPort) throws IOException {
		super(address, port, localAddr, localPort);
		inClientMode = true;
		loadVariableDefaults();
	}

	//-----------------------------------------------------------------
	private void loadVariableDefaults() {
	}

	//-----------------------------------------------------------------
	/**
	 * @see java.net.Socket#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (debugIn==null)
			debugIn = new TelnetInputStream(
					this,
					new TelnetDebuggingInputStream(super.getInputStream()));
		return debugIn;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.net.Socket#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (debugOut==null)
			debugOut = new TelnetOutputStream(
					new TelnetDebuggingOutputStream(super.getOutputStream()));
		return debugOut;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedGoAheadSignal()
	 */
	@Override
	public void receivedGoAheadSignal() {
		// TODO Auto-generated method stub
		logger.log(Level.DEBUG,"Go Ahead received");

	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedInterruptProcess()
	 */
	@Override
	public void receivedInterruptProcess() {
		// TODO Auto-generated method stub
		logger.log(Level.WARNING,"TODO: Interrupt process received");
		try {
			this.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-----------------------------------------------------------------
	public TelnetOptions getFromSupportedOptions(int optionCode) {
		for (TelnetOptions opt : supportedOptions) {
			if (opt.getCode()==optionCode)
				return opt;
		}
		return null;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedWILL(int)
	 */
	@Override
	public void receivedWILL(int optionCode) {
		TelnetOptions known = TelnetOptions.valueOf(optionCode);
		TelnetOptions option = getFromSupportedOptions(optionCode);
		try {
			if (option==null && known==null) {
				logger.log(Level.WARNING,"remote party offers unknown option {0}", optionCode);
				debugOut.sendDont(optionCode);
				return;
			}
			if (option==null && known!=null) {
				logger.log(Level.WARNING,"remote party offers unsupported option {0} ({1})", known.getCode(), optionCode);
				debugOut.sendDont(optionCode);
				return;
			}
		
			// Did we request this?
			boolean wasRequested = answerExpected.contains(option);
			TelnetOptionHandler handler = option.getOptionHandler();
			if (wasRequested) {
				logger.log(Level.WARNING,"remote party agrees to do {0} ({1})", option.name(), optionCode);
				answerExpected.remove(option);
				handler.optionEnabled(this, true);
				activeFeatures.add(option);
			} else {
				// We did not request this
				logger.log(Level.WARNING,"remote party offers {0} ({1}), but we did not request this", handler.getName(), optionCode);
				debugOut.sendDo(optionCode);
				// DOes it make sense to inform handler about this?
				//handler.remotePartyOffered(this);
			}
		} catch (IOException e) {
			logger.log(Level.ERROR,"Could not answer that WILL offer: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedWONT(int)
	 */
	@Override
	public void receivedWONT(int optionCode) {
		TelnetOptions known = TelnetOptions.valueOf(optionCode);
		TelnetOptions option = getFromSupportedOptions(optionCode);
		try {
			if (option==null && known==null) {
				logger.log(Level.WARNING,"remote party rejects unknown option {0}", optionCode);
				return;
			}
			if (option==null && known!=null) {
				logger.log(Level.WARNING,"remote party rejects unsupported option {0} ({1})", known.getCode(), optionCode);
				return;
			}
			
			// Did we request this?
			boolean wasRequested = answerExpected.contains(option);
			TelnetOptionHandler handler = option.getOptionHandler();
			if (wasRequested) {
				logger.log(Level.WARNING,"remote party rejected to do {0} ({1})", option.name(), optionCode);
				answerExpected.remove(option);
				// Does it make sense to inform handler?
				handler.optionDisabled(this, true);
			} else {
				// We did not request this
				logger.log(Level.WARNING,"Weird! Remote party sends unsolicited WONT for {0} ({1})", option.name(), optionCode);
			}
		} catch (Exception e) {
			logger.log(Level.ERROR,"Could not answer that WONT answer: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedDO(int)
	 */
	@Override
	public void receivedDO(int optionCode) {
		TelnetOptions known = TelnetOptions.valueOf(optionCode);
		TelnetOptions option = getFromSupportedOptions(optionCode);
		try {
			if (option==null && known==null) {
				logger.log(Level.WARNING,"remote party requests unknown option {0}", optionCode);
				debugOut.sendWont(optionCode);
				return;
			}
			if (option==null && known!=null) {
				logger.log(Level.WARNING,"remote party requests unsupported option {0} ({1})", known.getCode(), optionCode);
				debugOut.sendWont(optionCode);
				return;
			}
			
			// Did we request this?
			boolean wasRequested = answerExpected.contains(option);
			TelnetOptionHandler handler = option.getOptionHandler();
			if (wasRequested) {
				logger.log(Level.WARNING,"remote party accepts offer to do {0} ({1})", option.name(), optionCode);
				handler.optionEnabled(this, true);
				answerExpected.remove(option);
			} else {
				// We did not request this
				logger.log(Level.WARNING,"remote party requests to do {0} ({1})", option.name(), optionCode);
				handler.optionEnabled(this, false);
				debugOut.sendWill(optionCode);
			}
			activeFeatures.add(option);
		} catch (IOException e) {
			logger.log(Level.ERROR,"Could not answer that DO request: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedDONT(int)
	 */
	@Override
	public void receivedDONT(int optionCode) {
		TelnetOptions known = TelnetOptions.valueOf(optionCode);
		TelnetOptions supported = getFromSupportedOptions(optionCode);
		try {
			if (supported==null && known==null) {
				logger.log(Level.WARNING,"remote party stops unknown option {0}", optionCode);
				return;
			}
			if (supported==null && known!=null) {
				logger.log(Level.WARNING,"remote party stops unsupported option {0} ({1})", known.getCode(), optionCode);
				return;
			}
			
			// Did we request this?
			boolean wasRequested = answerExpected.contains(supported);
			TelnetOptionHandler handler = supported.getOptionHandler();
			if (wasRequested) {
				logger.log(Level.WARNING,"remote party rejects out offer to do {0} ({1})", supported.name(), optionCode);
				answerExpected.remove(supported);
				handler.optionDisabled(this, true);
				// Does it make sense to inform handler?
				//handler.remotePartyRejected(this);
			} else {
				// We did not request this
				logger.log(Level.WARNING,"Weird! Remote party sends unsolicited DONT for {0} ({1})", supported.name(), optionCode);
				handler.optionDisabled(this, false);
			}
		} catch (Exception e) {
			logger.log(Level.ERROR,"Could not answer that WONT answer: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedSubnegotiationBegin(int)
	 */
	@Override
	public void receivedSubnegotiationBegin(int optionCode) {
		TelnetOptions supported = getFromSupportedOptions(optionCode);
		TelnetOptionHandler option = supported.getOptionHandler();
//		TelnetOption option = TelnetConfiguration.getOption(optionCode);
		try {
			if (option==null) {
				logger.log(Level.WARNING,"remote party performs subnegitation for unknown option "+optionCode);
			} else {
				logger.log(Level.DEBUG,"subnegotiation startet for "+option.getName());
				debugIn.setHigherLevelControl(true);
				option.performSubNegotiation(this, debugIn);
				debugIn.setHigherLevelControl(false);
			}
		} catch (IOException e) {
			logger.log(Level.ERROR,"Could not answer that WILL offer: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/**
	 * @return the inClientMode
	 */
	public boolean isInClientMode() {
		return inClientMode;
	}

	//-----------------------------------------------------------------
	/**
	 * @param inClientMode the inClientMode to set
	 */
	public void setInClientMode(boolean inClientMode) {
		this.inClientMode = inClientMode;
	}

//	//-----------------------------------------------------------------
//	public WillVariable getWillVariable(int code) {
//		return willVariables.get(code);
//	}
//
//	//-----------------------------------------------------------------
//	public DoVariable getDoVariable(int code) {
//		return doVariables.get(code);
//	}
//
//	//--------------------------------------------------------------
//	public boolean[] getDoWillStatesFor(TelnetOptionHandler opt) {
//		boolean[] ret = new boolean[]{
//				doVariables.get(opt.getCode()).getState(), 
//				willVariables.get(opt.getCode()).getState()};
//		logger.log(Level.DEBUG,"Do/Will states for "+opt.getName()+" are: "+Arrays.toString(ret));
//		return ret;
//	}
//
//	//-----------------------------------------------------------------
//	public void setOptionVariable(TelnetVariable variable) {
//		if (variable instanceof WillVariable)
//			willVariables.put(variable.getName(), (WillVariable) variable);
//		else if (variable instanceof DoVariable)
//			doVariables.put(variable.getName(), (DoVariable) variable);
//		
//	}
	
	//-------------------------------------------------------------------
	public void setOptionVariable(Class<? extends TelnetOptionHandler> option, String variable, Object value) {
		Map<String,Object>  perOptionVars = optionVariables.getOrDefault(variable, new HashMap<>());
		perOptionVars.put(variable, value);
		optionVariables.put(option, perOptionVars);
	}
	
	//-------------------------------------------------------------------
	public Object getOptionVariable(Class<? extends TelnetOptionHandler> option, String variable) {
		Map<String,Object>  perOptionVars = optionVariables.getOrDefault(variable, new HashMap<>());
		return perOptionVars.get(variable);
	}

	//-----------------------------------------------------------------
	public void setOptionState(TelnetOptionHandler option, Object stateObject) {
		optionState.put(option, stateObject);
	}

	//-----------------------------------------------------------------
	public Object getOptionState(TelnetOptionHandler option) {
		return optionState.get(option);
	}

	//-----------------------------------------------------------------
	public void addOptionListener(TelnetOptionListener optList) {
		if (!optionListener.contains(optList))
			optionListener.add(optList);
	}

	//-----------------------------------------------------------------
	public void fireOptionDataChanged(TelnetOptionHandler option,Object data) {
		logger.log(Level.DEBUG, "fireOptionDataChange({0})", data.getClass().getSimpleName());
		for (TelnetOptionListener list : optionListener)
			try {
				list.telnetOptionDataChanged(this, option, data);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetOptionDataChanged: "+e,e);
			}
	}

	//-----------------------------------------------------------------
	public void requestEcho() throws IOException {
		TelnetOptions.ECHO.getOptionHandler().requestUsage(this);
//		if (TelnetConfiguration.getOption(TelnetEcho.CODE)!=null)
//			TelnetConfiguration.getOption(TelnetEcho.CODE).requestUsage(this);
	}

//	//-----------------------------------------------------------------
//	public boolean isEchoEnabled() throws IOException {
//		return doVariables.get(TelnetEcho.CODE).getState();
//	}

	//-----------------------------------------------------------------
	public void stopEcho() throws IOException {
		TelnetOptions.ECHO.getOptionHandler().requestStop(this);
	}

	//-----------------------------------------------------------------
	public void expectedAnswerFor(TelnetOptions option) {
		if (!answerExpected.contains(option))
			answerExpected.add(option);
	}
	
	//-------------------------------------------------------------------
	public boolean isFeatureActive(TelnetOptions option) {
		return activeFeatures.contains(option);
	}
	
	public static class TelnetSocketBuilder {
		
		private String host;
		private int port;
	    /** Send DO requests for these options on incoming connections */
	    private List<TelnetOptions> activelyRequest = new ArrayList<>();
	    /** */
	    private List<TelnetOptions> passiveSupport = new ArrayList<>();
	    
	    private TelnetSocketBuilder() {
	    	activelyRequest = new ArrayList<TelnetOptions>();
	    	passiveSupport  = new ArrayList<TelnetOptions>();
	    }
	    public TelnetSocketBuilder connectTo(String host, int port) {
	    	this.host = host;
	    	this.port = port;
	    	return this;
	    }

		//-----------------------------------------------------------------
		public TelnetSocketBuilder activelyRequest(TelnetOptions value) {
			activelyRequest.add(value);
			logger.log(Level.DEBUG, "I will ask server to perform {0} ({1})", value.name(), value.getCode());
			return this;
		}

		//-----------------------------------------------------------------
		public TelnetSocketBuilder passivelySupport(TelnetOptions value) {
			passiveSupport.add(value);
			logger.log(Level.DEBUG, "I will tell clients that I support {0} ({1})", value.name(), value.getCode());
			return this;
		}

		//-----------------------------------------------------------------
		public TelnetSocketBuilder withMTP() {
			activelyRequest(TelnetOptions.MTP);
			return this;
		}
		
		//-----------------------------------------------------------------
		public TelnetSocket build() throws UnknownHostException, IOException {
			return new TelnetSocket(this);
		}
	}
}
