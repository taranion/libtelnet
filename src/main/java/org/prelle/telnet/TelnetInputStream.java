/**
 *
 */
package org.prelle.telnet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetInputStream extends FilterInputStream {

	Logger logger = System.getLogger("telnet.lvl1.in");



	private TelnetSocket listener;
	private boolean commandMode;
	private boolean dataIsSubnegotiation;

	/** If stickyCRLF is true, then we're a machine, like an IBM PC,
    where a Newline is a CR followed by LF.  On UNIX, this is false
    because Newline is represented with just a LF character. */
	boolean         stickyCRLF = false;
	boolean         seenCR = false;

	public boolean  binaryMode = true;

	private List<Integer> subNegotiationBuffer = new ArrayList<>();
	private int subNegotiationFor;
	private boolean characterMode;

	//-----------------------------------------------------------------
	/**
	 */
	public TelnetInputStream(TelnetSocket list, InputStream in) {
		super(in);
		this.listener = list;
	}

	//-----------------------------------------------------------------
	public void setBinaryMode(boolean enabled) {
		if (!binaryMode && enabled) {
			logger.log(Level.INFO, "Enable 8 bit binary transfer");
		} else if (binaryMode && !enabled) {
			logger.log(Level.WARNING, "Disable 8 bit binary transfer");
		}
		binaryMode = enabled;
	}

	//-----------------------------------------------------------------
	public boolean isInBinaryMode() {
		return binaryMode;
	}

//	//-----------------------------------------------------------------
//	private ControlCode readNextCode() throws IOException {
//		int data = in.read();
//		ControlCode code = ControlCode.getCodeFor(data);
//		if (code==null)
//			throw new IOException("Expected control code but found "+data);
//		return code;
//	}

	//-----------------------------------------------------------------
	private int tracingRead() throws IOException {
		int data = in.read();
		String name = (data>=240)?ControlCode.getCodeFor(data).name():"";
		logger.log(Level.TRACE, "RCV {0} {1} ", data, name);
		return data;
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		if (commandMode) {
			int commandRaw = tracingRead();
			if (commandRaw==-1)
				return -1;
			ControlCode code = ControlCode.getCodeFor(commandRaw);
			if (code==null) {
				logger.log(Level.WARNING, "No Controlcode for "+commandRaw);
			}
			switch (code) {
			case IAC : return 255;
			case WILL: case WONT:
			case DO  : case DONT:
				if (dataIsSubnegotiation) {
					logger.log(Level.WARNING, "Receive an IAC {0} while in SB mode", code);
				}
				int cmdVal = tracingRead();
				commandMode = false;;
				if (cmdVal==-1) {
					logger.log(Level.WARNING, "Connection reset");
					return -1;
				}
				logger.log(Level.INFO, "recv: {0} {1}", code, cmdVal);
				listener.processCommand(new TelnetCommand(code, cmdVal));
				break;
			case SB:
				// Subnegotiation begin
				subNegotiationFor = tracingRead();
				if (subNegotiationFor==-1) {
					logger.log(Level.WARNING, "Connection reset");
					commandMode = false;;
					logger.log(Level.TRACE, "Leaving command mode");
					return -1;
				}
				TelnetOption option = TelnetOption.valueOf(subNegotiationFor);
				logger.log(Level.DEBUG, "Subnegotiation begins for {0}/{1}", subNegotiationFor, option);
				dataIsSubnegotiation = true;
				commandMode = false;;
				logger.log(Level.TRACE, "Leaving command mode");
				subNegotiationBuffer.clear();
				break;
			case SE:
				// Subnegotiation end
				option = TelnetOption.valueOf(subNegotiationFor);
				logger.log(Level.DEBUG, "Subnegotiation ends for {0}/{1}: {2}",subNegotiationFor, option,subNegotiationBuffer);
				int[] values = new int[subNegotiationBuffer.size()];
				int i=0;
				for (Integer v : subNegotiationBuffer) values[i++]=v;
				listener.processSubnegotiation(subNegotiationFor,values);
				subNegotiationBuffer.clear();
				dataIsSubnegotiation = false;
				break;
			default:
				commandMode = false;;
				logger.log(Level.TRACE, "Leaving command mode");
				logger.log(Level.WARNING, "call listener "+listener);
				listener.processCommand(new TelnetCommand(code));
			}
		}

		if (dataIsSubnegotiation) {
			readInSubnegotiationMode();
		}

		// Loop until next data is received
		commandMode = false;;
//		do {
			int data = -1;
			while (true) {
				data = in.read();
				logger.log(Level.TRACE, "RCV DATA {0} ({1})", data, (char)data);
				if (data==-1)
					return data;
				// If not in binary mode, codes >128 can be ignored
				if (data>=128 && !binaryMode && data<255) {
					logger.log(Level.WARNING, "Ignore character code {0} / {2} / {1} because not in binary mode",data, (char)data, Integer.toHexString(data));
				}
				else
					break;
			}

			if (data==255) {
				logger.log(Level.TRACE, "Entering command mode");
				commandMode = true;
				return read();
			}
//		} while (true);
			return data;
	}

//	//-----------------------------------------------------------------
//	private int readInDataMode() throws IOException {
//		// Loop until next data is received
//		do {
//			int data = -1;
//			while (true) {
//				data = in.read();
//				logger.log(Level.TRACE, "RCV {0} ({1})", data, (char)data);
//				if (data==-1)
//					return data;
//				// If not in binary mode, codes >128 can be ignored
//				if (data>=128 && !binaryMode && data<255) {
//					logger.log(Level.WARNING, "Ignore character code {0} / {1} because not in binary mode",data, (char)data);
//				} else
//					break;
//			}
//
//			if (data==255) {
//				commandMode = true;
//				return read();
//			}
//		} while (true);
//	}

//	//-----------------------------------------------------------------
//	private int readInCommandMode() throws IOException {
//		int commandRaw = in.read();
//		logger.log(Level.TRACE, "RCV {0} ", commandRaw);
//		ControlCode code = ControlCode.getCodeFor(commandRaw);
//		switch (code) {
//		case IAC : return 255;
//		case WILL: case WONT:
//		case DO  : case DONT:
//			int cmdVal = in.read();
//			logger.log(Level.TRACE, "RCV {0} ", cmdVal);
//			commandMode = false;;
//			if (cmdVal==-1) {
//				logger.log(Level.WARNING, "Connection reset");
//				return -1;
//			}
//			listener.processCommand(new TelnetCommand(code, cmdVal));
//			break;
//		case SB:
//			// Subnegotiation begin
//			cmdVal = in.read();
//			logger.log(Level.TRACE, "RCV {0} ", cmdVal);
//			if (cmdVal==-1) {
//				logger.log(Level.WARNING, "Connection reset");
//				commandMode = false;;
//				return -1;
//			}
//			logger.log(Level.DEBUG, "Subnegotiation begins for {0}", cmdVal);
//			dataIsSubnegotiation = true;
//			commandMode = false;;
//			subnegotiationBuffer.clear();
//			break;
//		case SE:
//			// Subnegotiation end
//			logger.log(Level.WARNING, "Subnegotiation ends "+subnegotiationBuffer);
//			System.exit(1);
//			break;
//		default:
//			commandMode = false;;
//			listener.processCommand(new TelnetCommand(code));
//		}
//
//		if (dataIsSubnegotiation) {
//			readInCommandMode();
//		}
//		return readInDataMode();
//	}

	//-----------------------------------------------------------------
	private int readInSubnegotiationMode() throws IOException {
		do {
			int data = -1;
			while (true) {
				data = tracingRead();
				if (data==-1)
					return data;
				// If not in binary mode, codes >128 can be ignored
				if (data>=128 && !binaryMode && data<255) {
					logger.log(Level.WARNING, "Ignore character code {0} / {1} because not in binary mode",data, (char)data);
				} else
					break;
			}

			if (data==255) {
				data = tracingRead();
				if (data==ControlCode.SE.code()) {
					dataIsSubnegotiation = false;
					int[] values = new int[subNegotiationBuffer.size()];
					int i=0; for (Integer  t: subNegotiationBuffer) values[i++]=t;
					listener.processSubnegotiation(subNegotiationFor, values);
					return data;
				} else if (data<255) {
					logger.log(Level.WARNING, "Received a control code !=SE {0} while in sub-negotiation",data);
				}
			}
			subNegotiationBuffer.add(data);
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

//	//-----------------------------------------------------------------
//	private void processIAC() throws IOException {
//		//		mode = Mode.RCV_IAC;
//		ControlCode code = readNextCode();
//
//		logger.log(Level.DEBUG,"IAC "+code);
//		int next = -1;
//		switch (code) {
//		case IP:
//			logger.log(Level.DEBUG,"Interrupt Process Requested");
//			listener.receivedInterruptProcess();
//			break;
//		case GA:
//			listener.receivedGoAheadSignal();
//			break;
//		case WILL:
//			next = in.read();
//			listener.receivedWILL(next);
//			break;
//		case WONT:
//			next = in.read();
//			listener.receivedWONT(next);
//			break;
//		case DO:
//			next = in.read();
//			listener.receivedDO(next);
//			break;
//		case DONT:
//			next = in.read();
//			listener.receivedDONT(next);
//			break;
//		case SB:
//			next = in.read();
//			listener.receivedSubnegotiationBegin(next);
//			break;
//		default:
//			logger.log(Level.WARNING,"Received unprocessed "+code);
//		}
//	}

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
//
//	//-----------------------------------------------------------------
//	/**
//	 * Switch the stream into a dumb mode, letting the higher level
//	 * interpret the bytes
//	 * @param b
//	 */
//	public void setHigherLevelControl(boolean higherLevel) {
//		inIACMode = higherLevel;
//	}

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
//			case 10: // LINEFEED
//				continue;
			default:
				buf.append( (char)data );
			}
		} while (data>31);
		return buf.toString();
	}

	//-------------------------------------------------------------------
	/**
	 * @return the characterMode
	 */
	public boolean isCharacterMode() {
		return characterMode;
	}

	//-------------------------------------------------------------------
	/**
	 * @param characterMode the characterMode to set
	 */
	public void setCharacterMode(boolean characterMode) {
		this.characterMode = characterMode;
	}

}
