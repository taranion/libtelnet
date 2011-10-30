/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prelle.telnet.option.TelnetEcho;
import org.prelle.telnet.option.TelnetOption;

/**
 * @author prelle
 *
 */
public class NetworkVirtualConsole implements TelnetConstants {

	private Logger logger = Logger.getLogger("telnet");

	private static Map<Integer, TelnetOption> options = new HashMap<Integer, TelnetOption>();
	private static int count = 0;

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private String name;
	private LineBuffer buffer;
	private Map<String,TelnetVariable> optionFlags;
	private NetworkVirtualConsoleListener listener;
	
	private boolean clientMode;

	//-----------------------------------------------------------------
	public static void registerOption(TelnetOption option) {
		options.put(option.getCode(), option);
	}

	//-----------------------------------------------------------------
	/**
	 * @throws IOException 
	 */
	public NetworkVirtualConsole(Socket socket, boolean clientMode, NetworkVirtualConsoleListener list) throws IOException {
		this.socket = socket;
		in = socket.getInputStream();
		out= socket.getOutputStream();
		this.listener   = list;
		this.clientMode = clientMode;
		optionFlags = new HashMap<String, TelnetVariable>();
		name = "nvc-"+(++count);
		logger = Logger.getLogger("telnet."+name);

		buffer = new LineBuffer();
		for (TelnetOption opt : options.values())
			opt.setDefaults(this);
		logger.debug("Variables = "+optionFlags.values());

		for (TelnetOption opt : options.values())
			opt.initialize(this);

		//		sendDo(SUB_NAWS);
		//		sendWont(SUB_ECHO);
		//		sendWill(SUB_BINARY);
	}

	//-----------------------------------------------------------------
	public NetworkVirtualConsoleListener getListener() {
		return listener;
	}
	
	//--------------------------------------------------------------
	public void setOptionVariable(TelnetVariable var) {
		optionFlags.put(var.getKey(), var);
	}

	//--------------------------------------------------------------
	public WillVariable getWillVariable(String name) {
		for (TelnetVariable var : optionFlags.values()) {
			if (var instanceof WillVariable && var.getName().equals(name))
				return (WillVariable) var;
		}
		return null;
	}

	//--------------------------------------------------------------
	public DoVariable getDoVariable(String name) {
		for (TelnetVariable var : optionFlags.values()) {
			if (var instanceof DoVariable && var.getName().equals(name))
				return (DoVariable) var;
		}
		return null;
	}

	//--------------------------------------------------------------
	public boolean[] getDoWillStatesFor(TelnetOption opt) {
		String key1 = "DO/DONT_"+opt.getName();
		String key2 = "WILL/WONT_"+opt.getName();
		boolean[] ret = new boolean[]{optionFlags.get(key1).getState(), optionFlags.get(key2).getState()};
//		logger.debug("Do/Will states for "+opt.getName()+" are: "+Arrays.toString(ret));
		return ret;
	}

	//--------------------------------------------------------------
	public void sendWill(int code) throws IOException {
		logger.debug("Send IAC WILL "+code);
		byte[] send = new byte[3];
		send[0] = (byte)IAC; 
		send[1] = (byte)WILL; 
		send[2] = (byte)code;  
		out.write(send);
		out.flush();
	}

	//--------------------------------------------------------------
	public void sendWont(int code) throws IOException {
		logger.debug("Send IAC WONT "+code);
		byte[] send = new byte[3];
		send[0] = (byte)IAC; 
		send[1] = (byte)WONT; 
		send[2] = (byte)code;  
		out.write(send);
		out.flush();
	}

	//--------------------------------------------------------------
	public void sendDo(int code) throws IOException {
		logger.debug("Send IAC DO "+code);
		byte[] send = new byte[3];
		send[0] = (byte)IAC; 
		send[1] = (byte)DO; 
		send[2] = (byte)code;  
		out.write(send);
		out.flush();
	}

