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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.prelle.telnet.TelnetSocket.State;

/**
 * @author prelle
 *
 */
public class TelnetSocket extends Socket implements TelnetConstants {

	public static enum State {
		CREATED,
		OPTION_NEGOTIATION,
		OPTION_SUBNEGOTIATION,
		READY,
		DISCONNECTED
	}

	private final static Logger logger = System.getLogger("telnet.lvl3");

	/**
	 * Used to start longer running task upon reception of data in
	 * the input stream, so that the input stream isn't blocked
	 */
	//private static BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
	private static ExecutorService executor= Executors.newFixedThreadPool(1);

	protected TelnetInputStream in;
	protected TelnetOutputStream out;
	private CommunicationRole role;
	private State state = State.CREATED;

	private List<TelnetSocketListener> socketListener = new ArrayList<>();
	Map<Integer, ControlCode> negotiate = new LinkedHashMap<>();
	private Map<Integer, TelnetOptionListener> optionListener = new HashMap<>();
	private TelnetOptionCapabilities optionCaps = new TelnetOptionCapabilities();;

	private List<Integer> active = new ArrayList<>();
	Map<Integer, ControlCode> lastStateSent = new HashMap<>();




	//-----------------------------------------------------------------
	public static ExecutorService getExecutorService() {
		return executor;
	}

	//-----------------------------------------------------------------
	public TelnetSocket() {
		role = CommunicationRole.SERVER;
		optionCaps.capabilities.put(TelnetOption.ECHO    , new TelnetConfigOption());
		optionCaps.capabilities.put(TelnetOption.EOR     , new TelnetConfigOption());
		optionCaps.capabilities.put(TelnetOption.LINEMODE, new TelnetConfigOption());
	}

	//-----------------------------------------------------------------
	public TelnetSocket(String host, int port) throws UnknownHostException, IOException {
		super(host, port);
		role = CommunicationRole.CLIENT;
		negotiate.put(0, ControlCode.DO);
		active.add(0);
		getOutputStream();
		getInputStream();
		out().logger = System.getLogger("telnet.lvl1.out."+host);
		in().logger = System.getLogger("telnet.lvl1.in."+host);
//		initialize();
		optionCaps.capabilities.put(TelnetOption.ECHO    , new TelnetConfigOption());
		optionCaps.capabilities.put(TelnetOption.EOR     , new TelnetConfigOption());
		optionCaps.capabilities.put(TelnetOption.LINEMODE, new TelnetConfigOption());
	}

	//-------------------------------------------------------------------
	public TelnetConfigOption getConfigOption(TelnetOption key) {
		return optionCaps.getConfigOption(key);
	}

