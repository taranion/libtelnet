/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetOutputStream extends OutputStream {

	private final static Logger logger = Logger.getLogger("telnet.lvl1");

	private OutputStream realOut;
	
	//-----------------------------------------------------------------
	/**
	 */
	public TelnetOutputStream(OutputStream out) {
		realOut = out;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int data) throws IOException {
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] data) throws IOException {
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	public void sendDo(int optionCode) throws IOException {
		logger.debug("confirm "+optionCode);
		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(true);

		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.DO.code();
		data[2] = (byte)optionCode;
		realOut.write(data);
		realOut.flush();

		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(false);
	}

	//-----------------------------------------------------------------
	public void sendWill(int optionCode) throws IOException {
		logger.debug("offer "+optionCode);
		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(true);

		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.WILL.code();
		data[2] = (byte)optionCode;
		realOut.write(data);
		realOut.flush();

		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(false);
	}

	//-----------------------------------------------------------------
	public void sendDont(int optionCode) throws IOException {
		logger.debug("reject "+optionCode);
		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(true);
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.DONT.code();
		data[2] = (byte)optionCode;
		realOut.write(data);
		realOut.flush();
		
		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(false);
	}

	//-----------------------------------------------------------------
	public void sendWont(int optionCode) throws IOException {
		logger.debug("reject (Wont) "+optionCode);
		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(true);
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.WONT.code();
		data[2] = (byte)optionCode;
		realOut.write(data);
		realOut.flush();
		
		if (realOut instanceof TelnetDebuggingOutputStream)
			((TelnetDebuggingOutputStream)realOut).setInControlMode(false);
	}

}