	//--------------------------------------------------------------
	public void sendDont(int code) throws IOException {
		logger.debug("Send IAC DONT "+code);
		byte[] send = new byte[3];
		send[0] = (byte)IAC; 
		send[1] = (byte)DONT; 
		send[2] = (byte)code;  
		out.write(send);
		out.flush();
	}

	//--------------------------------------------------------------
	private String getCommandName(int com) {
		switch (com) {
		case SE  : return "SE ";
		case IP  : return "IP ";
		case DO  : return "DO ";
		case DONT: return "DON'T ";
		case WILL: return "WILL ";
		case WONT: return "WON'T ";
		case SB  : return "SB ";
		case IAC : return "IAC ";
		default:
			return "("+com+") ";
		}
	}

	//--------------------------------------------------------------
	public String read() throws IOException {
		while (true) {
			logger.trace("Waiting for input");
			int dat = in.read();
			switch (dat) {
			case -1:
				logger.info("Connection lost");
				throw new IOException("Connection lost");
			case IAC:
				processIAC(in);
				break;
			case 3:
				// CTRL-C
				logger.debug("CTRL-C");
				//					out.println("Bye\r");
				out.close();
				return null;
			case 4:
				// CTRL-D
				logger.debug("CTRL-D / EOT");
				buffer.processKeyEvent(LineBuffer.ENTER);
				out.write(13);
				out.write(10);
				out.flush();
				return buffer.getFinishedInput();
			case 10:
				logger.debug("CF");
				buffer.processKeyEvent(LineBuffer.ENTER);
				out.write(13);
				out.write(10);
				out.flush();
				return buffer.getFinishedInput();
			case 13:
				// Return
				logger.debug("CR");
				break;
			default:
				logger.debug((char)dat+"  // "+dat);
				buffer.appendChar((char)dat);
				if (!Character.isLetterOrDigit((char)dat)) 
					logger.debug("("+dat+")");
				if (isEchoEnabled()) {
					logger.info("Send echo of input");
					out.write((byte)dat);
					out.flush();
				}
			}
		} // while true
	}

	//--------------------------------------------------------------
	private void processIAC(InputStream in) throws IOException {
		logger.trace("IAC ");
		int next = in.read();
		logger.trace(getCommandName(next));

		switch (next) {
		case SB:
			processSubNegotiation(in);
			break;
		case WILL:
		case WONT:
		case DO  :
		case DONT:
			processOptionNegotiation(next, in);
			break;
		case IP:
			logger.debug("Interrupt Process");
			try {
				listener.interruptProcessRequested(this);
			} catch (Exception e) {
				logger.error("While in listener.interruptProcessRequested(): "+e.toString(),e);
			}
			break;
		case 27:
			int what = in.read();
			if (what==91) {
				int sub  = in.read();
				// Cursor
				switch (sub) {
				case 65: 
					logger.debug("Cursor up");
					buffer.processKeyEvent(LineBuffer.UP);
					//                                        String toDisplay = buffer.toString();
					//                                        if (toDisplay!=null) {
					//                                                out.print(toDisplay);
					//                                                out.flush();                                                    
					//                                        }
					break;
				case 66: logger.debug("Cursor down"); break;
				case 67: logger.debug("Cursor right"); break;
				case 68: logger.debug("Cursor left"); break;
				default:
					logger.debug("ESC (91) ("+sub+")");
				}
			} else
				logger.debug("ESC");
			break;
		case 10:
			// Follows any CR - ignore
			break;
		case 8:
			// Delete
			logger.debug("Delete");
			buffer.processKeyEvent(LineBuffer.DELETE);
			out.write(8);
			out.flush();
			//				} else if (dat==9) {
			//					// Tabulator
			//					buffer.processKeyEvent(LineBuffer.TABULATOR);
			//					String remem = buffer.getUnfinishedInput();
			//					user.remember = remem;
			//					String command = remem+BaseShell.INDICATE_TAB;
			//
			//					out.flush();
			//					return command;
			//					break;
			break;
		case 26:
			//					} else if (dat==26) {
			//					// CTRL-Z
			//					//                            user.dir = registry.getRoot();
			//					user.buf = new StringBuffer();
			//					out.write(13);
			//					out.write(10);
			//					out.flush();
			//					return;
			break;
		case 127:
			// BackSpace
			logger.debug("DeleteCode with 127");
			buffer.processKeyEvent(LineBuffer.DELETE);
			out.write(8);
			out.flush();
			out.write(32);
			out.write(8);
			out.flush();
			break;
		default:
		}
		
	}