	//-------------------------------------------------------------------
	public Set<Entry<TelnetOption, TelnetConfigOption>> getCapabilities() {
		return optionCaps.getCapabilities();
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
	public TelnetOutputStream out() throws IOException {
		return (TelnetOutputStream) getOutputStream();
	}

	//-----------------------------------------------------------------
	TelnetInputStream in() throws IOException {
		return (TelnetInputStream) getInputStream();
	}

	//-------------------------------------------------------------------
	public TelnetSocket support(int code, ControlCode willOrDo) {
//		if (willOrDo!=ControlCode.WILL && willOrDo!=ControlCode.DO)
//			throw new IllegalArgumentException("Only WILL or DO expected here");
		negotiate.put(code, willOrDo);
		return this;
	}

	//-------------------------------------------------------------------
	public TelnetSocket support(int code, ControlCode willOrDo, Object configData) {
		optionCaps.setOptionData(code, configData);
		return support(code, willOrDo);
	}

	//-----------------------------------------------------------------
	public TelnetSocket addSocketListener(TelnetSocketListener optList) {
		if (!socketListener.contains(optList))
			socketListener.add(optList);
		return this;
	}

	//-----------------------------------------------------------------
	void setState(State newState) {
		State oldState = state;
		this.state = newState;
		for (TelnetSocketListener callback : socketListener) {
			try {
				callback.telnetSocketChanged(this, oldState, newState);
			} catch (Throwable e) {
				logger.log(Level.ERROR, "Failed processing socket state change",e);
			}
		}
	}

	//-----------------------------------------------------------------
	public TelnetSocket setOptionListener(int code, TelnetOptionListener callback) {
		logger.log(Level.DEBUG, "Send events for option {0} to {1}", code, callback);
		if (callback==null)
			throw new NullPointerException();
		optionListener.put(code, callback);
		return this;
	}
	//-----------------------------------------------------------------
	public TelnetSocket setOptionListener(TelnetOption option, TelnetOptionListener callback) {
		return setOptionListener(option.getCode(), callback);
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

	//-----------------------------------------------------------------
	void fireFeatureActive(TelnetOption option, boolean state) {
		logger.log(Level.DEBUG, "fireFeatureActive({0},{1}, in {2})", option,state, this.state);

		if (this.state==State.OPTION_NEGOTIATION) {
			logger.log(Level.INFO, "Option {0} has been {1}", option.name(), state?"ENABLED":"REJECTED");
			TelnetConfigOption opt = optionCaps.getConfigOption(option);
			if (opt==null) {
				opt = new TelnetConfigOption();
				optionCaps.capabilities.put(option, opt);
			}
			opt.setConfigurable(state);
			opt.setActive(state);
		}

		if (state && !active.contains( (Integer)option.getCode() )) {
			active.add( option.getCode());
		}
		synchronized (optionCaps.capExchangeAwaitResponses) {
			if (optionCaps.capExchangeAwaitResponses.contains( (Integer)option.getCode())) {
				optionCaps.capExchangeAwaitResponses.remove((Integer)option.getCode());
				if (optionCaps.capExchangeAwaitResponses.isEmpty()) {
					optionCaps.capExchangeAwaitResponses.notify();
				}
			}
		}

		for (TelnetSocketListener list : socketListener) {
			try {
				list.telnetOptionStatusChange(this, option, state);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetOptionStatusChange: "+e,e);
			}
		}
	}

	//-----------------------------------------------------------------
	public void fireTelnetCommand(TelnetCommand command) {
		for (TelnetSocketListener list : socketListener)
			try {
				list.telnetCommandReceived(this, command);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetCommandReceived: "+e,e);
			}
	}

	//-------------------------------------------------------------------
	public boolean isFeatureActive(int code) {
		return active.contains(code);
	}

	//-------------------------------------------------------------------
	public boolean isFeatureSupported(TelnetOption option) {
		return (getConfigOption(option)!=null && getConfigOption(option).isConfigurable());
	}

	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetStreamListener#processCommand(org.prelle.telnet.TelnetCommand)
//	 */
//	@Override
	void processCommand(TelnetCommand command) throws IOException {
		logger.log(Level.DEBUG, "RCV "+command);
		switch (command.getCode()) {
		case DO  : case DONT:
		case WILL: case WONT:
			break;
		default:
			logger.log(Level.DEBUG, "fire "+command);
			fireTelnetCommand(command);
			return;
		}

		if (command.getData()==null) {
			logger.log(Level.ERROR, "Received {0} without an option code", command.getCode());
			return;
		}

		int optionCode = command.getData();
		TelnetOption option = TelnetOption.valueOf(optionCode);
		TelnetSubnegotiationHandler handler = TelnetOptionRegistry.get(optionCode);

		ControlCode lastState = lastStateSent.getOrDefault(optionCode,  ControlCode.WONT);

		// See if this is configured
		ControlCode config = negotiate.get(optionCode);
		if (config!=null) {
			switch (command.getCode()) {
			case DO:
				// Remote party wants us to do something
				if (config==ControlCode.WILL) {
					if (active.contains(optionCode))
						return;
					if (!active.contains(optionCode))
						active.add(optionCode);
					if (lastState==ControlCode.WILL) {
						logger.log(Level.TRACE, "Don't respond to DO {0} , state would not change", option.name());
					} else {
						out.sendWill(optionCode);
						logger.log(Level.WARNING, "Remote party sends DO {0} and we agreed with WILL {0}", option.name());
					}
//					if (handler!=null) {
//						handler.initializeAs(option, role, this, out);
//					}
					fireFeatureActive(option, true);
					return;
				} else if (config==ControlCode.DO) {
					logger.log(Level.WARNING, "Remote party sends DO {0} and we already sent DO {0}- answering with WILL", option.name());
					out.sendWill(optionCode);
					fireFeatureActive(option, true);
					return;
				}
				break;
			case WILL:
				// Remote party offers to do something
				if (config==ControlCode.DO) {
					if (active.contains(optionCode))
						return;
					if (!active.contains(optionCode))
						active.add(optionCode);
					if (lastState==ControlCode.DO) {
						logger.log(Level.DEBUG, "Don't respond to WILL {0} - state would not change", option.name());
					} else {
						out.sendDo(optionCode);
						logger.log(Level.WARNING, "Remote party offers WILL {0} and we agreed with DO {0}", option.name());
					}
//					if (handler!=null)
//						handler.initializeAs(option, role, this, out);
					fireFeatureActive(option, true);
					return;
				}
				break;
			case DONT:
			case WONT:
				for (int i=0; i<active.size(); i++) {
					if (active.get(i)==optionCode) {
						active.remove(i);
						break;
					}
				}
				fireFeatureActive(option, false);
				return;
			}
		} else {
			logger.log(Level.WARNING, "Remote party offered unsupported option {0}={1}", optionCode, option);
		}

		// If there is an option listener for that Telnet option, ask what to do
		TelnetOptionListener listener = optionListener.get(optionCode);
		if (listener!=null) {
//			logger.log(Level.INFO, "no preconfig for {0}, but a listener exists", option);
			listener.remotePartySent(this, optionCode, command);
			return;
		}

		if (option==null) {
			logger.log(Level.WARNING, "Remote party requests {0} for unknown option {1} and we reject it", command.getCode(), command.getData());
		} else {
			logger.log(Level.WARNING, "Remote party requests {0} for option {1} and we reject it = {2}", command.getCode(), option.name(), config);
		}

		reject(command);


	}

	//-------------------------------------------------------------------
	private void reject(TelnetCommand command) throws IOException {
		switch (command.getCode()) {
		case DO  : out.sendWont(command.getData()); break;
		case WILL: out.sendDont(command.getData()); break;
		}
	}

//	//-----------------------------------------------------------------
//	public void sendSubnegotiation(int code, int[] values) throws IOException {
//		byte[] toSend = new byte[values.length+5];
//		toSend[0] = (byte)IAC;
//		toSend[1] = (byte)SB;
//		toSend[2] = (byte)code;
//		for (int i=0; i<values.length; i++)
//			toSend[i+3] = (byte) values[i];
//		toSend[toSend.length-2] = (byte)IAC;
//		toSend[toSend.length-1] = (byte)SE;
//
//		out.write(toSend);
//	}
//
//	//-----------------------------------------------------------------
//	public void sendSubnegotiation(int code, byte[] values) throws IOException {
//		byte[] toSend = new byte[values.length+5];
//		toSend[0] = (byte)IAC;
//		toSend[1] = (byte)SB;
//		toSend[2] = (byte)code;
//		System.arraycopy(values, 0, toSend, 3, values.length);
//		toSend[toSend.length-2] = (byte)IAC;
//		toSend[toSend.length-1] = (byte)SE;
//
//	}

	//-------------------------------------------------------------------
	public void processSubnegotiation(int code, int[] values) {
		logger.log(Level.TRACE, "RCV Subnegotiation for {0}: {1}", code, Arrays.toString((values)));

		TelnetSubnegotiationHandler handler = TelnetOptionRegistry.get(code);
		if (handler==null) {
			logger.log(Level.WARNING, "Received {2} bytes subnegotiation for {0}/{1}, but cannot find a TelnetOptionHandler", code, TelnetOption.valueOf(code), values.length);
			return;
		}

		handler.handleSubnegotiation(code, values, this, out);
	}

	//-------------------------------------------------------------------
	public CompletableFuture<TelnetOptionCapabilities> initialize() throws IOException {
		getOutputStream();
		getInputStream();

		setState(State.OPTION_NEGOTIATION);
		CompletableFuture<TelnetOptionCapabilities> negotiation = CompletableFuture
				.runAsync(new NegotiateOptionsTask(this))
				.thenRunAsync(new SubnegotiationTask(this))
				.thenCompose(new Function<Void,CompletableFuture<TelnetOptionCapabilities>>() {
					public CompletableFuture<TelnetOptionCapabilities> apply(Void arg0) {
						return CompletableFuture.completedFuture(optionCaps);
					}
				})
				;
		return negotiation;
	}


	//-------------------------------------------------------------------
	/**
	 * Called by TelnetSubnegotiationHandler implementor classes
	 */
	public void subnegotiationEndedFor(int optionCode, Object data) {
		setOptionData(optionCode, data);
		logger.log(Level.INFO, "Negotiation for option {0}/{2} received {1}", optionCode, data, TelnetOption.valueOf(optionCode));
		logger.log(Level.DEBUG, "expected are "+optionCaps.capSubNegAwaitResponses);
		logger.log(Level.DEBUG, "status is "+state);

		if (state==State.OPTION_SUBNEGOTIATION) {
			synchronized (optionCaps.capSubNegAwaitResponses) {
				if (optionCaps.capSubNegAwaitResponses.contains( (Integer)optionCode)) {
					optionCaps.capSubNegAwaitResponses.remove((Integer)optionCode);
					if (optionCaps.capSubNegAwaitResponses.isEmpty()) {
						logger.log(Level.WARNING, "DONE SUBNEG------------------------------");
						optionCaps.capSubNegAwaitResponses.notify();
					}
				}
			}
		}
	}

	//-------------------------------------------------------------------
	public TelnetOptionCapabilities getNegotiationResult() {
		return optionCaps;
	}

	//-------------------------------------------------------------------
	CommunicationRole getCommunicationRole() {
		return role;
	}

	//-------------------------------------------------------------------
	public List<Integer> getActiveOptions() {
		return active;
	}

}

class NegotiateOptionsTask implements Runnable, TelnetConstants {

	private final static Logger logger = System.getLogger("telnet.lvl3");

	private TelnetSocket socket;
	private TelnetOutputStream out;
	private TelnetOptionCapabilities capabilities;
	
	private boolean taskFailed = false;

	//-------------------------------------------------------------------
	public NegotiateOptionsTask(TelnetSocket socket) throws IOException {
		this.socket = socket;
		this.out    = socket.out();
		capabilities= socket.getNegotiationResult();
	}

	//-------------------------------------------------------------------
	public boolean isTaskFailed() {
		return taskFailed;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.log(Level.WARNING, "ENTER: Detect supported TELNET options");
		try {
			out.write("Detecting Telnet capabilities\r\n".getBytes(StandardCharsets.US_ASCII));
			capabilities.capExchangeAwaitResponses.clear();
			for (Entry<Integer, ControlCode> entry : socket.negotiate.entrySet()) {
				socket.lastStateSent.put(entry.getKey(), entry.getValue());
				capabilities.capExchangeAwaitResponses.add( entry.getKey() );
				if (entry.getValue()==ControlCode.DO) {
					out.sendDo(entry.getKey());
				} else if (entry.getValue()==ControlCode.WILL)
					out.sendWill(entry.getKey());
				else if (entry.getValue()==ControlCode.WONT)
					out.sendWont(entry.getKey());
				else
					logger.log(Level.WARNING, "Ignore operation "+entry.getValue()+" for "+entry.getKey());
			}
			socket.setState(State.OPTION_NEGOTIATION);

			logger.log(Level.INFO, "Initiated option negotiation for all options - waiting for results");
			Instant before = Instant.now();
			synchronized (capabilities.capExchangeAwaitResponses) {
				capabilities.capExchangeAwaitResponses.wait(500);
			}
			Duration dura = Duration.between(before, Instant.now());
			logger.log(Level.DEBUG, "TELNET option negotiation required {0} ms\n", dura.toMillis());
		} catch (SocketException e) {
			logger.log(Level.ERROR, "Connection to client lost: "+e);
			socket.setState(State.DISCONNECTED);
			taskFailed = true;
		} catch (Exception e) {
			logger.log(Level.ERROR, "Error in Telnet capability exchange",e);
			taskFailed = true;
		} finally {
			logger.log(Level.WARNING, "LEAVE: Detect supported TELNET options\n");
		}
	}
}

class SubnegotiationTask implements Runnable, TelnetConstants {

	private final static Logger logger = System.getLogger("telnet.lvl3");

	private TelnetSocket socket;
	private TelnetOutputStream out;
	private TelnetOptionCapabilities capabilities;

	//-------------------------------------------------------------------
	public SubnegotiationTask(TelnetSocket socket) throws IOException {
		this.socket = socket;
		this.out    = socket.out();
		capabilities= socket.getNegotiationResult();
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.log(Level.WARNING, "ENTER: TELNET subnegotiation");
		try {
			out.write("Telnet Negotiate\r\n".getBytes(StandardCharsets.US_ASCII));
			capabilities.capExchangeAwaitResponses.clear();
			logger.log(Level.WARNING, "Subnegotiation starts for active options = {0}", socket.getActiveOptions());
			for (Integer optionCode : new ArrayList<Integer>(socket.getActiveOptions())) {
				TelnetOption option = TelnetOption.valueOf(optionCode);
				TelnetSubnegotiationHandler handler = TelnetOptionRegistry.get(optionCode);
				if (handler!=null) {
					TelnetSubnegotiationHandler.logger.log(Level.INFO, "send subnegotiation for {0}", option.name());
					boolean expectResult = handler.initializeAs(option, socket.getCommunicationRole(), socket, out);
					if (expectResult) {
						capabilities.capSubNegAwaitResponses.add(optionCode);
					}
				} else
					logger.log(Level.TRACE, "no subnegotiation for {0}", option.name());
			}
			socket.setState(State.OPTION_SUBNEGOTIATION);
			logger.log(Level.WARNING, "wait for all subnegs\n");
			Instant before = Instant.now();
			synchronized (capabilities.capSubNegAwaitResponses) {
				capabilities.capSubNegAwaitResponses.wait(500);
			}
			Duration dura = Duration.between(before, Instant.now());
			logger.log(Level.WARNING, "TELNET subnegotiation required {0} ms\n", dura.toMillis());
			socket.setState(TelnetSocket.State.READY);
			logger.log(Level.WARNING, "After setting state to READY");
		} catch (SocketException e) {
			logger.log(Level.ERROR, "Error in Telnet capability exchange: "+e);
			socket.setState(State.DISCONNECTED);
		} catch (Exception e) {
			logger.log(Level.ERROR, "Error in Telnet capability exchange",e);
		} finally {
			logger.log(Level.WARNING, "LEAVE: TELNET subnegotiation\n");
		}
	}

}