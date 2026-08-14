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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.event.internal.DataEventImpl;
import org.prelle.telnet.option.CommunicationRole;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.parser.TelnetConstants;
import org.prelle.telnet.protocol.TelnetProtocol;
import org.prelle.telnet.protocol.TelnetProtocolListener;

/**
 * @author prelle
 *
 */
public class TelnetSocket extends Socket implements TelnetConstants, TelnetProtocolListener {
	

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
	private InetAddress remoteAddress;
	
	private TelnetSocketListener listener;

	//-----------------------------------------------------------------
	public static ExecutorService getExecutorService() {
		return executor;
	}

	//-----------------------------------------------------------------
	public TelnetSocket() {
		stack = TelnetProtocol.builder(CommunicationRole.SERVER)
				.withListener(this)
				.build();
	}

	//-----------------------------------------------------------------
	public TelnetSocket(InputStream wrapIn, OutputStream wrapOut, InetAddress src) {
		this.remoteAddress = src;
		stack = TelnetProtocol.builder(CommunicationRole.SERVER)
				.withListener(this)
				.build();
		out = new TelnetOutputStream(wrapOut, stack);
		in  = new TelnetInputStream( wrapIn, stack);
		stack.setReturnChannel(out);
//		stack.setInputStream(in);
//		
//		in.setReverseStream(out);
		
		// Start sending request for all supported options
		stack.initializeExtensions();
//		try {
//			this.setSoTimeout(500);
//		} catch (SocketException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	//-----------------------------------------------------------------
	public TelnetSocket(String host, int port) throws UnknownHostException, IOException {
		super(host, port);
		stack = TelnetProtocol.builder(CommunicationRole.CLIENT)
				.withListener(this)
				.build();
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
		
		in().startReadingFromSocket();
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see java.net.Socket#getInetAddress()
	 */
	@Override
    public InetAddress getInetAddress() {
        return remoteAddress != null ? remoteAddress : super.getInetAddress();
    }

	//-----------------------------------------------------------------
	public TelnetSocket addListener(TelnetSocketListener listener) {
		Objects.requireNonNull(listener, "listener must not be null");
		this.listener = listener;
		return this;
	}

	//-------------------------------------------------------------------
	public List<Integer> negotiateOptionsAsync(TelnetOption...options) {
		logger.log(Level.INFO, "ENTER: negotiateOptionsAsync");
		try {
			//stack.getOutputStream();
			List<Integer> result = new ArrayList<>();
			for (TelnetOption option : options) {
				stack.add(option);
				result.add(option.getOptionCode());
			}
			stack.initializeExtensions();
			return result;
		} finally {
			logger.log(Level.INFO, "LEAVE: negotiateOptionsAsync");
		}
	}

//	//-----------------------------------------------------------------
//	public void waitUntilSubnegotiationDone() {
//		stack.waitUntilSubnegotiationDone(3000);
//	}
	
	//-----------------------------------------------------------------
	/**
	 * @see java.net.Socket#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
//		if (in==null) {
//			in = new TelnetInputStream( super.getInputStream(), stack);
//			stack.setInputStream(in);
//		}
		return in;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.net.Socket#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
//		if (out==null) {
//			out = new TelnetOutputStream(super.getOutputStream(), stack);
//			in().setReverseStream(out);
//			stack.setOutputStream(out);
//		}
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

	//-------------------------------------------------------------------
	public boolean isFeatureActive(Integer code) {
		return stack.isFeatureActive(code);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.protocol.TelnetProtocolListener#onTelnetEvent(org.prelle.telnet.event.TelnetEvent)
	 */
	@Override
	public void onTelnetEvent(TelnetEvent event) {
		// Instead of delivering data events to a listener, we directly feed them into the input stream, so that the application can read them from there.
		if (event instanceof DataEventImpl dataEvent) {
			try {
				in().receiveData(dataEvent.getData());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (listener!=null) {
			listener.onTelnetEvent(event);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.protocol.TelnetProtocolListener#optionStateChanged(org.prelle.telnet.option.TelnetOption, boolean)
	 */
	@Override
	public void optionStateChanged(TelnetOption extension, boolean active) {
		if (listener!=null) {
			listener.optionStateChanged(extension, active);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.protocol.TelnetProtocolListener#telnetReady()
	 */
	@Override
	public void telnetReady() {
		if (listener!=null) {
			listener.telnetReady();
		}
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
