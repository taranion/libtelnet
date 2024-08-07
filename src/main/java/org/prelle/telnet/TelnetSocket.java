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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author prelle
 *
 */
public class TelnetSocket extends Socket implements TelnetConstants {

	private final static Logger logger = System.getLogger("telnet.lvl3");

	/**
	 * Used to start longer running task upon reception of data in
	 * the input stream, so that the input stream isn't blocked
	 */
	//private static BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
	private static ExecutorService executor= Executors.newFixedThreadPool(1);

	private TelnetInputStream in;
	private TelnetOutputStream out;
	private CommunicationRole role;

	private List<TelnetSocketListener> socketListener = new ArrayList<>();
	private Map<Integer, ControlCode> negotiate = new LinkedHashMap<>();
	private Map<Integer, Object> configData = new LinkedHashMap<>();
	private Map<Integer, TelnetOptionListener> optionListener = new HashMap<>();

	private List<Integer> active = new ArrayList<>();
	private Map<Integer, ControlCode> lastStateSent = new HashMap<>();


	private List<Integer> capExchangeAwaitResponses = new ArrayList<>();
	private TimerTask capExchangeWaitForOptions;
	private static Timer timer = new Timer("SocketOptionTimer");
	private static Instant exchangeStart;
	private Map<TelnetOption, TelnetConfigOption> capabilities;

	/**
	 * Is set to TRUE, when LINEMODE option is supported
	 */
	private boolean canEnterCharacterMode;
	private boolean canDisableEcho;


	//-----------------------------------------------------------------
	public static ExecutorService getExecutorService() {
		return executor;
	}

	//-----------------------------------------------------------------
	public TelnetSocket() {
		role = CommunicationRole.SERVER;
		capabilities   = new HashMap<>();
		capabilities.put(TelnetOption.ECHO    , new TelnetConfigOption());
		capabilities.put(TelnetOption.EOR     , new TelnetConfigOption());
		capabilities.put(TelnetOption.LINEMODE, new TelnetConfigOption());
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
		capabilities   = new HashMap<>();
		capabilities.put(TelnetOption.ECHO    , new TelnetConfigOption());
		capabilities.put(TelnetOption.EOR     , new TelnetConfigOption());
		capabilities.put(TelnetOption.LINEMODE, new TelnetConfigOption());
	}

	//-------------------------------------------------------------------
	public TelnetConfigOption getConfigOption(TelnetOption key) {
		return capabilities.get(key);
	}

	//-------------------------------------------------------------------
	public Set<Entry<TelnetOption, TelnetConfigOption>> getCapabilities() {
		return capabilities.entrySet();
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
		this.configData.put(code, configData);
		return support(code, willOrDo);
	}

	//-----------------------------------------------------------------
	public TelnetSocket addSocketListener(TelnetSocketListener optList) {
		if (!socketListener.contains(optList))
			socketListener.add(optList);
		return this;
	}

	//-----------------------------------------------------------------
	public TelnetSocket setOptionListener(int code, TelnetOptionListener callback) {
		logger.log(Level.DEBUG, "Send events for option {0} to {1}", code, callback);
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
		return (E) configData.get(code);
	}

	//-----------------------------------------------------------------
	/**
	 * Store data related to a specific config option on this connection
	 */
	public void setOptionData(int code, Object value) {
		configData.put(code, value);
	}

	//-----------------------------------------------------------------
	/**
	 * All WILL and DOs have been exchanged
	 */
	private void fireOptionPhaseDone() {
		logger.log(Level.INFO, "All telnet options are known");
		capExchangeAwaitResponses.clear();
		// Measure how long Telnet option exchange took
		Duration dur = Duration.between(exchangeStart, Instant.now());
		logger.log(Level.WARNING, "Option exchange required {0} milliseconds", dur.toMillis());

		for (TelnetSocketListener list : socketListener)
			try {
//				System.err.println("STOP: "+Instant.now()+"  in TelnetSocket.fireOptionPhaseDone");
				list.telnetSupportedOptionsKnown(this);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetSupportedOptionsKnown: "+e,e);
			}
	}

