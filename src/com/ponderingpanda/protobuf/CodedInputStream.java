/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ponderingpanda.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author ralf
 */
public class CodedInputStream {
    private static final int DIRECT_READ_LIMIT = 20*1024;
    private InputStream in;

    private int currentTag = 0;

    public CodedInputStream(InputStream in) {
        this.in = in;
        advanceTag();
    }

    public long readInt64(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_VARINT))
            return 0;
        long value = readInt64();
        advanceTag();
        return value;
    }

    public int readInt32(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_VARINT))
            return 0;
        int value = readInt32();
        advanceTag();
        return value;
    }

    public long readUInt64(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_VARINT))
            return 0;
        long value = readUInt64();
        advanceTag();
        return value;
    }

    public int readUInt32(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_VARINT))
            return 0;
        int value = readUInt32();
        advanceTag();
        return value;
    }

    public long readSInt64(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_VARINT))
            return 0;
        long value = readSInt64();
        advanceTag();
        return value;
    }

    public int readSInt32(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_VARINT))
            return 0;
        int value = readSInt32();
        advanceTag();
        return value;
    }

    public float readFloat(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_FIXED32))
            return 0;
        float value = readFloat();
        advanceTag();
        return value;
    }

    public double readDouble(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_FIXED64))
            return 0;
        double value = readDouble();
        advanceTag();
        return value;
    }

    public String readString(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_LENGTH_DELIMITED))
            return "";
        String value = readString();
        advanceTag();
        return value;
    }

    public byte[] readBytes(int field) throws IOException {
        if(!skipTo(field, WireFormat.WIRETYPE_LENGTH_DELIMITED))
            return null;
        byte[] value = readBytes();
        advanceTag();
        return value;
    }

    public boolean readMessage(int field, Message message) throws IOException {
        //TODO: optimize
        byte[] bytes = readBytes(field);
        if(bytes == null)
            return false;
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        CodedInputStream stream = new CodedInputStream(bin);
        message.deserialize(stream);
        return true;
    }

    public boolean hasVarInt(int field) throws IOException {
        return skipTo(field, WireFormat.WIRETYPE_VARINT);
    }

    public boolean hasFixed32(int field) throws IOException {
        return skipTo(field, WireFormat.WIRETYPE_FIXED32);
    }

    public boolean hasFixed64(int field) throws IOException {
        return skipTo(field, WireFormat.WIRETYPE_FIXED64);
    }

    public boolean hasLengthDelimited(int field) throws IOException {
        return skipTo(field, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    }

    private boolean skipTo(int field, int wiretype) throws IOException {
        while(true) {
            if(currentTag == 0)
                return false;
            int cfield = WireFormat.getTagFieldNumber(currentTag);
            if(cfield > field)
                return false;
            if(cfield == field && WireFormat.getTagWireType(currentTag) == wiretype)
                return true;
            skipTag(currentTag);
            advanceTag();
        }
    }

    private void advanceTag() {
        try {
            currentTag = readTag();
        } catch(IOException e) {
            currentTag = 0;
        }
    }

	// -----------------------------------------------------------------

	/** Read a {@code double} field value from the stream. */
	private double readDouble() throws IOException {
		return Double.longBitsToDouble(readRawLittleEndian64());
	}

	/** Read a {@code float} field value from the stream. */
	private float readFloat() throws IOException {
		return Float.intBitsToFloat(readRawLittleEndian32());
	}

	/** Read an {@code int64} field value from the stream. */
	private long readInt64() throws IOException {
		return readRawVarint64();
	}

    private long readUInt64() throws IOException {
		return readRawVarint64();
	}

	/** Read an {@code int32} field value from the stream. */
	private int readInt32() throws IOException {
		return readRawVarint32();
	}

    /** Read an {@code uint32} field value from the stream. */
	private int readUInt32() throws IOException {
		return readRawVarint32();
	}

    private long readSInt64() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
	}

	/** Read an {@code int32} field value from the stream. */
	private int readSInt32() throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/** Read a {@code bool} field value from the stream. */
	private boolean readBool() throws IOException {
		return readRawVarint32() != 0;
	}

	/** Read a {@code string} field value from the stream. */
	private String readString() throws IOException {
		byte[] bytes = readBytes();
        return new String(bytes, "UTF-8");
	}

	/** Read a {@code bytes} field value from the stream. */
	private byte[] readBytes() throws IOException {
		int size = readRawVarint32();
        int off = 0;
        if(size < DIRECT_READ_LIMIT) {
            // If the size is small enough, we read it directly
            byte[] bytes = new byte[size];
            //TODO: limit size
            while(off < size) {
                int read = in.read(bytes, off, size - off);
                if(read < 0)
                    throw new IOException("Unexpected end of stream");
                off += read;
            }
            return bytes;
        } else {
            // If the size is large, we use a buffer, so we don't waste space
            // if there isn't actually that much data
            ByteArrayOutputStream out = new ByteArrayOutputStream(DIRECT_READ_LIMIT);
            byte[] buffer = new byte[DIRECT_READ_LIMIT];
            while(off < size) {
                int read = in.read(buffer);
                if(read < 0)
                    throw new IOException("Unexpected end of stream");
                off += read;
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
	}

    private int readTag() throws IOException {
        int tag = readRawVarint32();
        if(tag == 0)
            throw new IOException("Invalid tag");
        return tag;
    }

    private void skipTag(int tag) throws IOException {
        if(tag == 0)
            return;
        final int dataType = WireFormat.getTagWireType(tag);
		switch (dataType) {
		case WireFormat.WIRETYPE_FIXED32:
			readFloat();
			break;
		case WireFormat.WIRETYPE_FIXED64:
			readDouble();
			break;
		case WireFormat.WIRETYPE_LENGTH_DELIMITED:
            //TODO: skip the bytes instead of reading
			readBytes();
			break;
		case WireFormat.WIRETYPE_VARINT:
			readRawVarint64();
			break;
		default:
			break;
		}
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