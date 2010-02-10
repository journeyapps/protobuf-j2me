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
        m.nestedMessage.message = null;
        m.addFloat(444);
        m.addFloat(123);
        m.addFloat(123.234f);
        m.message = "Outer";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CodedOutputStream stream = new CodedOutputStream(out);
        m.serialize(stream);
        System.out.println(m);
        System.out.println(new String(out.toByteArray()));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        CodedInputStream cin = new CodedInputStream(in);

        TestMessage m2 = new TestMessage();
        m2.deserialize(cin);
        System.out.println(m2);
    }
}
