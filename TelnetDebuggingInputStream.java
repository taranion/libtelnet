/**
 *
 */
package org.prelle.telnet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * @author prelle
 *
 */
public class TelnetDebuggingInputStream extends FilterInputStream implements TelnetConstants {

	private final static Logger logger = System.getLogger("telnet.lvl1");

	private boolean inControlMode = false;

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetDebuggingInputStream(InputStream in) {
		super(in);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int data = super.read();

		ControlCode code = ControlCode.getCodeFor(data);
		if (code!=null)
			logger.log(Level.DEBUG,String.format("RCV %s", code.toString()));
		else  {
			if (inControlMode) {
				logger.log(Level.DEBUG,String.format("RCV %d", data));
			} else
				logger.log(Level.TRACE,String.format("RCV %d (%s)", data, (char)data));
		}
		return data;
	}


	//-----------------------------------------------------------------
	/**
	 * @return the inControlMode
	 */
	public boolean isInControlMode() {
		return inControlMode;
	}

	//-----------------------------------------------------------------
	/**
	 * @param inControlMode the inControlMode to set
	 */
	public void setInContdrolMode(boolean inControlMode) {
		this.inControlMode = inControlMode;
	}

}
