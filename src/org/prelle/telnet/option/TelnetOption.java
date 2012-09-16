/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 * @author prelle
 *
 */
public abstract class TelnetOption implements TelnetConstants {

	protected final static Logger logger = Logger.getLogger("telnet.option");

	public abstract void setDefaults(TelnetSocket nvt);

	public abstract String getName();
	public abstract int getCode();

	public abstract void initialize(TelnetSocket console) throws IOException;

	//-----------------------------------------------------------------
	public void requestUsage(TelnetSocket nvt) throws IOException {
		logger.debug("Suggest "+getName()+" , clientmode="+nvt.isInClientMode());
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
		logger.debug("Desire stopping "+getName());
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
				logger.debug("Already assume DO "+getName()+" on remote party");
			} else {
				nvt.getDoVariable(getCode()).setState(true);
				logger.info("Enabled "+getName()+" (remote party accepted)");
			}
		} else if (!doState) {
			// This is a request
			if (willState) {
				// Already in "WILL" state
				logger.debug("Already in WILL "+getName()+" locally");
			} else {
				nvt.getWillVariable(getCode()).setState(true);
				out.sendWill(getCode());
				logger.info("Enabled "+getName()+" (remote party requested)");
			}
		} else {
			logger.debug("Ignore DO when I sent a DO");
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
			logger.info("Remote party rejected "+getName());
		} else if (doState) {
			// This is a stop request
			if (!willState) {
				// Already in "WONT" state
				logger.debug("Already in WONT "+getName()+" locally");
			} else {
				nvt.getWillVariable(getCode()).setState(false);
				out.sendWont(getCode());
				logger.info("Disabled "+getName()+" (remote party requested)");
			}
		} else {
			logger.debug("Ignore DONT when I sent a DONT");
		}
	}

	//-----------------------------------------------------------------
	public void processWill(TelnetSocket nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
//		logger.debug("doState="+doState+"   willState="+willState);
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (doState) {
			// This "WILL" is a response
			if (willState) {
				// Already assume WILL on remote site
				logger.debug("Already assume WILL "+getName()+" on remote party");
			} else {
				nvt.getWillVariable(getCode()).setState(true);
				logger.info("Enabled "+getName()+" (remote party accepted)");
				optionEnabled(nvt, true);
			}
		} else if (!willState) {
			// This is a request
			if (doState) {
				// Already in "DO" state
				logger.debug("Already in DO "+getName()+" locally");
			} else {
				nvt.getDoVariable(getCode()).setState(true);
				out.sendDo(getCode());
				logger.info("Enabled "+getName()+" (remote party requested)");
				optionEnabled(nvt, false);
			}
		} else {
			logger.debug("Ignore WILL when I sent a WILL");
		}
	}

	//-----------------------------------------------------------------
	public void processWont(TelnetSocket nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
//		logger.debug("doState="+doState+"   willState="+willState);
		if (doState) {
			// This "WONT" is a response
			if (!willState) {
				// Already assume WILL on remote site
				logger.debug("Already assume WONT "+getName()+" on remote party");
			} else {
				nvt.getWillVariable(getCode()).setState(false);
				logger.info("Disabled "+getName()+" (remote party rejected)");
			}
			optionDisabled(nvt, true);
		} else if (!willState) {
			// This is a stop request
			if (!doState) {
				// Already in "DO" state
				logger.debug("Already in DONT "+getName()+" locally");
			} else {
				nvt.getDoVariable(getCode()).setState(false);
				out.sendDont(getCode());
				logger.info("Disabled "+getName()+" (remote party requested)");
				optionDisabled(nvt, false);
			}
		} else {
			logger.debug("Ignore WONT when I sent a WONT");
		}
	}

	//-----------------------------------------------------------------
	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
		logger.info(getName()+" enabled");
	}

	//-----------------------------------------------------------------
	protected void optionDisabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
		logger.info(getName()+" not supported");
	}

//	//-----------------------------------------------------------------
//	public boolean isEnabledByDefault() {
//		return false;
//	}

	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
		
	}
	
}
