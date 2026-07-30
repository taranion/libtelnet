/**
 *
 */
package org.prelle.telnet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.prelle.telnet.TelnetConstants.ControlCode;

/**
 * @author prelle
 *
 */
public class TelnetInputStream extends FilterInputStream {

	Logger logger = System.getLogger("telnet.lvl1.in");
	
	static interface TelnetInputStreamListener {
		public void processSubnegotiation(TelnetInputStream telnetInputStream, int subNegotiationFor, int[] values);
		public void processCommand(TelnetInputStream telnetInputStream, TelnetCommand telnetCommand) throws IOException;
	}
	
	private static enum TelnetState {
		OPTION_DETECTION,
		SUBNEGOTIATION,
		DATA
	}

	private boolean commandMode;
	private boolean dataIsSubnegotiation;

	private boolean sendGoAheadAsANSISeparator = false;

	private boolean  binaryMode = true;

	private List<Integer> subNegotiationBuffer = new ArrayList<>();
	private int subNegotiationFor;
	private boolean characterMode;
	
	private TelnetOutputStream reverseStream;
	private TelnetInputStreamListener protocol;
	
	private boolean bufferHasData = false;
	
	private List<Integer> preReadData = new ArrayList<>();
	
	//-----------------------------------------------------------------
	/**
	 */
	public TelnetInputStream(InputStream in, TelnetInputStreamListener config) {
		super(in);
		this.protocol = config;
		if (in instanceof TelnetInputStream) {
			throw new RuntimeException("Cannot wrap a TelnetInputStream");
		}
	}

	//-----------------------------------------------------------------
	public String toString() {
		return "Telnet <-- "+in;
	}

	//-----------------------------------------------------------------
	public InputStream getWrappedInputStream() {
		return super.in;
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
//		logger.log(Level.INFO, "ENTER: tracingRead()");
		if (!preReadData.isEmpty()) {
			return preReadData.remove(0);
		}
		int data = in.read();
		String name = (data>=240)?ControlCode.getCodeFor(data).name():"";
		logger.log(Level.TRACE, "RCV {0} {1} ", data, name);
//		logger.log(Level.INFO, "LEAVE: tracingRead()");
		return data;
	}

	//-----------------------------------------------------------------
	public int read(byte[] buff, int offset, int length) throws IOException {
		logger.log(Level.INFO, "ENTER: read(byte[], {0}, {1})", offset, length);
		int i=0;
		bufferHasData = false;
		try {
			for (; i<length && (in.available()>0 || !preReadData.isEmpty()); i++) {
				if (offset+i>=buff.length) {
					return (i>=0)?i:-1;
				}
				int c = read();
				if (c==-1) {
					bufferHasData = (i>0);
					break;
				}
				buff[offset+i] = (byte)c;
				bufferHasData = true;
				// If the read byte is the ANSI record separator generated from a GA
				// we should stop filling the buffer here and return the bytes
				// read so far. The next read() call will continue with the
				// remaining data (e.g. the byte that followed the GA sequence).
				if (c == 0x1E) {
					// return number of bytes including the RS we just placed
					return (i+1);
				}
			}
			return (i>0)?i:-1;
		} catch (SocketTimeoutException e) {
			return (i>=0)?i:-1;
		} finally {
			logger.log(Level.INFO, "LEAVE: read(byte[], {0}, {1}) read {2}", offset, length, i);
		}
	}

