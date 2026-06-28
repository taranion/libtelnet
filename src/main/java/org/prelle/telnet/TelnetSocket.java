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
import java.util.Objects;
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
	
	public static record NegotiationResult(TelnetSubnegotiationHandler handler, boolean accepted, Object data) {
		public <E> E getResultData() {
			return (E) data;
		}
		public String toString() {
			return handler.getName()+" \t: "+(accepted?"ACTIVE":"REJECTED");
		}
	}

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
	private TelnetProtocol stack;

	//-----------------------------------------------------------------
	public static ExecutorService getExecutorService() {
		return executor;
	}

	//-----------------------------------------------------------------
	public TelnetSocket() {
		stack = new TelnetProtocol(CommunicationRole.SERVER);
//		optionCaps.capabilities.put(WellKnownTelnetOptions.ECHO    , new TelnetConfigOption());
//		optionCaps.capabilities.put(WellKnownTelnetOptions.EOR     , new TelnetConfigOption());
//		optionCaps.capabilities.put(WellKnownTelnetOptions.LINEMODE, new TelnetConfigOption());
	}

	//-----------------------------------------------------------------
	public TelnetSocket(String host, int port) throws UnknownHostException, IOException {
		super(host, port);
		stack = new TelnetProtocol(CommunicationRole.CLIENT);
//		negotiate.put(0, ControlCode.DO);
//		active.add(0);
		getOutputStream();
		getInputStream();
//		out().logger = System.getLogger("telnet.lvl1.out."+host);
//		in().logger = System.getLogger("telnet.lvl1.in."+host);
////		initialize();
//		optionCaps.capabilities.put(WellKnownTelnetOptions.ECHO    , new TelnetConfigOption());
//		optionCaps.capabilities.put(WellKnownTelnetOptions.EOR     , new TelnetConfigOption());
//		optionCaps.capabilities.put(WellKnownTelnetOptions.LINEMODE, new TelnetConfigOption());
	}

	//-----------------------------------------------------------------
	public TelnetSocket addListener(TelnetListener listener) {
		stack.addListener(listener);
		return this;
	}

	//-----------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public TelnetSocket setOptionListener(WellKnownTelnetOptions option, TelnetOptionListener listener) {
		if (stack.getExtensionForOption(option.getCode())==null) {
			throw new IllegalArgumentException("No extension registered for option "+option);
		}
		stack.getExtensionForOption(option.getCode()).addListener(listener);
		return this;
	}

	//-------------------------------------------------------------------
	public void negotiateOptionsAsync(TelnetSubnegotiationHandler...options) {
		stack.getOutputStream();
		for (TelnetSubnegotiationHandler option : options) {
			stack.add(option);
		}
		stack.initializeExtensions();
	}

	//-------------------------------------------------------------------
	public List<NegotiationResult> negotiateOptionsAndWait(TelnetSubnegotiationHandler...options) {
		List<NegotiationResult> result = new ArrayList<>();
		synchronized (this) {
			negotiateOptionsAsync(options);
		}
		return result;
	}

	//-----------------------------------------------------------------
	/**
	 * @see java.net.Socket#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (in==null) {
			in = new TelnetInputStream( super.getInputStream(), stack);
			stack.setInputStream(in);
		}
		return in;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.net.Socket#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (out==null) {
			out = new TelnetOutputStream(super.getOutputStream(), stack);
			in().setReverseStream(out);
			stack.setOutputStream(out);
		}
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
	/**
	 * @return the stack
	 */
	public TelnetProtocol getStack() {
		return stack;
	}


//	//-------------------------------------------------------------------
//	public boolean isFeatureActive(int code) {
//		return active.contains(code);
//	}
//
//	//-------------------------------------------------------------------
//	public boolean isFeatureSupported(WellKnownTelnetOptions option) {
//		return (getConfigOption(option)!=null && getConfigOption(option).isConfigurable());
//	}

}
