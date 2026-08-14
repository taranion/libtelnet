/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;

import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.parser.TelnetEncoder;
import org.prelle.telnet.protocol.TelnetProtocol;
import org.prelle.telnet.protocol.TelnetReturnChannel;

/**
 * @author prelle
 *
 */
public class TelnetOutputStream extends OutputStream implements TelnetReturnChannel {

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
		stack.setReturnChannel(this);
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
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSink#write(int)
	 */
	@Override
	public void write(int data) throws IOException {
		logger.log(Level.INFO,"write "+data);
		realOut.write(data);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSink#write(byte[])
	 */
	@Override
	public void write(byte[] data) throws IOException {
//		logger.log(Level.INFO,"write "+(new String(data)));
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

//	//-----------------------------------------------------------------
//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetSink#writeCommand(byte[])
//	 */
//	@Override
//	public void writeCommand(byte[] data) throws IOException {
//		if (data.length>2 && data[0]==(byte)ControlCode.IAC.code()) {
//			StringBuilder buf = new StringBuilder();
//			// Replace byte values with their names
//			buf.append(ControlCode.getCodeFor(data[0]&0xff)+" ");
//			buf.append(ControlCode.getCodeFor(data[1]&0xff)+" ");
//			TelnetOption handler = stack.getExtensionForOption(data[2]);
//			if (handler==null) {
//				buf.append(ControlCode.getCodeFor(data[2]&0xff));
//			} else {
//				buf.append(handler.getName());
//			}
//			buf.append(' ');
//			// Subnegotiation content
//			for (int i=3; i<(data.length-2); i++) {
//				byte b = data[i];
//				if (b>=0 && b<32) {
//					if (handler!=null) {
//						buf.append(handler.resolveSubCommandName(i,b));
//					} else {
//						buf.append(b);
//					}
//				} else {
//					buf.append(((char)b));
//				}
//				buf.append(' ');
//			}
//			buf.append(ControlCode.getCodeFor(data[data.length-2]&0xff)+" ");
//			buf.append(ControlCode.getCodeFor(data[data.length-1]&0xff));
//			
//			logger.log(Level.INFO,"send: "+buf);
//		} else {
//			logger.log(Level.INFO,"send: {0}", Arrays.toString(data));
//		}
//		
//		realOut.write(data);
//	}

	//-------------------------------------------------------------------
	/**
	 * @param injectCRBeforeLF the injectCRBeforeLF to set
	 */
	public void setInjectCRBeforeLF(boolean injectCRBeforeLF) {
		this.injectCRBeforeLF = injectCRBeforeLF;
	}

	@Override
	public void sendToRemote(TelnetEvent event) throws IOException {
		realOut.write(TelnetEncoder.encodeEvent(event));
		realOut.flush();
	}

}
