package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Consumer;

/**
 * 
 */
@Deprecated
public abstract class SendDatagramOutputStream extends OutputStream {
	
	private final static Logger logger = System.getLogger("telnet");

	private Consumer<byte[]> sendFunction;
	
	//-------------------------------------------------------------------
	/**
	 */
	public SendDatagramOutputStream(Consumer<byte[]> sendFunction) {
		this.sendFunction = sendFunction;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
//		logger.log(Level.WARNING, "writeSingle");
		sendFunction.accept(new byte[] {(byte)b});
	}
	
	@Override
	public void write(byte[] buf) throws IOException {
		logger.log(Level.WARNING, "writeMulti {0}", buf.length);
		sendFunction.accept(buf);
	}

}
