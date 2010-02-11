/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ponderingpanda.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author ralf
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
        Util.writeDelimitedMessage(out, m);
        m.message = "Outer2";
        Util.writeDelimitedMessage(out, m);
        m.message = "Outer3";
        Util.writeDelimitedMessage(out, m);
        
        System.out.println(new String(out.toByteArray()));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        
        TestMessage min;

        min = new TestMessage();
        Util.readDelimitedMessage(in, min);
        System.out.println(min);

        min = new TestMessage();
        Util.readDelimitedMessage(in, min);
        System.out.println(min);

        min = new TestMessage();
        Util.readDelimitedMessage(in, min);
        System.out.println(min);

        min = new TestMessage();
        Util.readDelimitedMessage(in, min);
        System.out.println(min);
    }
}
