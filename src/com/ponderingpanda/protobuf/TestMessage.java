
package com.ponderingpanda.protobuf;

import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author Ralf Kistner
 */
public class TestMessage implements Message {
    public Vector f = new Vector();
    public int i = 0;
    public TestMessage nestedMessage;
    public String message = "hallo";
    public boolean b = false;
    

    public String toString() {
        return message + "," + f + "," + i + ",{" + nestedMessage + "}";
    }

    public int floatSize() {
        return f.size();
    }
    
    public float getFloat(int i) {
        return ((Float)f.elementAt(i)).floatValue();
    }

    public void addFloat(float f) {
        this.f.addElement(new Float(f));
    }

    public Vector getFloatVector() {
        return f;
    }
    
    public void serialize(CodedOutputStream out) throws IOException {
        out.writeInt32(3, i);
        for(int i = 0; i < floatSize(); i++) {
            out.writeFloat(5, getFloat(i));
        }
        out.writeString(2, "Some random data");
        out.writeMessage(6, nestedMessage);
        out.writeString(7, message);
    }

    public void deserialize(CodedInputStream in) throws IOException {
        while(true) {
            int tag = in.readTag();
            switch(tag) {
                case 0:
                    return;
                case (3 << 3) | WireFormat.WIRETYPE_VARINT:
                    i = in.readInt32();
                    break;
                case (5 << 3) | WireFormat.WIRETYPE_FIXED32:
                    addFloat(in.readFloat());
                    break;
                case (6 << 3) | WireFormat.WIRETYPE_LENGTH_DELIMITED:
                    nestedMessage = new TestMessage();
                    in.readMessage(nestedMessage);
                    break;
                case (7 << 3) | WireFormat.WIRETYPE_LENGTH_DELIMITED:
                    message = in.readString();
                    break;
                default:
                    in.skipTag(tag);
            }
        }           
    }
}
