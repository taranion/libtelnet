/**
 * 
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.prelle.telnet.NetworkVirtualConsole;

/**
 * @author prelle
 *
 */
public abstract class TelnetOption {

	protected final static Logger logger = Logger.getLogger("telnet.option");

	public abstract void setDefaults(NetworkVirtualConsole console);

	public abstract String getName();
	public abstract int getCode();

	public abstract void initialize(NetworkVirtualConsole console) throws IOException;

	public abstract void performSubNegotiation(NetworkVirtualConsole nvt, InputStream in) throws IOException;

	//-----------------------------------------------------------------
	public void requestUsage(NetworkVirtualConsole nvt) throws IOException {
		logger.debug("Suggest "+getName());
		if (nvt.isClientMode()) {
			nvt.getWillVariable(getName()).setState(true);
			nvt.sendWill(getCode());
		} else {
			nvt.getDoVariable(getName()).setState(true);
			nvt.sendDo(getCode());
		}
	}

	//-----------------------------------------------------------------
	public void requestStop(NetworkVirtualConsole nvt) throws IOException {
		logger.debug("Desire stopping "+getName());
		if (nvt.isClientMode()) {
			nvt.getWillVariable(getName()).setState(false);
			nvt.sendWont(getCode());
		} else {
			nvt.getDoVariable(getName()).setState(true);
			nvt.sendDont(getCode());
		}
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#requestNotUsage()
	 */
	public void processDo(NetworkVirtualConsole nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
		if (willState) {
			// This "DO" is a response
			if (doState) {
				// Already assume DO on remote site
				logger.debug("Already assume DO "+getName()+" on remote party");
			} else {
				nvt.getDoVariable(getName()).setState(true);
				logger.info("Enabled "+getName()+" (remote party accepted)");
			}
		} else if (!doState) {
			// This is a request
			if (willState) {
				// Already in "WILL" state
				logger.debug("Already in WILL "+getName()+" locally");
			} else {
				nvt.getWillVariable(getName()).setState(true);
				nvt.sendWill(getCode());
				logger.info("Enabled "+getName()+" (remote party requested)");
			}
		} else {
			logger.debug("Ignore DO when I sent a DO");
		}
	}

	//-----------------------------------------------------------------
	public void processDont(NetworkVirtualConsole nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
		if (willState) {
			// This "DONT" is a response
			nvt.getDoVariable(getName()).setState(false);
			nvt.getWillVariable(getName()).setState(false);
			logger.info("Remote party rejected "+getName());
		} else if (doState) {
			// This is a stop request
			if (!willState) {
				// Already in "WONT" state
				logger.debug("Already in WONT "+getName()+" locally");
			} else {
				nvt.getWillVariable(getName()).setState(false);
				nvt.sendWont(getCode());
				logger.info("Disabled "+getName()+" (remote party requested)");
			}
		} else {
			logger.debug("Ignore DONT when I sent a DONT");
		}
	}

	//-----------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetOption#processWill(org.prelle.telnet.NetworkVirtualConsole)
	 */
	public void processWill(NetworkVirtualConsole nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
//		logger.debug("doState="+doState+"   willState="+willState);
		if (doState) {
			// This "WILL" is a response
			if (willState) {
				// Already assume WILL on remote site
				logger.debug("Already assume WILL "+getName()+" on remote party");
			} else {
				nvt.getWillVariable(getName()).setState(true);
				logger.info("Enabled "+getName()+" (remote party accepted)");
				optionEnabled(nvt, true);
			}
		} else if (!willState) {
			// This is a request
			if (doState) {
				// Already in "DO" state
				logger.debug("Already in DO "+getName()+" locally");
			} else {
				nvt.getDoVariable(getName()).setState(true);
				nvt.sendDo(getCode());
				logger.info("Enabled "+getName()+" (remote party requested)");
				optionEnabled(nvt, false);
			}
		} else {
			logger.debug("Ignore WILL when I sent a WILL");
		}
	}

	//-----------------------------------------------------------------
	public void processWont(NetworkVirtualConsole nvt) throws IOException {
		boolean[] states = nvt.getDoWillStatesFor(this);
		boolean doState = states[0];
		boolean willState = states[1];
//		logger.debug("doState="+doState+"   willState="+willState);
		if (doState) {
			// This "WONT" is a response
			if (!willState) {
				// Already assume WILL on remote site
				logger.debug("Already assume WONT "+getName()+" on remote party");
			} else {
				nvt.getWillVariable(getName()).setState(false);
				logger.info("Disabled "+getName()+" (remote party rejected)");
			}
			optionDisabled(nvt, true);
		} else if (!willState) {
			// This is a stop request
			if (!doState) {
				// Already in "DO" state
				logger.debug("Already in DONT "+getName()+" locally");
			} else {
				nvt.getDoVariable(getName()).setState(false);
				nvt.sendDont(getCode());
				logger.info("Disabled "+getName()+" (remote party requested)");
				optionDisabled(nvt, false);
			}
		} else {
			logger.debug("Ignore WONT when I sent a WONT");
		}
	}

	//-----------------------------------------------------------------
	protected void optionEnabled(NetworkVirtualConsole nvt, boolean iAmInitiator) throws IOException {
		logger.info(getName()+" enabled");
	}

	//-----------------------------------------------------------------
	protected void optionDisabled(NetworkVirtualConsole nvt, boolean iAmInitiator) throws IOException {
		logger.info(getName()+" not supported");
	}
	
}
