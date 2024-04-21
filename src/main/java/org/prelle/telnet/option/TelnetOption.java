/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * @author prelle
 *
 */
public abstract class TelnetOption implements TelnetConstants {

	protected final static Logger logger = System.getLogger("telnet.option");

	public abstract void setDefaults(TelnetSocket nvt);

	public abstract String getName();
	public abstract int getCode();

	public abstract void initialize(TelnetSocket console) throws IOException;

	//-----------------------------------------------------------------
	public void requestUsage(TelnetSocket nvt) throws IOException {
		logger.log(Level.DEBUG,"Suggest "+getName()+" , clientmode="+nvt.isInClientMode());
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (nvt.isInClientMode()) {
			nvt.getWillVariable(getCode()).setState(true);
			out.sendWill(getCode());
		} else {
			nvt.getDoVariable(getCode()).setState(true);
			out.sendDo(getCode());
		}
	}

	//-----------------------------------------------------------------
	public void requestStop(TelnetSocket nvt) throws IOException {
		logger.log(Level.DEBUG,"Desire stopping "+getName());
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (nvt.isInClientMode()) {
			nvt.getWillVariable(getCode()).setState(false);
			out.sendWont(getCode());
		} else {
			nvt.getDoVariable(getCode()).setState(true);
			out.sendDont(getCode());
		}
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#requestNotUsage()
	 */
	public void processDo(TelnetSocket nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (willState) {
			// This "DO" is a response
			if (doState) {
				// Already assume DO on remote site
				logger.log(Level.DEBUG,"Already assume DO "+getName()+" on remote party");
			} else {
				nvt.getDoVariable(getCode()).setState(true);
				logger.log(Level.INFO,"Enabled "+getName()+" (remote party accepted)");
			}
		} else if (!doState) {
			// This is a request
			if (willState) {
				// Already in "WILL" state
				logger.log(Level.DEBUG,"Already in WILL "+getName()+" locally");
			} else {
				nvt.getWillVariable(getCode()).setState(true);
				out.sendWill(getCode());
				logger.log(Level.INFO,"Enabled "+getName()+" (remote party requested)");
			}
		} else {
			logger.log(Level.DEBUG,"Ignore DO when I sent a DO");
		}
	}

	//-----------------------------------------------------------------
	public void processDont(TelnetSocket nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (willState) {
			// This "DONT" is a response
			nvt.getDoVariable(getCode()).setState(false);
			nvt.getWillVariable(getCode()).setState(false);
			logger.log(Level.INFO,"Remote party rejected "+getName());
		} else if (doState) {
			// This is a stop request
			if (!willState) {
				// Already in "WONT" state
				logger.log(Level.DEBUG,"Already in WONT "+getName()+" locally");
			} else {
				nvt.getWillVariable(getCode()).setState(false);
				out.sendWont(getCode());
				logger.log(Level.INFO,"Disabled "+getName()+" (remote party requested)");
			}
		} else {
			logger.log(Level.DEBUG,"Ignore DONT when I sent a DONT");
		}
	}

	//-----------------------------------------------------------------
	public void processWill(TelnetSocket nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
//		logger.log(Level.DEBUG,"doState="+doState+"   willState="+willState);
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (doState) {
			// This "WILL" is a response
			if (willState) {
				// Already assume WILL on remote site
				logger.log(Level.DEBUG,"Already assume WILL "+getName()+" on remote party");
			} else {
				nvt.getWillVariable(getCode()).setState(true);
				logger.log(Level.DEBUG,"Enabled "+getName()+" (remote party accepted)");
				optionEnabled(nvt, true);
			}
		} else if (!willState) {
			// This is a request
			if (doState) {
				// Already in "DO" state
				logger.log(Level.DEBUG,"Already in DO "+getName()+" locally");
			} else {
				nvt.getDoVariable(getCode()).setState(true);
				out.sendDo(getCode());
				logger.log(Level.INFO,"Enabled "+getName()+" (remote party requested)");
				optionEnabled(nvt, false);
			}
		} else {
			logger.log(Level.DEBUG,"Ignore WILL when I sent a WILL");
		}
	}

	//-----------------------------------------------------------------
	public void processWont(TelnetSocket nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
//		logger.log(Level.DEBUG,"doState="+doState+"   willState="+willState);
		if (doState) {
			// This "WONT" is a response
			if (!willState) {
				// Already assume WILL on remote site
				logger.log(Level.DEBUG,"Already assume WONT "+getName()+" on remote party");
			} else {
				nvt.getWillVariable(getCode()).setState(false);
				logger.log(Level.INFO,"Disabled "+getName()+" (remote party rejected)");
			}
			optionDisabled(nvt, true);
		} else if (!willState) {
			// This is a stop request
			if (!doState) {
				// Already in "DO" state
				logger.log(Level.DEBUG,"Already in DONT "+getName()+" locally");
			} else {
				nvt.getDoVariable(getCode()).setState(false);
				out.sendDont(getCode());
				logger.log(Level.INFO,"Disabled "+getName()+" (remote party requested)");
				optionDisabled(nvt, false);
			}
		} else {
			logger.log(Level.DEBUG,"Ignore WONT when I sent a WONT");
		}
	}

	//-----------------------------------------------------------------
	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
		logger.log(Level.INFO,getName()+" enabled");
	}

	//-----------------------------------------------------------------
	protected void optionDisabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
		logger.log(Level.INFO,getName()+" not supported");
	}

//	//-----------------------------------------------------------------
//	public boolean isEnabledByDefault() {
//		return false;
//	}

	//-----------------------------------------------------------------
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
		
	}
	
	//-----------------------------------------------------------------
	protected static void startSubNegotiation(TelnetSocket sock, int code) throws IOException {
		OutputStream out = sock.getOutputStream();
		out.write(IAC);
		out.write(SB);
		out.write(code);		
	}
	
	//-----------------------------------------------------------------
	protected static void endSubNegotiation(TelnetSocket sock, int code) throws IOException {
		OutputStream out = sock.getOutputStream();
		out.write(IAC);
		out.write(SE);
		out.write(code);		
	}

	
}
