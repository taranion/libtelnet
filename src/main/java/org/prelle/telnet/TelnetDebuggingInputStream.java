/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * @author prelle
 *
 */
public class TelnetDebuggingInputStream extends InputStream implements TelnetConstants {

	private final static Logger logger = Logger.getLogger("telnet.lvl1");
	
	private InputStream real;
	private boolean inControlMode = false;
	
	//-----------------------------------------------------------------
	/**
	 */
	public TelnetDebuggingInputStream(InputStream in) {
		real = in;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int data = real.read();
		
		ControlCode code = ControlCode.getCodeFor(data);
		if (code!=null)
			logger.debug(String.format("RCV %s", code.toString()));
		else  {
			if (inControlMode) {
				logger.debug(String.format("RCV %d", data));
			} else
				logger.trace(String.format("RCV %d (%s)", data, (char)data));
		}
		return data;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read(byte[] b) throws IOException {
		logger.debug("read(byte[])");
		return real.read(b);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long amount) throws IOException {
		return real.skip(amount);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return real.available();
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		real.close();
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
	public void setInControlMode(boolean inControlMode) {
		this.inControlMode = inControlMode;
	}

}
