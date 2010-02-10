/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ponderingpanda.protobuf;

import java.io.IOException;

/**
 *
 * @author ralf
 */
public class TestMessage implements Message {
    public float f = 0;
    public int i = 0;
    private TestMessage nestedMessage;
    
    public void serialize(CodedOutputStream out) throws IOException {
        out.writeInt32(3, i);
        out.writeFloat(5, f);
    }

    public void deserialize(CodedInputStream in) throws IOException {
        i = in.readInt32(3);
        if(in.hasFixed32(5))
            f = in.readFloat(5);
        else
            f = 123;
    }
}
