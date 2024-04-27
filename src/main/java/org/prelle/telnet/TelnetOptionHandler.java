/**
 * 
 */
package org.prelle.telnet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * @author prelle
 *
 */
public abstract class TelnetOptionHandler implements TelnetConstants {

	protected final static Logger logger = System.getLogger("telnet.option");

	protected int code;
	protected String name;
	
	//-----------------------------------------------------------------
	protected TelnetOptionHandler(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getName() { return name; }
	public int getCode() { return code; }

	public abstract void initialize(TelnetSocket console) throws IOException;

	//-------------------------------------------------------------------
	/**
	 * @param ret
	 */
	public void indicateSupport(TelnetSocket nvt) throws IOException {
		logger.log(Level.DEBUG,"Indicate support for {0}, clientmode={1}",getName(),nvt.isInClientMode());
		nvt.expectedAnswerFor(nvt.getFromSupportedOptions(getCode()));
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		out.sendWill(getCode());
	}

	//-----------------------------------------------------------------
	public void requestUsage(TelnetSocket nvt) throws IOException {
		logger.log(Level.DEBUG,"Suggest "+getName()+" , clientmode="+nvt.isInClientMode());
		nvt.expectedAnswerFor(nvt.getFromSupportedOptions(getCode()));
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (nvt.isInClientMode()) {
//			nvt.getWillVariable(getCode()).setState(true);
			out.sendWill(getCode());
		} else {
//			nvt.getDoVariable(getCode()).setState(true);
			out.sendDo(getCode());
		}
	}

	//-----------------------------------------------------------------
	public void requestStop(TelnetSocket nvt) throws IOException {
		logger.log(Level.DEBUG,"Desire stopping "+getName());
		nvt.expectedAnswerFor(nvt.getFromSupportedOptions(getCode()));
		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
		if (nvt.isInClientMode()) {
//			nvt.getWillVariable(getCode()).setState(false);
			out.sendWont(getCode());
		} else {
//			nvt.getDoVariable(getCode()).setState(true);
			out.sendDont(getCode());
		}
	}

//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.option.TelnetOptionHandler#requestNotUsage()
//	 */
//	public void processDo(TelnetSocket nvt) throws IOException {
//		boolean[] states = nvt.getDoWillStatesFor(this);
//		boolean doState = states[0];
//		boolean willState = states[1];
//		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
//		if (willState) {
//			// This "DO" is a response
//			if (doState) {
//				// Already assume DO on remote site
//				logger.log(Level.DEBUG,"Already assume DO "+getName()+" on remote party");
//			} else {
//				nvt.getDoVariable(getCode()).setState(true);
//				logger.log(Level.INFO,"Enabled "+getName()+" (remote party accepted)");
//			}
//		} else if (!doState) {
//			// This is a request
//			if (doState) {
//				// Already in "DO" state
//				logger.log(Level.DEBUG,"Already in DO "+getName()+" locally");
//			} else {
//				if (!willState) {
//					out.sendWont(getCode());
//					logger.log(Level.INFO,"Rejected "+getName()+" (remote party requested)");
//				} else {
//					nvt.getWillVariable(getCode()).setState(true);
//					out.sendWill(getCode());
//					logger.log(Level.INFO,"Enabled "+getName()+" (remote party requested)");
//				}
//			}
//		} else {
//			logger.log(Level.DEBUG,"Ignore DO when I sent a DO");
//		}
//	}
//
//	//-----------------------------------------------------------------
//	public void processDont(TelnetSocket nvt) throws IOException {
//		boolean[] states = nvt.getDoWillStatesFor(this);
//		boolean doState = states[0];
//		boolean willState = states[1];
//		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
//		if (willState) {
//			// This "DONT" is a response
//			nvt.getDoVariable(getCode()).setState(false);
//			nvt.getWillVariable(getCode()).setState(false);
//			logger.log(Level.INFO,"Remote party rejected "+getName());
//		} else if (doState) {
//			// This is a stop request
//			if (!willState) {
//				// Already in "WONT" state
//				logger.log(Level.DEBUG,"Already in WONT "+getName()+" locally");
//			} else {
//				nvt.getWillVariable(getCode()).setState(false);
//				out.sendWont(getCode());
//				logger.log(Level.INFO,"Disabled "+getName()+" (remote party requested)");
//			}
//		} else {
//			logger.log(Level.DEBUG,"Ignore DONT when I sent a DONT");
//		}
//	}
//
//	//-----------------------------------------------------------------
//	public void processWill(TelnetSocket nvt) throws IOException {
//		NegotiationState state = nvt.getNegotiationState(getCode());
//		if (nvt.isInClientMode()) {
//			
//		} else {
//			logger.log(Level.DEBUG,"Enabled {0} (remote party accepted)", getName());
//			optionEnabled(nvt, true);
//			nvt.setNegotiationState(getCode(), NegotiationState.REMOTE_CONFIRMED);
//			
//		}
////		boolean[] states = nvt.getDoWillStatesFor(this);
////		boolean doState = states[0];
////		boolean willState = states[1];
//////		logger.log(Level.DEBUG,"doState="+doState+"   willState="+willState);
////		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
////		if (doState) {
////			// This "WILL" is a response
////			if (willState) {
////				// Already assume WILL on remote site
////				logger.log(Level.DEBUG,"Already assume WILL "+getName()+" on remote party");
////			} else {
////				nvt.getWillVariable(getCode()).setState(true);
////				logger.log(Level.DEBUG,"Enabled "+getName()+" (remote party accepted)");
////				optionEnabled(nvt, true);
////			}
////		} else if (!willState) {
////			// This is a request
////			if (doState) {
////				// Already in "DO" state
////				logger.log(Level.DEBUG,"Already in DO "+getName()+" locally");
////			} else {
////				nvt.getDoVariable(getCode()).setState(true);
////				out.sendDo(getCode());
////				logger.log(Level.INFO,"Enabled "+getName()+" (remote party requested)");
////				optionEnabled(nvt, false);
////			}
////		} else {
////			logger.log(Level.DEBUG,"Ignore WILL when I sent a WILL");
////		}
//	}
//
//	//-----------------------------------------------------------------
//	public void processWont(TelnetSocket nvt) throws IOException {
//		boolean[] states = nvt.getDoWillStatesFor(this);
//		boolean doState = states[0];
//		boolean willState = states[1];
//		TelnetOutputStream out = (TelnetOutputStream) nvt.getOutputStream();
//		logger.log(Level.DEBUG,"doState={0}   willState={1}",doState,willState);
//		if (doState) {
//			// This "WONT" is a response
//			if (!willState) {
//				// Already assume WILL on remote site
//				logger.log(Level.DEBUG,"Already assume WONT "+getName()+" on remote party");
//			} else {
//				nvt.getWillVariable(getCode()).setState(false);
//				logger.log(Level.INFO,"Disabled "+getName()+" (remote party rejected)");
//			}
//			optionDisabled(nvt, true);
//		} else if (!willState) {
//			// This is a stop request
//			if (!doState) {
//				// Already in "DO" state
//				logger.log(Level.DEBUG,"Already in DONT "+getName()+" locally");
//			} else {
//				nvt.getDoVariable(getCode()).setState(false);
//				out.sendDont(getCode());
//				logger.log(Level.INFO,"Disabled "+getName()+" (remote party requested)");
//				optionDisabled(nvt, false);
//			}
//		} else {
//			logger.log(Level.DEBUG,"Ignore WONT when I sent a WONT");
//		}
//	}
	

	//-----------------------------------------------------------------
	protected void optionEnabled(TelnetSocket nvt, boolean iAmInitiator) throws IOException {
		logger.log(Level.DEBUG,"No subnegotiation for {0}", getName());
	}

	//-----------------------------------------------------------------
	protected void optionDisabled(TelnetSocket nvt, boolean iAmInitiator) {
		logger.log(Level.INFO,getName()+" not supported");
	}

//	//-----------------------------------------------------------------
//	public boolean isEnabledByDefault() {
//		return false;
//	}

	//-----------------------------------------------------------------
	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
		
	}
	
	//-----------------------------------------------------------------
	protected static void startSubNegotiation(TelnetSocket sock, int code) throws IOException {
		OutputStream out = sock.getOutputStream();
		out.write(IAC);
		out.write(SB);
		out.write(code);		
	}
	
	//-----------------------------------------------------------------
	protected static void endSubNegotiation(TelnetSocket sock, int code) throws IOException {
		OutputStream out = sock.getOutputStream();
		out.write(IAC);
		out.write(SE);
		out.write(code);		
	}

	
}
