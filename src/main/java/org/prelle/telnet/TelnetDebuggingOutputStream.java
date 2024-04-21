/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetDebuggingOutputStream extends OutputStream {

	private final static Logger logger = System.getLogger("telnet.lvl1");

	private OutputStream realOut;
	private boolean inControlMode = false;
	
	//-----------------------------------------------------------------
	/**
	 */
	public TelnetDebuggingOutputStream(OutputStream out) {
		realOut = out;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int data) throws IOException {
		ControlCode code = ControlCode.getCodeFor(data);
		if (code!=null)
			logger.log(Level.DEBUG,String.format("SND %s", code.toString()));
		else
			logger.log(Level.TRACE,String.format("SND %d (%s)", data, (char)data));
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] data) throws IOException {
		if (logger.isLoggable(Level.DEBUG) && inControlMode) {
			StringBuffer buf = new StringBuffer();
			for (byte dat : data) {
				int tmp = dat;
				if (tmp<0) tmp = 256+dat;
				if (tmp>=240)
					buf.append(ControlCode.getCodeFor(tmp)+" ");
				else
					buf.append( tmp );
			}
			logger.log(Level.DEBUG,"SND "+buf);
		}
		realOut.write(data);
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
