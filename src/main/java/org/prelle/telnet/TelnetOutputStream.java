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
public class TelnetOutputStream extends OutputStream {

	private final static Logger logger = System.getLogger("telnet.lvl1.out");

	private OutputStream realOut;
	private boolean  binaryMode = true;

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetOutputStream(OutputStream out) {
		realOut = out;
	}

	//-----------------------------------------------------------------
	public void setBinaryMode(boolean enabled) {
		binaryMode = enabled;
	}

	//-----------------------------------------------------------------
	public boolean isInBinaryMode() {
		return binaryMode;
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
		logger.log(Level.TRACE,"write "+(new String(data)));
		// Scan how many byte 255 are there
		int count = 0;
		for (byte b : data) {
			if (b==-1) count++;
		}
//		if (data.length<10) {
//			try {
//				throw new RuntimeException("Trace");
//			} catch (Exception e) {
//				logger.log(Level.WARNING, "Did you mean to use writeCommand?",e);
//			}
//		}
		if (count>0) {
			logger.log(Level.WARNING, "TODO: Encode 0xff");
			byte[] corrected = new byte[data.length+count];
			int pos=0;
			for (byte b : data) {
				if (b==-1) {
					corrected[pos++]=(byte)0xff;
				}
				corrected[pos++]=b;
			}
			data = corrected;
		}
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	public void writeCommand(byte[] data) throws IOException {
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	public void sendDo(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.DO.code();
		data[2] = (byte)optionCode;
		logger.log(Level.DEBUG,"IAC DO {0}",optionCode);
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendWill(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.WILL.code();
		data[2] = (byte)optionCode;
		logger.log(Level.DEBUG,"IAC WILL {0}",optionCode);
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendDont(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.DONT.code();
		data[2] = (byte)optionCode;
		logger.log(Level.DEBUG,"IAC DONT {0}",optionCode);
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendWont(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.WONT.code();
		data[2] = (byte)optionCode;
		logger.log(Level.DEBUG,"IAC WONT {0}",optionCode);
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendSubNegotiation(int code, int command, byte[] value) {
		logger.log(Level.DEBUG,"sub-negotiation for {0}, command {1}, value={2}", code,command);
		byte[] data = new byte[5+value.length];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.SB.code();
		data[2] = (byte)code;
		System.arraycopy(value, 0, data, 3, value.length);
		data[data.length-2] = (byte)ControlCode.IAC.code();
		data[data.length-1] = (byte)ControlCode.SE.code();

	}

}
