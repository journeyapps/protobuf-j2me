package com.ponderingpanda.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Ralf Kistner
 */
public class TestApp {
    public static void main(String[] args) throws IOException {
        TestMessage m = new TestMessage();
        m.nestedMessage = new TestMessage();
        m.nestedMessage.i = 1234235;
        m.nestedMessage.message = "inner";
        m.addFloat(444);
        m.addFloat(123);
        m.addFloat(123.234f);
        m.message = "Outer";

        System.out.println(m);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProtoUtil.writeDelimitedMessage(out, m);
        m.message = "Outer2";
        ProtoUtil.writeDelimitedMessage(out, m);
        m.message = "Outer3";
        ProtoUtil.writeDelimitedMessage(out, m);
        
        System.out.println(new String(out.toByteArray()));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        
        TestMessage min;

        min = new TestMessage();
        ProtoUtil.readDelimitedMessage(in, min);
        System.out.println(min);

        min = new TestMessage();
        ProtoUtil.readDelimitedMessage(in, min);
        System.out.println(min);

        min = new TestMessage();
        ProtoUtil.readDelimitedMessage(in, min);
        System.out.println(min);

        min = new TestMessage();
        ProtoUtil.readDelimitedMessage(in, min);
        System.out.println(min);
    }
}