	//-----------------------------------------------------------------
	public void fireFeatureActive(TelnetOption option, boolean state) {
		logger.log(Level.DEBUG, "fireFeatureActive({0},{1})", option,state);
		for (TelnetSocketListener list : socketListener)
			try {
				list.telnetOptionStatusChange(this, option, state);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetOptionStatusChange: "+e,e);
			}

		if (state && !active.contains( (Integer)option.getCode() ))
			active.add( option.getCode());
		if (capExchangeAwaitResponses.contains( (Integer)option.getCode())) {
			capExchangeAwaitResponses.remove((Integer)option.getCode());
			if (capExchangeAwaitResponses.isEmpty()) {
				logger.log(Level.INFO, "DONE-------------------------------------");
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
			logger.log(Level.WARNING, "fire "+command);
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
						logger.log(Level.DEBUG, "Don't respond to DO {0} , state would not change", option.name());
					} else {
						out.sendWill(optionCode);
						logger.log(Level.WARNING, "Remote party sends DO {0} and we agreed with WILL {0}", option.name());
					}
					if (handler!=null)
						handler.initializeAs(option, role, this, out);
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
					if (handler!=null)
						handler.initializeAs(option, role, this, out);
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
			logger.log(Level.WARNING, "Remote party requests {0} for option {1} and we reject it", command.getCode(), option.name());
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
		logger.log(Level.DEBUG, "Subnegotiation for {0}: {1}", code, Arrays.toString((values)));

		TelnetSubnegotiationHandler handler = TelnetOptionRegistry.get(code);
		if (handler==null) {
			logger.log(Level.WARNING, "Received subnegotiation for {0}/{1}, but cannot find a TelnetOptionHandler", code, TelnetOption.valueOf(code));
			return;
		}

		handler.handleSubnegotiation(code, values, this, out);
	}

	//-------------------------------------------------------------------
	public void initialize() throws IOException {
		getOutputStream();
		getInputStream();


		capExchangeWaitForOptions = new TimerTask() {
			public void run() {
				logger.log(Level.DEBUG, "End capability exchange");
				fireOptionPhaseDone();
			}
		};
		timer = new Timer(true);
		timer.schedule(capExchangeWaitForOptions, 500);
		exchangeStart = Instant.now();

		TelnetSocket.getExecutorService().submit( () -> {
			logger.log(Level.DEBUG, "Start capability exchange");
			try {
				out.write("Detecting capabilities\r\n".getBytes(StandardCharsets.US_ASCII));
				for (Entry<Integer, ControlCode> entry : negotiate.entrySet()) {
					lastStateSent.put(entry.getKey(), entry.getValue());
					capExchangeAwaitResponses.add( entry.getKey() );
					if (entry.getValue()==ControlCode.DO) {
						out.sendDo(entry.getKey());
					} else if (entry.getValue()==ControlCode.WILL)
						out.sendWill(entry.getKey());
					else if (entry.getValue()==ControlCode.WONT)
						out.sendWont(entry.getKey());
					else
						logger.log(Level.WARNING, "Ignore operation "+entry.getValue()+" for "+entry.getKey());
				}
			} catch (IOException e) {
				logger.log(Level.ERROR, "Error in Telnet capability exchange",e);
			}
		});

//		logger.log(Level.DEBUG, "Start capability exchange");
//		super.getOutputStream().write("Detecting capabilities\r\n".getBytes(StandardCharsets.US_ASCII));
//		for (Entry<Integer, ControlCode> entry : negotiate.entrySet()) {
//			lastStateSent.put(entry.getKey(), entry.getValue());
//			capExchangeAwaitResponses.add( entry.getKey() );
//			if (entry.getValue()==ControlCode.DO) {
//				out.sendDo(entry.getKey());
//			} else if (entry.getValue()==ControlCode.WILL)
//				out.sendWill(entry.getKey());
//			else if (entry.getValue()==ControlCode.WONT)
//				out.sendWont(entry.getKey());
//			else
//				logger.log(Level.WARNING, "Ignore operation "+entry.getValue()+" for "+entry.getKey());
//		}
	}

}
