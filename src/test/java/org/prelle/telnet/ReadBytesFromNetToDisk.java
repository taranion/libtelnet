package org.prelle.telnet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 */
public class ReadBytesFromNetToDisk {

	//-------------------------------------------------------------------
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		TelnetSocket sock = new TelnetSocket("mud.paramud.com", 23);
		Path out = Paths.get("from_socket.dat");
		Files.deleteIfExists(out);
		Thread.sleep(100);
//		System.out.println("To read: "+sock.getInputStream().available());
		FileOutputStream fout = new FileOutputStream(out.toFile());
		while (true) {
			int i = sock.getInputStream().read();
			if (i<0) {
				fout.flush();
				fout.close();
				break;
			}
			fout.write(i);
		}
		fout.close();
//		Files.copy(sock.getInputStream(), out);
		System.out.println("Did read: "+Files.size(out));
		System.exit(1);
	}

}
