/**
 *
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.telnet.option.TelnetProtocol;

/**
 * Reads byte buffers from the underlying stream
 * @author prelle
 *
 */
public class TelnetInputStream extends ReceiveDatagramInputStream  {

	Logger logger = System.getLogger("telnet.lvl1.in");

	private InputStream in;
	
	private TelnetOutputStream reverseStream;
	private TelnetProtocol protocol;
	
	//-----------------------------------------------------------------
	/**
	 */
	public TelnetInputStream(InputStream in, TelnetProtocol stack) {
		this.in = in;
		this.protocol = stack;
		if (in instanceof TelnetInputStream) {
			throw new RuntimeException("Cannot wrap a TelnetInputStream");
		}
		
		stack.setDataListener(dataEv -> {
			logger.log(Level.WARNING, "Consume "+dataEv);
			super.receiveData(dataEv.getData());
		});
		startReadingFromSocket();
	}

	//-----------------------------------------------------------------
	void startReadingFromSocket() {
		// Start virtual thread to read from the underlying stream and process commands
		Runnable reader = () -> {
			try {
				byte[] readBuffer = new byte[8192];
				logger.log(Level.INFO, "Starting reader thread for "+in);
				while (true) {
					int read = in.read(readBuffer, 0, readBuffer.length);
					logger.log(Level.DEBUG, "Read {0} bytes from stream", read);
					if (read==-1) {
						logger.log(Level.INFO, "Stream closed");
						super.closed = true;
						synchronized (super.incomingData) {
							super.incomingData.notifyAll();
						}
						break;
					}
					if (read>0) {
						byte[] data = new byte[read];
						System.arraycopy(readBuffer, 0, data, 0, read);
						protocol.process(data);
					}
				}
			} catch (IOException e) {
				logger.log(Level.ERROR, "Exception in reader thread: {0}", e);
			}
		};
		Thread thread = Thread.startVirtualThread(reader);
		thread.setName("SocketToTelnetParser");
	}

	//-----------------------------------------------------------------
	public String toString() {
		return "Telnet <-- "+in;
	}

	//-----------------------------------------------------------------
	@Deprecated
	public InputStream getWrappedInputStream() {
		return in;
	}

//	//-----------------------------------------------------------------
//	public void setBinaryMode(boolean enabled) {
//		if (!binaryMode && enabled) {
//			logger.log(Level.INFO, "Enable 8 bit binary transfer");
//		} else if (binaryMode && !enabled) {
//			logger.log(Level.WARNING, "Disable 8 bit binary transfer");
//		}
//		binaryMode = enabled;
//	}
//
//	//-----------------------------------------------------------------
//	public boolean isInBinaryMode() {
//		return binaryMode;
//	}



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

	public boolean isSendGoAheadAsANSISepator() {
		return protocol.isSendGoAheadAsANSISepator();
	}

	public void setSendGoAheadAsANSISepator(boolean sendGoAheadAsANSISepator) {
		protocol.setSendGoAheadAsANSISepator(sendGoAheadAsANSISepator);
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

}
