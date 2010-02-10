/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ponderingpanda.protobuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author ralf
 */
public class CodedOutputStream {
    private CodedOutputStream nestedStream;
    private ByteArrayOutputStream nestedOut;
    private OutputStream out;

    public CodedOutputStream(OutputStream out) throws IOException {
        this.out = out;
    }
    
    public void writeInt64(int field, long value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_VARINT);
		writeRawVarint64(value);
    }
    
    public void writeInt32(int field, int value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_VARINT);
		if (value >= 0) {
			writeRawVarint32(value);
		} else {
			// Must sign-extend.
			writeRawVarint64(value);
		}
    }

    public void writeUInt64(int field, long value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_VARINT);
        writeRawVarint64(value);
    }

    public void writeUInt32(int field, int value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_VARINT);
        writeRawVarint32(value);
    }

    public void writeSInt64(int field, long value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_VARINT);
        writeRawVarint64((value << 1) ^ (value >> 63));
    }
    
    public void writeSInt32(int field, int value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_VARINT);
        writeRawVarint32((value << 1) ^ (value >> 31));
    }

    public void writeFloat(int field, float value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_FIXED32);
		writeRawLittleEndian32(Float.floatToIntBits(value));
    }

    public void writeDouble(int field, double value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_FIXED64);
		writeRawLittleEndian64(Double.doubleToLongBits(value));
    }

    public void writeBool(int field, boolean value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_VARINT);
		out.write(value ? 1 : 0);
    }

    public void writeString(int field, String value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED);
		// Unfortunately there does not appear to be any way to tell Java to
		// encode
		// UTF-8 directly into our buffer, so we have to let it create its own
		// byte
		// array and then copy.
		byte[] bytes = value.getBytes("UTF-8");
		writeRawVarint32(bytes.length);
		out.write(bytes);
	}

    public void writeBytes(int field, byte[] value) throws IOException {
        writeTag(field, WireFormat.WIRETYPE_LENGTH_DELIMITED);
		writeRawVarint32(value.length);
        out.write(value);
    }

    public void writeInt32s(int field, int[] values) throws IOException {
        for(int i = 0; i < values.length; i++) {
            writeInt32(field, values[i]);
        }
    }

    public void writeInt64s(int field, long[] values) throws IOException {
        for(int i = 0; i < values.length; i++) {
            writeInt64(field, values[i]);
        }
    }

    public void writeMessage(int field, Message message) throws IOException {
        if(nestedStream == null) {
            nestedOut = new ByteArrayOutputStream();
            nestedStream = new CodedOutputStream(out);
        }
        message.serialize(nestedStream);
        writeBytes(field, nestedOut.toByteArray());
        nestedOut.reset();
    }

	/** Encode and write a tag. */
	private void writeTag(int fieldNumber, int wireType) throws IOException {
		writeRawVarint32(WireFormat.makeTag(fieldNumber, wireType));
	}

    /**
	 * Encode and write a varint. {@code value} is treated as unsigned, so it
	 * won't be sign-extended if negative.
	 */
	private void writeRawVarint32(int value) throws IOException {
		while (true) {
			if ((value & ~0x7F) == 0) {
				out.write(value);
				return;
			} else {
				out.write((value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

    /** Encode and write a varint. */
	private void writeRawVarint64(long value) throws IOException {
		while (true) {
			if ((value & ~0x7FL) == 0) {
				out.write((int) value);
				return;
			} else {
				out.write(((int) value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

    /** Write a little-endian 32-bit integer. */
	private void writeRawLittleEndian32(int value) throws IOException {
		out.write((value) & 0xFF);
		out.write((value >> 8) & 0xFF);
		out.write((value >> 16) & 0xFF);
		out.write((value >> 24) & 0xFF);
	}

    /** Write a little-endian 64-bit integer. */
	private void writeRawLittleEndian64(long value) throws IOException {
		out.write((int) (value) & 0xFF);
		out.write((int) (value >> 8) & 0xFF);
		out.write((int) (value >> 16) & 0xFF);
		out.write((int) (value >> 24) & 0xFF);
		out.write((int) (value >> 32) & 0xFF);
		out.write((int) (value >> 40) & 0xFF);
		out.write((int) (value >> 48) & 0xFF);
		out.write((int) (value >> 56) & 0xFF);
	}
}
