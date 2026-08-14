package org.prelle.telnet.event;

public interface DataEvent extends TelnetEvent {

	byte[] getData();

}