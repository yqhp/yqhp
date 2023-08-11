package com.yqhp.common.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * copy from <a href="https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/main/java/com/github/sarxos/webcam/util/MjpegInputStream.java">...</a> MIT license
 *
 * @author Bartosz Firyn (sarxos)
 */
public class MjpegInputStream extends DataInputStream {

    private static final Logger LOG = LoggerFactory.getLogger(MjpegInputStream.class);

    /**
     * The first two bytes of every JPEG frame are the Start Of Image (SOI) marker values FFh D8h.
     */
    private final byte[] SOI_MARKER = {(byte) 0xFF, (byte) 0xD8};

    /**
     * All JPEG data streams end with the End Of Image (EOI) marker values FFh D9h.
     */
    private final byte[] EOI_MARKER = {(byte) 0xFF, (byte) 0xD9};

    /**
     * Name of content length header.
     */
    private final String CONTENT_LENGTH = "Content-Length".toLowerCase();

    /**
     * Maximum header length.
     */
    private final static int HEADER_MAX_LENGTH = 100;

    /**
     * Max frame length (100kB).
     */
    private final static int FRAME_MAX_LENGTH = 100000 + HEADER_MAX_LENGTH;

    public MjpegInputStream(final InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }

    private int getEndOfSeqeunce(final DataInputStream in, final byte[] sequence) throws IOException {
        int s = 0;
        byte c;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if (c == sequence[s]) {
                s++;
                if (s == sequence.length) {
                    return i + 1;
                }
            } else {
                s = 0;
            }
        }
        return -1;
    }

    private int getStartOfSequence(final DataInputStream in, final byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return end < 0 ? -1 : end - sequence.length;
    }

    private int parseContentLength(final byte[] headerBytes) throws IOException, NumberFormatException {

        try (
                final ByteArrayInputStream bais = new ByteArrayInputStream(headerBytes);
                final InputStreamReader isr = new InputStreamReader(bais);
                final BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().startsWith(CONTENT_LENGTH)) {
                    final String[] parts = line.split(":");
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[1].trim());
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Read single MJPEG frame (JPEG image) from stream.
     */
    public byte[] readFrame() throws IOException {
        mark(FRAME_MAX_LENGTH);

        int n = getStartOfSequence(this, SOI_MARKER);

        reset();

        final byte[] header = new byte[n];

        readFully(header);

        int length;
        try {
            length = parseContentLength(header);
        } catch (NumberFormatException e) {
            length = getEndOfSeqeunce(this, EOI_MARKER);
        }

        if (length == 0) {
            LOG.error("Invalid MJPEG stream, EOI (0xFF,0xD9) not found!");
        }

        reset();

        final byte[] frame = new byte[length];

        skipBytes(n);

        readFully(frame);

        return frame;
    }
}