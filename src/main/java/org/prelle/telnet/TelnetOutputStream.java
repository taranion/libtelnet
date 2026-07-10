/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetOutputStream extends OutputStream {

	Logger logger = System.getLogger("telnet.lvl1.out");

	private OutputStream realOut;
	private boolean  binaryMode = true;
	private boolean injectCRBeforeLF = true;
	private TelnetProtocol stack;

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetOutputStream(OutputStream out, TelnetProtocol stack) {
		Objects.requireNonNull(out, "OutputStream cannot be null");
		this.stack = stack;
		stack.setOutputStream(this);
		realOut = out;
	}

	//-----------------------------------------------------------------
	public OutputStream getWrappedOutputStream() {
		return realOut;
	}
	
    //-------------------------------------------------------------------
    /**
     * @throws IOException
     */
//    @Override
//    public void flush() throws IOException {
//    	sendIAC(ControlCode.EOR.code());
//    }

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
		boolean lastWasCR = false;
		for (byte b : data) {
			if (b==-1) count++;
			if (injectCRBeforeLF && b=='\n' && !lastWasCR) count++;
			lastWasCR = b=='\r';
		}
		if (count>0) {
//			logger.log(Level.WARNING, "TODO: Encode 0xff");
			byte[] corrected = new byte[data.length+count];
			int pos=0;
			lastWasCR = false;
			for (byte b : data) {
				if (b==-1) {
					corrected[pos++]=(byte)0xff;
				} else if (b=='\n' && !lastWasCR) {
					corrected[pos++]=(byte)'\r';
				}
				corrected[pos++]=b;
				lastWasCR = b=='\r';
			}
			data = corrected;
		}
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	public void writeCommand(byte[] data) throws IOException {
		if (data.length>2 && data[0]==(byte)ControlCode.IAC.code()) {
			StringBuilder buf = new StringBuilder();
			// Replace byte values with their names
			buf.append(ControlCode.getCodeFor(data[0]&0xff)+" ");
			buf.append(ControlCode.getCodeFor(data[1]&0xff)+" ");
			TelnetOption handler = stack.getExtensionForOption(data[2]);
			if (handler==null) {
				buf.append(ControlCode.getCodeFor(data[2]&0xff));
			} else {
				buf.append(handler.getName());
			}
			buf.append(' ');
			// Subnegotiation content
			for (int i=3; i<(data.length-2); i++) {
				byte b = data[i];
				if (b>=0 && b<32) {
					if (handler!=null) {
						buf.append(handler.resolveSubCommandName(i,b));
					} else {
						buf.append(b);
					}
				} else {
					buf.append(((char)b));
				}
				buf.append(' ');
			}
			buf.append(ControlCode.getCodeFor(data[data.length-2]&0xff)+" ");
			buf.append(ControlCode.getCodeFor(data[data.length-1]&0xff));
			
			logger.log(Level.INFO,"send: "+buf);
		} else {
			logger.log(Level.INFO,"send: {0}", Arrays.toString(data));
		}
		
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	public void sendDo(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.DO.code();
		data[2] = (byte)optionCode;
		logger.log(Level.INFO,"send: IAC DO {0}={1}",optionCode, WellKnownTelnetOptions.valueOf(optionCode));
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendWill(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.WILL.code();
		data[2] = (byte)optionCode;
		logger.log(Level.ERROR,"send: IAC WILL {0}={1}",optionCode, WellKnownTelnetOptions.valueOf(optionCode));
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendDont(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.DONT.code();
		data[2] = (byte)optionCode;
		logger.log(Level.INFO,"send: IAC DONT {0}={1}",optionCode, WellKnownTelnetOptions.valueOf(optionCode));
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendWont(int optionCode) throws IOException {
		byte[] data = new byte[3];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.WONT.code();
		data[2] = (byte)optionCode;
		logger.log(Level.INFO,"send: IAC WONT {0}={1}",optionCode, WellKnownTelnetOptions.valueOf(optionCode));
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendIAC(int optionCode) throws IOException {
		byte[] data = new byte[2];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)optionCode;
//		logger.log(Level.INFO,"send: IAC {0}={1}",optionCode, ControlCode.getCodeFor(optionCode));
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendSubNegotiation(int code, int command, byte[] value) throws IOException {
//		logger.log(Level.DEBUG,"sub-negotiation for {0}, command {1}, value={2}", WellKnownTelnetOptions.valueOf(code),command, Arrays.toString(value));
		byte[] data = new byte[6+value.length];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.SB.code();
		data[2] = (byte)code;
		data[3] = (byte)command;
		System.arraycopy(value, 0, data, 4, value.length);
		data[data.length-2] = (byte)ControlCode.IAC.code();
		data[data.length-1] = (byte)ControlCode.SE.code();
//		logger.log(Level.WARNING,"sub-negotiation for {0}, value={1}", WellKnownTelnetOptions.valueOf(code),Arrays.toString(data));
		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendSubNegotiation(int code, byte[] value) throws IOException {
		logger.log(Level.INFO,"SND sub-negotiation for {0}, value={1}", code,Arrays.toString(value));
		byte[] data = new byte[5+value.length];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.SB.code();
		data[2] = (byte)code;
		System.arraycopy(value, 0, data, 3, value.length);
		data[data.length-2] = (byte)ControlCode.IAC.code();
		data[data.length-1] = (byte)ControlCode.SE.code();
		logger.log(Level.INFO,"SND --> {0}", Arrays.toString(data));

		realOut.write(data);
		realOut.flush();
	}

	//-----------------------------------------------------------------
	public void sendSubNegotiation(int code, String line) throws IOException {
		logger.log(Level.INFO,"SND sub-negotiation for {0}: {1}", code, line);
		byte[] value = line.getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[5+value.length];
		data[0] = (byte)ControlCode.IAC.code();
		data[1] = (byte)ControlCode.SB.code();
		data[2] = (byte)code;
		System.arraycopy(value, 0, data, 3, value.length);
		data[data.length-2] = (byte)ControlCode.IAC.code();
		data[data.length-1] = (byte)ControlCode.SE.code();
		realOut.write(data);
		realOut.flush();
	}

	//-------------------------------------------------------------------
	/**
	 * @param injectCRBeforeLF the injectCRBeforeLF to set
	 */
	public void setInjectCRBeforeLF(boolean injectCRBeforeLF) {
		this.injectCRBeforeLF = injectCRBeforeLF;
	}

}
