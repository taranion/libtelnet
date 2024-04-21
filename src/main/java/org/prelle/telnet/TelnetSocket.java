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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.telnet.option.TelnetEcho;
import org.prelle.telnet.option.TelnetOption;

/**
 * @author prelle
 *
 */
public class TelnetSocket extends Socket implements TelnetStreamListener {

	private final static Logger logger = System.getLogger("telnet.lvl3");

	private TelnetInputStream in;
	private TelnetOutputStream out;
	private boolean inClientMode;
	private Map<Integer,WillVariable> willVariables = new HashMap<Integer, WillVariable>();
	private Map<Integer,DoVariable>   doVariables   = new HashMap<Integer, DoVariable>();
	private Map<TelnetOption,Object> optionState = new HashMap<TelnetOption, Object>();
	private List<TelnetOptionListener> optionListener = new ArrayList<TelnetOptionListener>();

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetSocket() {
		inClientMode = false;
		
		loadVariableDefaults();
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
		// Load default values for all variables
		for (TelnetOption option : TelnetConfiguration.getKnownOptions()) {
			// Load defaults
			option.setDefaults(this);
		}
		logger.log(Level.DEBUG,"Do-Variables   = "+doVariables.values());
		logger.log(Level.DEBUG,"Will-Variables = "+willVariables.values());		
	}

	//-----------------------------------------------------------------
	/**
	 * @see java.net.Socket#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (out==null)
			out = new TelnetOutputStream(
					new TelnetDebuggingOutputStream(super.getOutputStream()));

		if (in==null)
			in = new TelnetInputStream(
					this,
					new TelnetDebuggingInputStream(super.getInputStream()));
		return in;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.net.Socket#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (in==null)
			in = new TelnetInputStream(
					this,
					new TelnetDebuggingInputStream(super.getInputStream()));

		if (out==null)
			out = new TelnetOutputStream(
					new TelnetDebuggingOutputStream(super.getOutputStream()));
		return out;
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
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedWILL(int)
	 */
	@Override
	public void receivedWILL(int optionCode) {
		TelnetOption option = TelnetConfiguration.getOption(optionCode);
		try {
			if (option==null) {
				logger.log(Level.WARNING,"remote party offers option unknown option "+optionCode);
				out.sendDont(optionCode);
			} else {
				logger.log(Level.INFO,"remote party offers "+option.getName());
//				WillVariable cfg = getWillVariable(optionCode);
//				if (cfg.getState())
					option.processWill(this);
//				else
//					option.processWont(this);
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
		TelnetOption option = TelnetConfiguration.getOption(optionCode);
		try {
			if (option==null) {
				logger.log(Level.WARNING,"remote party rejects option unknown option "+optionCode);
			} else {
				logger.log(Level.INFO,"remote party rejects "+option.getName());
				option.processWont(this);
			}
		} catch (IOException e) {
			logger.log(Level.ERROR,"Could not answer that WILL offer: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedDO(int)
	 */
	@Override
	public void receivedDO(int optionCode) {
		TelnetOption option = TelnetConfiguration.getOption(optionCode);
		try {
			if (option==null) {
				logger.log(Level.WARNING,"remote party performs option unknown option "+optionCode);
				out.sendWont(optionCode);
			} else {
				logger.log(Level.INFO,"remote party performs "+option.getName());
				option.processDo(this);
			}
		} catch (IOException e) {
			logger.log(Level.ERROR,"Could not answer that WILL offer: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedDONT(int)
	 */
	@Override
	public void receivedDONT(int optionCode) {
		TelnetOption option = TelnetConfiguration.getOption(optionCode);
		try {
			if (option==null) {
				logger.log(Level.WARNING,"remote party performs option unknown option "+optionCode);
				out.sendWont(optionCode);
			} else {
				logger.log(Level.INFO,"remote party performs "+option.getName());
				option.processDont(this);
			}
		} catch (IOException e) {
			logger.log(Level.ERROR,"Could not answer that WILL offer: "+e,e);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.prelle.telnet.TelnetStreamListener#receivedSubnegotiationBegin(int)
	 */
	@Override
	public void receivedSubnegotiationBegin(int optionCode) {
		TelnetOption option = TelnetConfiguration.getOption(optionCode);
		try {
			if (option==null) {
				logger.log(Level.WARNING,"remote party performs subnegitation for unknown option "+optionCode);
			} else {
				logger.log(Level.INFO,"subnegotiation startet for "+option.getName());
				in.setHigherLevelControl(true);
				option.performSubNegotiation(this, in);
				in.setHigherLevelControl(false);
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

	//-----------------------------------------------------------------
	public WillVariable getWillVariable(int code) {
		return willVariables.get(code);
	}

	//-----------------------------------------------------------------
	public DoVariable getDoVariable(int code) {
		return doVariables.get(code);
	}

	//--------------------------------------------------------------
	public boolean[] getDoWillStatesFor(TelnetOption opt) {
		boolean[] ret = new boolean[]{
				doVariables.get(opt.getCode()).getState(), 
				willVariables.get(opt.getCode()).getState()};
		logger.log(Level.DEBUG,"Do/Will states for "+opt.getName()+" are: "+Arrays.toString(ret));
		return ret;
	}

	//-----------------------------------------------------------------
	public void setOptionVariable(TelnetVariable variable) {
		if (variable instanceof WillVariable)
			willVariables.put(variable.getName(), (WillVariable) variable);
		else if (variable instanceof DoVariable)
			doVariables.put(variable.getName(), (DoVariable) variable);
		
	}

	//-----------------------------------------------------------------
	public void setOptionState(TelnetOption option, Object stateObject) {
		optionState.put(option, stateObject);
	}

	//-----------------------------------------------------------------
	public Object getOptionState(TelnetOption option) {
		return optionState.get(option);
	}

	//-----------------------------------------------------------------
	public void addOptionListener(TelnetOptionListener optList) {
		if (!optionListener.contains(optList))
			optionListener.add(optList);
	}

	//-----------------------------------------------------------------
	public void fireOptionDataChanged(TelnetOption option,Object data) {
		for (TelnetOptionListener list : optionListener)
			try {
				list.telnetOptionDataChanged(this, option, data);
			} catch (Exception e) {
				logger.log(Level.ERROR,"Error calling "+list.getClass()+".telnetOptionDataChanged: "+e,e);
			}
	}

	//-----------------------------------------------------------------
	public void requestEcho() throws IOException {
		TelnetConfiguration.getOption(TelnetEcho.CODE).requestUsage(this);
	}

	//-----------------------------------------------------------------
	public boolean isEchoEnabled() throws IOException {
		return doVariables.get(TelnetEcho.CODE).getState();
	}

	//-----------------------------------------------------------------
	public void stopEcho() throws IOException {
		TelnetConfiguration.getOption(TelnetEcho.CODE).requestStop(this);
	}

}