	//--------------------------------------------------------------
	private void processOptionNegotiation(int command, InputStream in) throws IOException {
		int optionCode = in.read();
		TelnetOption option = options.get(optionCode);
		if (option==null) {
			logger.warn("Unsupported option: "+optionCode);
			switch (command) {
			case WILL:
				sendDont(optionCode);
				break;
			case DO:
				sendWont(optionCode);
				break;
			case DONT:
				sendWont(optionCode);
				break;
			case WONT:
				sendDont(optionCode);
				break;
			}
		} else {
			switch (command) {
			case WILL:
				logger.debug("Client requests/accepts "+option);
				option.processWill(this);
				break;
			case DO:
				logger.debug("Server requests/accepts "+option);
				option.processDo(this);
				break;
			case DONT:
				logger.debug("Server stops/rejects "+option);
				option.processDont(this);
				break;
			case WONT:
				logger.debug("Client stops/rejects "+option);
				option.processWont(this);
				break;
			}
		}

	}

	//--------------------------------------------------------------
	private void processSubNegotiation(InputStream in) throws IOException {
		int optionCode = in.read();
		TelnetOption option = options.get(optionCode);
		if (option!=null) {
			logger.debug("Perform subnegotation using "+option);
			option.performSubNegotiation(this, in);
		} else {
			logger.warn("Unsupported option: "+optionCode);
			switch (optionCode) {
			case SUB_BINARY: logger.debug("BINARY"); break;
			case SUB_ECHO: logger.debug("ECHO"); break;
			case SUB_RECONNECTION: logger.debug("RECONNECTION"); break;
			case SUB_SUPRESS_GO_AHEAD: logger.debug("SUPRESS GO AHEAD"); break;
			case SUB_STATUS: logger.debug("STATUS"); break;
			case SUB_LINEMODE: logger.debug("LINEMODE"); break;
			case SUB_TERMTYPE: logger.debug("TERMTYPE"); break;
			case SUB_NAWS: logger.debug("NAWS"); break;
			}
			// Read to negotation end
			int code1 = in.read();
			int code2 = in.read();
			while (!(code1==IAC && code2==SE)) {
				logger.debug("  "+code1+" / "+code2);
				code1 = code2;
				code2 = in.read();
			}
		}
		logger.debug("Subnegotiation end");
	}

	//--------------------------------------------------------------
	public void sendText(String text) throws IOException {
		out.write(text.getBytes());
		out.flush();
	}

	//-----------------------------------------------------------------
	public void requestEcho() throws IOException {
		options.get(TelnetEcho.CODE).requestUsage(this);
	}

	//-----------------------------------------------------------------
	public boolean isEchoEnabled() throws IOException {
		return optionFlags.get("DO/DONT_"+TelnetEcho.NAME).getState();
	}

	//-----------------------------------------------------------------
//	public void requestEchoStop(NetworkVirtualConsole nvt) throws IOException {
//		nvt.sendDont(SUB_ECHO);
//	}
//
//	//-----------------------------------------------------------------
//	public void confirmEcho(NetworkVirtualConsole nvt) throws IOException {
//		nvt.sendWill(SUB_ECHO);
//	}
//
//	//-----------------------------------------------------------------
//	public void confirmEchoStop(NetworkVirtualConsole nvt) throws IOException {
//		nvt.sendWont(SUB_ECHO);
//	}

	//-----------------------------------------------------------------
	/**
	 * @return the clientMode
	 */
	public boolean isClientMode() {
		return clientMode;
	}

	//-----------------------------------------------------------------
	public OutputStream getOutputStream() {
		return out;
	}

	//-----------------------------------------------------------------
	public void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			logger.error(e.toString(),e);
		}
	}

}
