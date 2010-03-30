/*
 * Copyright Copyright (c) 2010, Pondering Panda
 * All rights reserved.
 *
 * See COPYING.txt for the complete copyright notice.
 *
 */
package com.ponderingpanda.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Ralf Kistner
 */
public class ProtoUtil {
    /** Only use this when this will be the only message written to the stream. */
    public static void writeMessage(OutputStream out, Message message) throws IOException {
        CodedOutputStream coded = new CodedOutputStream(out);
        message.serialize(coded);
    }

    /** Reads until the end of the stream. */
    public static void readMessage(InputStream in, Message message) throws IOException {
        CodedInputStream coded = new CodedInputStream(in);
        message.deserialize(coded);
    }

    public static void writeMessageWithSize(OutputStream out, Message message) throws IOException {
        CodedOutputStream coded = new CodedOutputStream(out);
        byte[] bytes = messageToBytes(message);
        coded.writeRawVarint32(bytes.length);
        out.write(bytes);
    }

    public static void writeDelimitedMessage(OutputStream out, Message message) throws IOException {
        CodedOutputStream coded = new CodedOutputStream(out);
        message.serialize(coded);
        coded.writeRawVarint32(0);
    }

    public static byte[] messageToBytes(Message message) throws EncodingException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CodedOutputStream coded = new CodedOutputStream(out);
            message.serialize(coded);
            return out.toByteArray();
        } catch(IOException e) {
            // We are using a ByteArrayOutputStream, this should not happen
            throw new EncodingException("IOException: " + e.getMessage());
        }
    }

    public static void messageFromBytes(byte[] in, Message message) throws EncodingException {
        CodedInputStream coded = new CodedInputStream(new ByteArrayInputStream(in));
        try {
            message.deserialize(coded);
        } catch(IOException e) {
            // We are using a ByteArrayInputStream, this should not happen
            throw new EncodingException("IOException: " + e.getMessage());
        }
    }

    public static void readMessageWithSize(InputStream in, Message message) throws IOException {
        CodedInputStream coded = new CodedInputStream(in);
        coded.readMessage(message);
    }

    public static void readDelimitedMessage(InputStream in, Message message) throws IOException {
        CodedInputStream coded = new CodedInputStream(in);
        message.deserialize(coded);
        if(coded.isEndOfStream())
            throw new IOException("End of stream");
    }
}
