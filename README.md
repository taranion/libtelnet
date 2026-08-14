This Java library provides ways to 

* write a socket-based Telnet server  or client
* write an event-based Telnet Server or client

## Events

This library works with two kinds of events

* On-Wire events (``TelnetEvent``)

  * TelnetCommand
    Every single-byte command that has been prefixed by an IAC
  * TelnetNegotiationEvent
    These are WILL/WONT/DO/DONT events
  * TelnetSubnegotiationEvent
    An event for content that starts with IAC SB and ends with IAC SE
  * DataEvent
    This is the user data transported in the stream.

* Protocol events (`TelnetOptionEvent`)

  * SubnegotiationFinshedEvent

    This event is thrown when all configured options of the stack have been either rejected or successfully activated (and eventually finished a subnegotiation)

The events are described as Java interfaces. The library has a default implementation, which can be replaced by setting a `TelnetEventFactory`.

## Low-Level Encoding/Decoding

The relevant classes for encoding or decoding telnet content are the `TelnetEncoder` and `TelnetDecoder` classes.

```java
List<TelnetEvent> parsed = new ArrayList<>();
TelnetDecoder decoder = new TelnetDecoder( new TelnetParserListener() {
    public void onTelnetEvent(TelnetEvent event) {
        parsed.add(event);
    }
});

byte[]  rawData = ...;
decoder.process(rawData);
parsed.forEach(ev -> System.out.println("Decoded "+ev));
```

```java
byte[] rawData = TelnetEncoder.encodeEvent(event);
```

## Medium-Level Option Negotiation

The class `TelnetProtocol` is the central component for handling Telnet extension negotiations. You can configure it with the builder pattern and some elements like Telnet Options or listeners can also get set later.

```java
TelnetProtocol telnet = TelnetProtocol.builder(CommunicationRole.SERVER)
	.withEventFactory(...)
	.withListener(new TelnetProtocolListener(){..})
	.withDataListener( data -> {})
	.withOption(new TelnetWindowSize())
	.withOption(new TelnetCharset("UTF-8","ISO-8859-1"))
    .withReturnChannel( ... )
    ;
byte[]  rawData = ...;
telnet.process(rawData);

```

It works like the TelnetDecoder: Once configured you call the `process` method to inject a buffer to parse and the listeners get called with the results.

To be able to send responses in the negotiations, you need to configure the `TelnetReturnChannel` - a functional interface that processes `TelnetEvents`.

## Writing a socket based Telnet server

The basic setup works comparable to a regular socket.

```java
TelnetServerSocket servSock = new TelnetServerSocket(4000);
// Configure all sockets with extensions
socket.setExtensionFactory( socket -> List.of(
    new TelnetWindowSize(),
    new TelnetCharset("UTF-8","ISO-8859-1")
	));
// Set a listener for negotiation results

Thread waitIncoming = new Thread( () -> {
	try {
		Socket socket = servSock.accept();
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
	} catch (IOException ioe) {
	...
	}
});
waitIncoming.start();
```

The streams would only send and receive data, so while it would filter out telnet commands, this setup does not expose them. To see them you need to give the socket a listener.

### Listening to Telnet events

The `TelnetSocketListener` interface requires 3 methods

* **optionStateChanged**
  Called whenever a telnet option gets enabled or disabled

* **onTelnetEvent**
  Called for telnet commands, like GA (Go Aread)

* **telnetReady**

  Called when all configured options have been negotiated.

```
TelnetServerSocket servSock = new TelnetServerSocket(4000);
servSocket.setListenerFactory( socket ->)

Thread waitIncoming = new Thread( () -> {
	try {
		Socket socket = servSock.accept();
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
	} catch (IOException ioe) {
	...
	}
});
waitIncoming.start();
```

