package org.prelle.telnet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.prelle.telnet.event.DataEvent;
import org.prelle.telnet.event.TelnetCommand;
import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.event.TelnetNegotiationEvent;
import org.prelle.telnet.event.TelnetParserListener;
import org.prelle.telnet.parser.TelnetConstants;
import org.prelle.telnet.parser.TelnetStateMachine;

/**
 * 
 */
class ParserTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}
	

	//-------------------------------------------------------------------
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	//-------------------------------------------------------------------
	@Test
	void testWithoutANSISeparator() throws IOException {
		String text = "Hellö World";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(TelnetConstants.ControlCode.IAC.code());
		baos.write(TelnetConstants.ControlCode.DO.code());
		baos.write(1);
		baos.write(text.getBytes(StandardCharsets.UTF_8));
		baos.write(TelnetConstants.ControlCode.IAC.code());
		baos.write(TelnetConstants.ControlCode.GA.code());
		baos.write(65);
		
		byte[] data = baos.toByteArray();
		
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetParserListener listener = new TelnetParserListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
		};
		TelnetStateMachine parser = new TelnetStateMachine(listener);
		// Test push processing
		parser.process(data);
		// Should have 4 events
		assertEquals(4, commandsReceived.size(), "Expected 4 events, but got "+commandsReceived);
		assertEquals( TelnetNegotiationEvent.class, commandsReceived.get(0).getClass());
		assertEquals(              DataEvent.class, commandsReceived.get(1).getClass());
		assertEquals(          TelnetCommand.class, commandsReceived.get(2).getClass());
		assertEquals(              DataEvent.class, commandsReceived.get(3).getClass());
	}

	//-------------------------------------------------------------------
	@Test
	void testWithANSISeparator() throws IOException {
		String text = "Hellö World";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(TelnetConstants.ControlCode.IAC.code());
		baos.write(TelnetConstants.ControlCode.DO.code());
		baos.write(1);
		baos.write(text.getBytes(StandardCharsets.UTF_8));
		baos.write(TelnetConstants.ControlCode.IAC.code());
		baos.write(TelnetConstants.ControlCode.GA.code());
		baos.write(65);
		
		byte[] data = baos.toByteArray();
		System.out.println("Data length: " + data.length);
		
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetParserListener listener = new TelnetParserListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
		};
		TelnetStateMachine parser = new TelnetStateMachine(listener);
		parser.setSendGoAheadAsANSISepator(true);
		// Test push processing
		parser.process(data);
		
		// Should have 
		// 1. TelnetNegotiationEvent for IAC DO 1
		// 2. DataEvent for "Hellö World" + RS
		// 3. DataEvent for 'A'
		assertEquals(3, commandsReceived.size(), "Telnet GA not converted to data or not two data events: "+commandsReceived);
	}

	//-------------------------------------------------------------------
	/**
	 * Verify that a Telnet command sequence (IAC DO 1) is not treated as data
	 * and that a doubled IAC (255,255) in the data is converted to a single
	 * data byte 255.
	 */
	@Test
	void testIACCommandIgnoredAndDoubledIACDecoded() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// Send a telnet command sequence that should NOT become data
		baos.write(TelnetConstants.ControlCode.IAC.code());
		baos.write(TelnetConstants.ControlCode.DO.code());
		baos.write(1);
		// Then send data: 'A', doubled IAC (-> single 255), 'B'
		baos.write((int) 'A');
		baos.write(255); // IAC as data (escaped)
		baos.write(255); // IAC as data (escaped)
		baos.write((int) 'B');

		byte[] data = baos.toByteArray();
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetParserListener listener = new TelnetParserListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
		};
		TelnetStateMachine parser = new TelnetStateMachine(listener);
		// Test push processing
		parser.process(data);
		// Should have 4 events
		assertEquals(2, commandsReceived.size(), "Expected two events but got "+commandsReceived);
		assertEquals( TelnetNegotiationEvent.class, commandsReceived.get(0).getClass());
		assertEquals(              DataEvent.class, commandsReceived.get(1).getClass());
		
		DataEvent dataEvent = (DataEvent) commandsReceived.get(1);
		assertEquals(3, dataEvent.getData().length);
	}

	//-------------------------------------------------------------------
	/**
	 * Verify that multibyte UTF-8 data is preserved and that an escaped IAC
	 * (255,255) inside the data is decoded to a single 255 byte.
	 */
	@Test
	void testMultibyteUtf8WithEscapedIAC() throws IOException {
		String part1 = "Hellö 世界"; // contains multibyte characters
		String part2 = "End";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// a leading telnet command that should be filtered out
		baos.write(TelnetConstants.ControlCode.IAC.code());
		baos.write(TelnetConstants.ControlCode.DO.code());
		baos.write(1);
		// write multibyte text
		baos.write(part1.getBytes(StandardCharsets.UTF_8));
		// escaped IAC -> should become single 255 in data
		baos.write(255);
		baos.write(255);
		// trailing ASCII
		baos.write(part2.getBytes(StandardCharsets.UTF_8));

		byte[] data = baos.toByteArray();
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetParserListener listener = new TelnetParserListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
		};
		TelnetStateMachine parser = new TelnetStateMachine(listener);
		// Test push processing
		parser.process(data);
		// Should have 4 events
		assertEquals(2, commandsReceived.size(), "Expected two events but got "+commandsReceived);
		assertEquals( TelnetNegotiationEvent.class, commandsReceived.get(0).getClass());
		assertEquals(              DataEvent.class, commandsReceived.get(1).getClass());
		
		DataEvent dataEvent = (DataEvent) commandsReceived.get(1);
		int expected = part1.getBytes(StandardCharsets.UTF_8).length + 1 + part2.getBytes(StandardCharsets.UTF_8).length;
		assertEquals(expected, dataEvent.getData().length);
	}

}
