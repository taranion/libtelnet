package org.prelle.telnet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TelnetOutputStream} encoding behavior.
 */
class OutputStreamTest {

    @Test
    void testEscapeSingleIAC() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
        TelnetOutputStream tos = new TelnetOutputStream(baos, proto);

        byte[] input = new byte[] { (byte) 'A', (byte) 0xFF, (byte) 'B' };
        tos.write(input);
        tos.close();

        byte[] out = baos.toByteArray();
        // Expect the single 0xFF to be doubled
        byte[] expected = new byte[] { (byte) 'A', (byte) 0xFF, (byte) 0xFF, (byte) 'B' };
        assertArrayEquals(expected, out);
    }

    @Test
    void testInjectCRBeforeLF() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
        TelnetOutputStream tos = new TelnetOutputStream(baos, proto);

        // Default is injectCRBeforeLF = true
        byte[] input = new byte[] { (byte) 'A', (byte) '\n', (byte) 'B' };
        tos.write(input);
        tos.close();

        byte[] out = baos.toByteArray();
        byte[] expected = new byte[] { (byte) 'A', (byte) '\r', (byte) '\n', (byte) 'B' };
        assertArrayEquals(expected, out);
    }

    @Test
    void testInjectCRBeforeLFDisabled() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
        TelnetOutputStream tos = new TelnetOutputStream(baos, proto);
        tos.setInjectCRBeforeLF(false);

        byte[] input = new byte[] { (byte) 'A', (byte) '\n', (byte) 'B' };
        tos.write(input);
        tos.close();

        byte[] out = baos.toByteArray();
        byte[] expected = new byte[] { (byte) 'A', (byte) '\n', (byte) 'B' };
        assertArrayEquals(expected, out);
    }

    @Test
    void testMultibyteUtf8AndIAC() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
        TelnetOutputStream tos = new TelnetOutputStream(baos, proto);

        String p1 = "Hellö 世界";
        String p2 = "End";
        byte[] b1 = p1.getBytes(StandardCharsets.UTF_8);
        byte[] b2 = p2.getBytes(StandardCharsets.UTF_8);

        byte[] input = new byte[b1.length + 1 + b2.length];
        System.arraycopy(b1, 0, input, 0, b1.length);
        input[b1.length] = (byte) 0xFF; // a single IAC in the middle
        System.arraycopy(b2, 0, input, b1.length + 1, b2.length);

        tos.write(input);
        tos.close();

        byte[] out = baos.toByteArray();
        // Expect one extra byte for the doubled 0xFF
        assertEquals(b1.length + 1 + b2.length + 1, out.length);

        // first part equals original b1
        for (int i = 0; i < b1.length; i++) {
            assertEquals(b1[i], out[i]);
        }
        // check doubled 0xFF
        int idx = b1.length;
        assertEquals((byte) 0xFF, out[idx]);
        assertEquals((byte) 0xFF, out[idx + 1]);
        // check trailing bytes
        for (int i = 0; i < b2.length; i++) {
            assertEquals(b2[i], out[idx + 2 + i]);
        }
    }

    @Test
    void testWriteCommandNotEncoded() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TelnetProtocol proto = new TelnetProtocol(CommunicationRole.CLIENT);
        TelnetOutputStream tos = new TelnetOutputStream(baos, proto);

        byte[] cmd = new byte[] { (byte) 0xFF, (byte) 0xFA, (byte) 0x01, (byte) 0xFF };
        tos.writeCommand(cmd);
        tos.close();

        byte[] out = baos.toByteArray();
        // writeCommand should write the bytes unchanged
        assertArrayEquals(cmd, out);
    }

}