	//-----------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
//		logger.log(Level.WARNING, "read() from {0} with preRead={1}", this, preReadData);
		
		
		if (commandMode) {
			int commandRaw = tracingRead();
			logger.log(Level.TRACE, "read command {0}", commandRaw);
			if (commandRaw==-1)
				return -1;
			ControlCode code = ControlCode.getCodeFor(commandRaw);
			if (code==null) {
				logger.log(Level.WARNING, "No Controlcode for "+commandRaw);
				commandMode=false;
				return commandRaw;
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
				protocol.processCommand(this, new TelnetCommand(code, cmdVal));
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
				WellKnownTelnetOptions option = WellKnownTelnetOptions.valueOf(subNegotiationFor);
				logger.log(Level.DEBUG, "Subnegotiation begins for {0}/{1}", subNegotiationFor, option);
				dataIsSubnegotiation = true;
				commandMode = false;;
				logger.log(Level.TRACE, "Leaving command mode");
				subNegotiationBuffer.clear();
				break;
			case SE:
				// Subnegotiation end
				option = WellKnownTelnetOptions.valueOf(subNegotiationFor);
				logger.log(Level.DEBUG, "Subnegotiation ends for {0}/{1}: {2}",subNegotiationFor, option,subNegotiationBuffer);
				int[] values = new int[subNegotiationBuffer.size()];
				int i=0;
				for (Integer v : subNegotiationBuffer) values[i++]=v;
				protocol.processSubnegotiation(this, subNegotiationFor,values);
				subNegotiationBuffer.clear();
				dataIsSubnegotiation = false;
				break;
			case GA:
				// Go ahead found
				if (sendGoAheadAsANSISeparator) {
					commandMode = false;;
					logger.log(Level.INFO, "GA found - convert To ANSI RS (0x1E)");
					return 0x1E; // Record separator
				} else if (bufferHasData) {
					logger.log(Level.INFO, "GA found - but buffer has data - return data");
					// It is important that we return the buffer first and only then (with the next read) return the command 
					preReadData.add(0, code.code());
					preReadData.add(0, ControlCode.IAC.code());
					commandMode = false;;
					return -1; // Stop buffer reading
				} else {
					protocol.processCommand(this, new TelnetCommand(code));
				}
				commandMode = false;
				break;
			default:
				commandMode = false;;
				logger.log(Level.DEBUG, "Leaving command mode");
				protocol.processCommand(this, new TelnetCommand(code));
			}
		}

		if (dataIsSubnegotiation) {
			readInSubnegotiationMode();
		}

		// Loop until next data is received
		commandMode = false;;
		int data = -1;
		data = tracingRead();
		logger.log(Level.ERROR, "read data {0}={1} from {2}", data, (char)data, in);
		
