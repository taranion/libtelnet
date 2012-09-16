/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetInputStream extends InputStream {

	private final static Logger logger = Logger.getLogger("telnet.lvl2");

	private InputStream in;
	private TelnetStreamListener listener;
	private boolean higherLevelControl = false;

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetInputStream(TelnetStreamListener list, InputStream in) {
		this.listener = list;
		this.in = in;
	}

	//-----------------------------------------------------------------
	private ControlCode readNextCode() throws IOException {
		int data = in.read();
		ControlCode code = ControlCode.getCodeFor(data);
		if (code==null)
			throw new IOException("Expected control code but found "+data);
		return code;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		if (higherLevelControl) {
			if (in instanceof TelnetDebuggingInputStream)
				((TelnetDebuggingInputStream)in).setInControlMode(true);
			int ret = in.read();
			if (in instanceof TelnetDebuggingInputStream)
				((TelnetDebuggingInputStream)in).setInControlMode(false);
			return ret;
		}
		
		// Loop until next data is received
		do {
			if (in instanceof TelnetDebuggingInputStream)
				((TelnetDebuggingInputStream)in).setInControlMode(false);
			int data = in.read();

			if (data<240)
				return data;
			
			// Found a control code
			ControlCode code = ControlCode.getCodeFor(data);
			if (in instanceof TelnetDebuggingInputStream)
				((TelnetDebuggingInputStream)in).setInControlMode(true);

			switch (code) {
			case IAC:
				processIAC();
				continue;
			default:
				logger.warn("Don't know what to do with code "+code);
			}

		} while (true);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long amount) throws IOException {
		return in.skip(amount);
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return in.available();
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}

	//-----------------------------------------------------------------
	private void processIAC() throws IOException {
		//		mode = Mode.RCV_IAC;
		ControlCode code = readNextCode();

		logger.debug("IAC "+code);
		int next = -1;
		switch (code) {
		case IP:
			logger.debug("Interrupt Process Requested");
			listener.receivedInterruptProcess();
			break;
		case GA:
			listener.receivedGoAheadSignal();
			break;
		case WILL:
			next = in.read();
			listener.receivedWILL(next);
			break;
		case WONT:
			next = in.read();
			listener.receivedWONT(next);
			break;
		case DO:
			next = in.read();
			listener.receivedDO(next);
			break;
		case DONT:
			next = in.read();
			listener.receivedDONT(next);
			break;
		case SB:
			next = in.read();
			listener.receivedSubnegotiationBegin(next);
			break;
		default:
			logger.warn("Received unprocessed "+code);
		}
	}

//	//-----------------------------------------------------------------
//	public void readUntilSE() throws IOException {
//		logger.debug("Read until IAC SE");
//		while (true) {
//			int data1 = in.read();
//			if (data1==-1)
//				return;
//			if (data1!=TelnetConstants.ControlCode.IAC.code())
//				continue;
//
//			int data2 = in.read();
//			if (data2==-1)
//				return;
//			if (data2==TelnetConstants.ControlCode.SE.code()) {
//				return;
//			}
//			logger.warn("Expected IAC SE but found IAC "+data2);
//		}
//	}

	//-----------------------------------------------------------------
	/**
	 * Switch the stream into a dumb mode, letting the higher level
	 * interpret the bytes
	 * @param b
	 */
	public void setHigherLevelControl(boolean higherLevel) {
		higherLevelControl = higherLevel;
	}

}
