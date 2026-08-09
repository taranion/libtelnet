package org.prelle.telnet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.prelle.telnet.event.TelnetEvent;
import org.prelle.telnet.option.CommunicationRole;
import org.prelle.telnet.option.TelnetOption;
import org.prelle.telnet.option.TelnetProtocol;
import org.prelle.telnet.option.TelnetProtocolListener;
import org.prelle.telnet.parser.TelnetConstants;

/**
 * 
 */
class InputStreamTest {

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
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetProtocolListener list = new TelnetProtocolListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
			@Override
			public void optionStateChanged(TelnetOption extension, boolean active) {
			}
			@Override
			public void telnetReady() { }
			};
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT, list);
		TelnetInputStream tis = new TelnetInputStream(bais, proto);
		
		byte[] buf = new byte[1024];
		int read = tis.read(buf);
		assertEquals(12, read, "Expected to read 12 bytes, but read "+ read);
		String received = new String(buf, 0, read, StandardCharsets.UTF_8);
		assertEquals(text, received);
		assertEquals(1,commandsReceived.size(), "Expected IAC DO  "+commandsReceived);

		read = tis.read(buf);		
		assertEquals(1,commandsReceived.size(), "Expected IAC GA (IAC DO ignored), but got "+commandsReceived);
		assertEquals(1, read);
		
		assertEquals(-1, tis.read(buf), "Expected end of stream");
		
		tis.close();
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
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetProtocolListener list = new TelnetProtocolListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
			@Override
			public void optionStateChanged(TelnetOption extension, boolean active) {
			}
			@Override
			public void telnetReady() { }
			};
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT, list);
		TelnetInputStream tis = new TelnetInputStream(bais, proto);
		tis.setSendGoAheadAsANSISepator(true);
		
		byte[] buf = new byte[1024];
		int read = tis.read(buf);
		// Expected to read 12 bytes + 1 byte for the ANSI separator, so 13 bytes first
		assertEquals(13, read, "First read should return data up to and including RS");
		String received = new String(buf, 0, read, StandardCharsets.UTF_8);
		String expected = "Hellö World\u001E";
		assertEquals(expected, received);
		assertEquals(0,commandsReceived.size(), "Expected (IAC DO) to be ignored");

		// Next read should yield the remaining trailing data char
		read = tis.read(buf);
		assertEquals(1, read);
		assertEquals('A', buf[0]);

		assertEquals(-1, tis.read(buf), "Expected end of stream");
		
		tis.close();
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
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetProtocolListener list = new TelnetProtocolListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
			@Override
			public void optionStateChanged(TelnetOption extension, boolean active) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void telnetReady() {
				// TODO Auto-generated method stub
				
			}
			};
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT, list);
		TelnetInputStream tis = new TelnetInputStream(bais, proto);

		byte[] buf = new byte[32];
		int read = tis.read(buf);
		// Expect 3 data bytes: 'A', single 255 (from doubled IAC), 'B'
		assertEquals(3, read, "Expected to read 3 data bytes after filtering commands");
		// Compare unsigned values to avoid signed byte issues
		assertEquals((int) 'A', buf[0] & 0xFF);
		assertEquals(255, buf[1] & 0xFF);
		assertEquals((int) 'B', buf[2] & 0xFF);

		// Ensure end of stream afterwards
		assertEquals(-1, tis.read(buf), "Expected end of stream");
		tis.close();
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
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		List<TelnetEvent> commandsReceived = new ArrayList<>();
		TelnetProtocolListener list = new TelnetProtocolListener() {
			@Override
			public void onTelnetEvent(TelnetEvent event) { commandsReceived.add(event); }
			@Override
			public void optionStateChanged(TelnetOption extension, boolean active) { }
			@Override
			public void telnetReady() {	}
		};
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT, list);
		TelnetInputStream tis = new TelnetInputStream(bais, proto);

		byte[] buf = new byte[1024];
		int read = tis.read(buf);

		byte[] p1 = part1.getBytes(StandardCharsets.UTF_8);
		byte[] p2 = part2.getBytes(StandardCharsets.UTF_8);
		int expected = p1.length + 1 + p2.length; // +1 for the decoded single 255
		assertEquals(expected, read, "Expected combined length of multibyte + single IAC + trailing text");

		// verify first part as UTF-8 string
		String gotPart1 = new String(buf, 0, p1.length, StandardCharsets.UTF_8);
		assertEquals(part1, gotPart1);
		// verify single 255 present
		assertEquals(255, buf[p1.length] & 0xFF);
		// verify trailing ASCII
		String gotPart2 = new String(buf, p1.length + 1, p2.length, StandardCharsets.UTF_8);
		assertEquals(part2, gotPart2);

		assertEquals(-1, tis.read(buf), "Expected end of stream");
		tis.close();
	}

}