		switch (data) {
		case -1:
			// Lost connection
			logger.log(Level.INFO, "Lost stream");
			return -1;
		case 255:
			// Enter command mode
			logger.log(Level.DEBUG, "RCV IAC - Entering command mode");
			commandMode = true;
			return read();
		default:
			if (data>=128 && !binaryMode && data<255) {
				logger.log(Level.WARNING, "Ignore character code {0} / {2} / {1} because not in binary mode",data, (char)data, Integer.toHexString(data));
			}
			return data;
		}
	}

	//-----------------------------------------------------------------
	private Integer readUntilCommandMode() throws IOException {		
//		logger.log(Level.ERROR, "ENTER: readUntilCommandMode");
		try {
			if (commandMode) {
				throw new IOException("Already in command mode");
			}

			if (dataIsSubnegotiation) {
				readInSubnegotiationMode();
			}

			// Loop until next data is received
			int data = -1;
			while (true) {
				try {
					data = tracingRead();
					break;
				} catch (SocketTimeoutException e) {
				}
			}
			if (data==-1) {
				logger.log(Level.ERROR, "Lost stream");
				return data;
			}
			// If not in binary mode, codes >128 can be ignored
			if (data>=128 && !binaryMode && data<255) {
				logger.log(Level.WARNING, "Ignore character code {0} / {2} / {1} because not in binary mode",data, (char)data, Integer.toHexString(data));
			} else if (data==255) {
				logger.log(Level.DEBUG, "Entering command mode");
				commandMode = true;
				return null;
			}

			return data;
		} finally {
//			logger.log(Level.ERROR, "LEAVE: readUntilCommandMode");
		}
	}


	//-----------------------------------------------------------------
	private Integer readInCommandMode() throws IOException {
		logger.log(Level.DEBUG, "readInCommandMode");
		if (!commandMode) {
			throw new RuntimeException("Not in command mode");
		}
		
		int commandRaw = tracingRead();
		logger.log(Level.DEBUG, "read command {0}", commandRaw);
		if (commandRaw==-1)
			return -1;
		ControlCode code = ControlCode.getCodeFor(commandRaw);
		if (code==null) {
			logger.log(Level.WARNING, "No Controlcode for "+commandRaw);
			commandMode=false;
			return commandRaw;
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
			logger.log(Level.DEBUG, "recv: {0} {1}", code, cmdVal);
			protocol.processCommand(this, new TelnetCommand(code, cmdVal));
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
			WellKnownTelnetOptions option = WellKnownTelnetOptions.valueOf(subNegotiationFor);
			logger.log(Level.DEBUG, "Subnegotiation begins for {0}/{1}", subNegotiationFor, option);
			dataIsSubnegotiation = true;
			commandMode = false;;
			logger.log(Level.TRACE, "Leaving command mode");
			subNegotiationBuffer.clear();
			break;
		case SE:
			// Subnegotiation end
			option = WellKnownTelnetOptions.valueOf(subNegotiationFor);
			logger.log(Level.DEBUG, "Subnegotiation ends for {0}/{1}: {2}",subNegotiationFor, option,subNegotiationBuffer);
			int[] values = new int[subNegotiationBuffer.size()];
			int i=0;
			for (Integer v : subNegotiationBuffer) values[i++]=v;
			protocol.processSubnegotiation(this, subNegotiationFor,values);
			subNegotiationBuffer.clear();
			dataIsSubnegotiation = false;
			break;
		case GA:
			// Go ahead found
			if (sendGoAheadAsANSISeparator) {
				commandMode = false;;
				logger.log(Level.DEBUG, "GA found - convert To ANSI RS (0x1E)");
				protocol.processCommand(this, new TelnetCommand(code));
				return 0x1E; // Record separator
			} 
		default:
			commandMode = false;;
			logger.log(Level.DEBUG, "Leaving command mode");
			protocol.processCommand(this, new TelnetCommand(code));
		}

		return readUntilCommandMode();
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
		logger.log(Level.DEBUG, "readInSubnegotiationMode");
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
					protocol.processSubnegotiation(this, subNegotiationFor, values);
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

//    /** read into a byte array */
//    public int read(byte bytes[]) throws IOException {
//        return read(bytes, 0, bytes.length);
//    }
//
//    /**
//     * Read into a byte array at offset <i>off</i> for length <i>length</i>
//     * bytes.
//     */
//    @Override
//    public int read(byte bytes[], int off, int length) throws IOException {
////    	logger.log(Level.ERROR, "ENTER: read(byte[], {0}, {1})", off, length);
//    	try {
//            Integer c;
//            int offStart = off;
//
//            int amountRead = 0;
//            while (--length >= 0) {
//            	if (commandMode) {
//    				c = readInCommandMode();
//    			} else {
//    				c = readUntilCommandMode();				
//    			}
//				if (c==null) {
//					// Just entered command mode - return the current
//					logger.log(Level.DEBUG, "Command mode detected");
//					if (amountRead>0)
//				           return (off > offStart) ? off - offStart : -1;
//					else continue;
//				} 
//           	
//                amountRead++;
//                if (c == -1)
//                    break;
//                bytes[off++] = (byte)((int)c);
//                if (c == '\n')
//                	break;
//            }
//            return (off > offStart) ? off - offStart : -1;
//    	} catch (Exception e) {
//			logger.log(Level.ERROR, "Exception in read(byte[], {0}, {1}): {2}", off, length, e);
//			throw e;
//    	} finally {
////    		logger.log(Level.ERROR, "LEAVE: read(byte[], {0}, {1})", off, length);
//    	}
//    }

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
//		logger.log(Level.ERROR, "available will be passed to "+in);
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

	public boolean isSendGoAheadAsANSISepator() {
		return sendGoAheadAsANSISeparator;
	}

	public void setSendGoAheadAsANSISepator(boolean sendGoAheadAsANSISepator) {
		this.sendGoAheadAsANSISeparator = sendGoAheadAsANSISepator;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the reverseStream
	 */
	public TelnetOutputStream getReverseStream() {
		return reverseStream;
	}

	//-------------------------------------------------------------------
	/**
	 * @param reverseStream the reverseStream to set
	 */
	public void setReverseStream(TelnetOutputStream reverseStream) {
		this.reverseStream = reverseStream;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the protocol
	 */
	public TelnetProtocol getProtocol() {
		return (TelnetProtocol) protocol;
	}

	//-------------------------------------------------------------------
	/**
	 * @param preReadData the preReadData to set
	 */
	public void addPreReadData(List<Integer> data) {
		this.preReadData.addAll(data);
	}

}
