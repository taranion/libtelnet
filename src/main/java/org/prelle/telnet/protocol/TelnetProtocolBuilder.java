package org.prelle.telnet.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.prelle.telnet.event.DataEvent;
import org.prelle.telnet.event.TelnetEventFactory;
import org.prelle.telnet.event.internal.DefaultTelnetEventFactory;
import org.prelle.telnet.option.CommunicationRole;
import org.prelle.telnet.option.TelnetOption;

/**
 * 
 */
public class TelnetProtocolBuilder {
	
	private CommunicationRole role;
	private TelnetEventFactory factory = new DefaultTelnetEventFactory();
	private TelnetProtocolListener listener;
	private Consumer<DataEvent> dataConsumer;
	private List<TelnetOption> options;
	private TelnetReturnChannel returnChannel;

	//-------------------------------------------------------------------
	/**
	 */
	public TelnetProtocolBuilder(CommunicationRole role) {
		this.role = role;
		options = new ArrayList<>();
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withEventFactory(TelnetEventFactory factory) {
		this.factory = factory;
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withListener(TelnetProtocolListener listener) {
		this.listener = listener;
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withDataListener(Consumer<DataEvent> dataListener) {
		this.dataConsumer = dataListener;
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withOption(TelnetOption option) {
		Objects.requireNonNull(option);
		this.options.add(option);
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withOptions(TelnetOption... value) {
		for (TelnetOption option : value)
			this.options.add(option);
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocolBuilder withReturnChannel(TelnetReturnChannel returnChannel) {
		this.returnChannel = returnChannel;
		return this;
	}
	
	//-------------------------------------------------------------------
	public TelnetProtocol build() {
		return new TelnetProtocol(role, factory, listener, dataConsumer, options, returnChannel);
	}

}
