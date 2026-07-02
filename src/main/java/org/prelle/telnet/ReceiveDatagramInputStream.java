package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ReceiveDatagramInputStream extends InputStream {
	
	private final static Logger logger = System.getLogger("telnet");
	
	private List<byte[]> incomingData = new ArrayList<>();
	
	private byte[] currentlyConsuming;
	private int currentIndex = 0;

	//-------------------------------------------------------------------
	public ReceiveDatagramInputStream() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		logger.log(Level.INFO, "available: "+incomingData.size()+" currently: "+(currentlyConsuming != null ? currentlyConsuming.length : 0));
		if (currentlyConsuming != null && currentIndex < currentlyConsuming.length) {
			return currentlyConsuming.length - currentIndex;
		}
		synchronized (incomingData) {
			int totalAvailable = 0;
			for (byte[] data : incomingData) {
				totalAvailable += data.length;
			}
			return totalAvailable;
		}
	}
	
	@Override
	public int read() throws IOException {
		logger.log(Level.WARNING, "readSingle");
		if (currentlyConsuming != null && currentIndex < currentlyConsuming.length) {
			return currentlyConsuming[currentIndex++] & 0xFF; // Return the next byte as an int
		}
		
		synchronized (incomingData) {
			while (incomingData.isEmpty()) {
				try {
					incomingData.wait(); // Wait for new data to arrive
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("Thread interrupted while waiting for data", e);
				}
			}
			byte[] data = incomingData.remove(0);
			currentlyConsuming = data;
			currentIndex = 0;
			return currentlyConsuming[currentIndex++] & 0xFF; // Return the next byte as an int
		}
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		logger.log(Level.WARNING, "readMulti");
		if (currentlyConsuming != null && currentIndex < currentlyConsuming.length) {
			int bytesToRead = Math.min(len, currentlyConsuming.length - currentIndex);
			System.arraycopy(currentlyConsuming, currentIndex, b, off, bytesToRead);
			currentIndex += bytesToRead;
			return bytesToRead;
		}
		
		synchronized (incomingData) {
			while (incomingData.isEmpty()) {
				try {
					incomingData.wait(); // Wait for new data to arrive
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("Thread interrupted while waiting for data", e);
				}
			}
			byte[] data = incomingData.remove(0);
			currentlyConsuming = data;
			currentIndex = 0;
			int bytesToRead = Math.min(len, currentlyConsuming.length - currentIndex);
			System.arraycopy(currentlyConsuming, currentIndex, b, off, bytesToRead);
			currentIndex += bytesToRead;
			return bytesToRead;
		}
	}

	@Override
	public int read(byte[] data) throws IOException {
		return read(data, 0, data.length);
	}
	
	
	public void receiveData(byte[] data) {
        logger.log(Level.WARNING,"Received binary message of length: " + data.length);
        synchronized (incomingData) {
			if (currentlyConsuming == null) {
				currentlyConsuming = data;
				currentIndex = 0;
			} else {
				incomingData.add(data);
			}
			incomingData.notifyAll(); // Notify any waiting threads that new data is available
		}
	}	

}
