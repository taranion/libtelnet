/**
 *
 */
package org.prelle.telnet.option;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.prelle.telnet.CommunicationRole;
import org.prelle.telnet.TelnetOptionListener;
import org.prelle.telnet.TelnetProtocol;
import org.prelle.telnet.TelnetOption;

/**
 *
 * @author prelle
 *
 */
public class MXPOption implements TelnetOption<MXPOption.MXPListener> {

	protected final static Logger logger = System.getLogger("telnet.option.mxp");

	public static interface MXPListener extends TelnetOptionListener {
		public void telnetMXPLearned(MxpSupportTable data);
		public void mxpClientInfo(String client, String version, String mxpVersion, String style);
		public void mxpDTDChanged(String newDTD);
	}

	private CommunicationRole role;
	private List<String> supports;
	private MxpSupportTable supportTable = new MxpSupportTable();
	private String client;
	private String clientVersion;
	private String mxpVersion;
	private String style;
	private List<MXPListener> listeners = new ArrayList<>();

	//-----------------------------------------------------------------
	public MXPOption(CommunicationRole role,String ...supports) {
		this.role = role;
		this.supports = List.of(supports);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#getOptionCode()
	 */
	@Override
	public int getOptionCode() {
		return 91;
	}
	
	//-------------------------------------------------------------------
	@Override
	public String getName() { return "MXP"; }
	
	//-----------------------------------------------------------------
	/**
	 * Called from TelnetProtocol to learn if this handler will initiate communication or wait for the other side to do so.
	 */
	public boolean startCommunicationAs(CommunicationRole role) {
		return role==CommunicationRole.SERVER;
	}

	//-----------------------------------------------------------------
	/**
	 * Called after the use of a option has been confirmed
	 * @return TRUE when answers to a subnegotiation are expected
	 * @see org.prelle.telnet.TelnetOption#negotiateDetails(org.prelle.telnet.TelnetProtocol)
	 */
	public boolean negotiateDetails(TelnetProtocol stack) {
		if (role==CommunicationRole.CLIENT) {
			// client should send support
			return false;
		}
		logger.log(Level.ERROR, "Ask for support");
		String support = "\u001B[6z<SUPPORT>\n<VERSION>\u001B[7z";
		try {
			stack.getOutputStream().write(support.getBytes(StandardCharsets.US_ASCII));
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	//-------------------------------------------------------------------
	@Override
	public void handleSubnegotiation(int[] values, TelnetProtocol stack) {
		logger.log(Level.WARNING, "MXPOption.handleSubnegotiation: "+Arrays.toString(values));
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetOption#addListener(org.prelle.telnet.TelnetOptionListener)
	 */
	@Override
	public void addListener(MXPListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	//-------------------------------------------------------------------
	public void fireDTDChange(String dtd) {
		for (MXPListener l: listeners) {
			l.mxpDTDChanged(dtd);
		}
	}

	//-------------------------------------------------------------------
	public void fireClientInfo() {
		for (MXPListener l: listeners) {
			l.mxpClientInfo(client, clientVersion, mxpVersion, style);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @return the supports
	 */
	public List<String> getSupports() {
		return supports;
	}

	//-------------------------------------------------------------------
	/**
	 * @param supports the supports to set
	 */
	public void setSupports(List<String> supports) {
		this.supports = supports;
		// I need to parse "+a +a.href +a.hint +a.expire +b +bold +br +color +color.fore +color.back +dest +dest.name +dest.eol +dest.eof +element +element.name +element.definition +element.att +element.tag +element.flag +element.open +element.delete +element.empty +em +entity +entity.name +entity.value +entity.desc +entity.private +entity.publish +entity.delete +entity.add +entity.remove +expire +expire.name +font +font.color +font.back +frame +frame.name +frame.action +frame.internal +frame.external +frame.align +frame.left +frame.right +frame.top +frame.bottom +frame.width +frame.height +frame.scrolling +frame.floating +frame.title +gauge +gauge.max +gauge.caption +gauge.color +h +h1 +h2 +h3 +h4 +h5 +h6 +high +hr +i +italic +music +music.fname +music.v +music.l +music.p +music.c +music.t +music.u +s +send +send.href +send.hint +send.prompt +send.expire +small +sound +sound.fname +sound.v +sound.l +sound.p +sound.t +sound.u +stat +stat.max +stat.caption +strikeout +strong +support +tt +u +underline +var +var.publish +version" into a datastructure
		supportTable.parse(String.join(" ", supports));
		
		listeners.forEach(l ->  l.telnetMXPLearned(supportTable));
	}

	//-------------------------------------------------------------------
	/**
	 * @return the client
	 */
	public String getClient() {
		return client;
	}

	//-------------------------------------------------------------------
	/**
	 * @param client the client to set
	 */
	public void setClient(String client) {
		this.client = client;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the clientVersion
	 */
	public String getClientVersion() {
		return clientVersion;
	}

	//-------------------------------------------------------------------
	/**
	 * @param clientVersion the clientVersion to set
	 */
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the mxpVersion
	 */
	public String getMxpVersion() {
		return mxpVersion;
	}

	//-------------------------------------------------------------------
	/**
	 * @param mxpVersion the mxpVersion to set
	 */
	public void setMxpVersion(String mxpVersion) {
		this.mxpVersion = mxpVersion;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the style
	 */
	public String getStyle() {
		return style;
	}

	//-------------------------------------------------------------------
	/**
	 * @param style the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}
}
