/**
 * 
 */
package foo;

import java.io.IOException;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.prelle.telnet.NetworkVirtualConsole;

/**
 * @author prelle
 *
 */
public class NetThread extends Thread {

	private Logger logger = Logger.getLogger("app");

	private NetworkVirtualConsole nvt;

	//-----------------------------------------------------------------
	/**
	 */
	public NetThread(NetworkVirtualConsole nvt) {
		this.nvt = nvt;
	}

	public void run() {
		while (true) {
			try {
				logger.debug("Read "+nvt.read());
			} catch (SocketException e) {
				logger.info("Connection has been closed");
				return;
			} catch (IOException e) {
				logger.info("Connection has been lost");
				return;
			}
			Thread.yield();
		}
	}
}
