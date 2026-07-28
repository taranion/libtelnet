package org.prelle.telnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		System.out.println("Data length: " + data.length);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
		TelnetInputStream tis = new TelnetInputStream(bais, proto);
		
		byte[] buf = new byte[1024];
		int read = tis.read(buf);
		assertEquals(12, read, "Expected to read 12 bytes, but read "+ read);
		String received = new String(buf, 0, read, StandardCharsets.UTF_8);
		assertEquals(text, received);

		read = tis.read(buf);		
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
		System.out.println("Data length: " + data.length);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
		TelnetInputStream tis = new TelnetInputStream(bais, proto);
		tis.setSendGoAheadAsANSISepator(true);
		
		byte[] buf = new byte[1024];
		int read = tis.read(buf);
		// Expected to read 12 bytes + 1 byte for the ANSI separator plus a final data char, so 14 bytes in total
		assertEquals(14, read, "Read should have not been interrupted");
		String received = new String(buf, 0, read, StandardCharsets.UTF_8);
		String expected = "Hellö World\u001EA";
		assertEquals(expected, received);
		
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
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
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
		TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
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
