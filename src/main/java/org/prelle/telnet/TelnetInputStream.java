/**
 * 
 */
package org.prelle.telnet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetInputStream extends FilterInputStream {

	private final static Logger logger = System.getLogger("telnet.lvl2");

	private TelnetStreamListener listener;
	private boolean higherLevelControl = false;

	/** If stickyCRLF is true, then we're a machine, like an IBM PC,
    where a Newline is a CR followed by LF.  On UNIX, this is false
    because Newline is represented with just a LF character. */
	boolean         stickyCRLF = false;
	boolean         seenCR = false;

	public boolean  binaryMode = false;

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetInputStream(TelnetStreamListener list, InputStream in) {
		super(in);
		this.listener = list;
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
			logger.log(Level.TRACE,"Read in higher level control "+ret);
			return ret;
		}
		
		// Loop until next data is received
		do {
			if (in instanceof TelnetDebuggingInputStream)
				((TelnetDebuggingInputStream)in).setInControlMode(false);
			int data = in.read();
			logger.log(Level.TRACE, "RCV {0} ({1})", data, (char)data);

			if (data<240) {

		        /* If last time we determined we saw a CRLF pair, and we're
		           not turning that into just a Newline (that is, we're
		           stickyCRLF), then return the LF part of that sticky
		           pair now. */

		        if (seenCR) {
		            seenCR = false;
		            return '\n';
		        }

		        if (data== '\r') {    /* CR */
		        	data = in.read();
					logger.log(Level.TRACE, "RCV2 {0} ({1})", data, (char)data);
		            switch (data) {
		            default:
		            case -1:                        /* this is an error */
		            	throw new IOException("misplaced CR in input");

		            case 0:                         /* NUL - treat CR as CR */
		                return '\r';

		            case '\n':                      /* CRLF - treat as NL */
		                if (stickyCRLF) {
		                    seenCR = true;
		                    return '\r';
		                } else {
							logger.log(Level.TRACE, "RCV2 send NL");
		                    return '\n';
		                }
		            }
		        }
				return data;
			}
			
			// Found a control code
			ControlCode code = ControlCode.getCodeFor(data);
			if (in instanceof TelnetDebuggingInputStream)
				((TelnetDebuggingInputStream)in).setInControlMode(true);

			switch (code) {
			case IAC:
				processIAC();
				continue;
			default:
				logger.log(Level.WARNING,"Don't know what to do with code "+code);
			}

		} while (true);
	}

//	//-----------------------------------------------------------------
//	/* (non-Javadoc)
//	 * @see java.io.InputStream#read()
//	 */
//	@Override
//	public int read(byte[] b) throws IOException {
//		return in.read(b);
//	}

    /** read into a byte array */
    public int read(byte bytes[]) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    /**
     * Read into a byte array at offset <i>off</i> for length <i>length</i>
     * bytes.
     */
    @Override
    public int read(byte bytes[], int off, int length) throws IOException {
        if (binaryMode)
            return super.read(bytes, off, length);

        int c;
        int offStart = off;

        while (--length >= 0) {
            c = read();
            if (c == -1)
                break;
            if (c == '\n')
            	break;
            bytes[off++] = (byte)c;
        }
        return (off > offStart) ? off - offStart : -1;
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

		logger.log(Level.DEBUG,"IAC "+code);
		int next = -1;
		switch (code) {
		case IP:
			logger.log(Level.DEBUG,"Interrupt Process Requested");
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
			logger.log(Level.WARNING,"Received unprocessed "+code);
		}
	}

//	//-----------------------------------------------------------------
//	public void readUntilSE() throws IOException {
//		logger.log(Level.DEBUG,"Read until IAC SE");
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
//			logger.log(Level.WARNING,"Expected IAC SE but found IAC "+data2);
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

	//-----------------------------------------------------------------
	/**
	 * Read until the next CR can be found
	 * @return
	 */
	public String readUntilCR() throws IOException {
		StringBuffer buf = new StringBuffer();
		int data = -1;
		do {
			data = this.read();
			switch(data) {
			case -1: // Stream dead
				return null;
			case 10: // LINEFEED
				continue;
			default:
				buf.append( (char)data );
			}
		} while (data>31);
		return buf.toString();
	}

}
