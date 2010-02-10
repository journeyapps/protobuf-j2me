/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ponderingpanda.protobuf;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author ralf
 */
public class CodedInputStream {
    private InputStream in;

    public CodedInputStream(InputStream in) {
        this.in = in;
    }

    public long readInt64(int field) {
        if(!findTag(field))
            return 0;
        return 0;
    }

    public int readInt32(int field) {
        return 0;
    }

    public long readUInt64(int field) {
        return 0;
    }

    public int readUInt32(int field) {
        return 0;
    }

    public long readSInt64(int field) {
        return 0;
    }

    public int readSInt32(int field) {
        return 0;
    }

    public float readFloat(int field) {
        return 0;
    }

    public double readDouble(int field) {
        return 0;
    }

    public String readString(int field) {
        return "";
    }

    public byte[] readBytes(int field) {
        return new byte[0];
    }

    public boolean findTag(int field) {
        return false;
    }

    /**
	 * Reads a varint from the input one byte at a time, so that it does not
	 * read any bytes after the end of the varint. If you simply wrapped the
	 * stream in a CodedInputStream and used
	 * {@link #readRawVarint32(InputStream)} then you would probably end up
	 * reading past the end of the varint since CodedInputStream buffers its
	 * input.
	 */
	private static int readRawVarint32(InputStream input) throws IOException {
		int result = 0;
		int offset = 0;
		for (; offset < 32; offset += 7) {
			int b = input.read();
			if (b == -1) {
				throw new IOException("Malformed varint");
			}
			result |= (b & 0x7f) << offset;
			if ((b & 0x80) == 0) {
				return result;
			}
		}
		// Keep reading up to 64 bits.
		for (; offset < 64; offset += 7) {
			int b = input.read();
			if (b == -1) {
				throw new IOException("Truncated message");
			}
			if ((b & 0x80) == 0) {
				return result;
			}
		}
		throw new IOException("Malformed varint");
	}

	// =================================================================

    private byte readRawByte() throws IOException {
        int b = in.read();
        if(b == -1)
            throw new IOException("End of stream");
        return (byte)b;
    }

	/**
	 * Read a raw Varint from the stream. If larger than 32 bits, discard the
	 * upper bits.
	 */
	private int readRawVarint32() throws IOException {
		byte tmp = readRawByte();
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = readRawByte()) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = readRawByte()) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = readRawByte()) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = readRawByte()) << 28;
					if (tmp < 0) {
						// Discard upper 32 bits.
						for (int i = 0; i < 5; i++) {
							if (readRawByte() >= 0)
								return result;
						}
						throw new IOException("Malformed varint");
					}
				}
			}
		}
		return result;
	}

	/** Read a raw Varint from the stream. */
	long readRawVarint64() throws IOException {
		int shift = 0;
		long result = 0;
		while (shift < 64) {
			byte b = readRawByte();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0)
				return result;
			shift += 7;
		}
		throw new IOException("Malformed varint");
	}

	/** Read a 32-bit little-endian integer from the stream. */
	int readRawLittleEndian32() throws IOException {
		byte b1 = readRawByte();
		byte b2 = readRawByte();
		byte b3 = readRawByte();
		byte b4 = readRawByte();
		return (((int) b1 & 0xff)) | (((int) b2 & 0xff) << 8) | (((int) b3 & 0xff) << 16) | (((int) b4 & 0xff) << 24);
	}

	/** Read a 64-bit little-endian integer from the stream. */
	long readRawLittleEndian64() throws IOException {
		byte b1 = readRawByte();
		byte b2 = readRawByte();
		byte b3 = readRawByte();
		byte b4 = readRawByte();
		byte b5 = readRawByte();
		byte b6 = readRawByte();
		byte b7 = readRawByte();
		byte b8 = readRawByte();
		return (((long) b1 & 0xff)) | (((long) b2 & 0xff) << 8) | (((long) b3 & 0xff) << 16)
				| (((long) b4 & 0xff) << 24) | (((long) b5 & 0xff) << 32) | (((long) b6 & 0xff) << 40)
				| (((long) b7 & 0xff) << 48) | (((long) b8 & 0xff) << 56);
	}
}
