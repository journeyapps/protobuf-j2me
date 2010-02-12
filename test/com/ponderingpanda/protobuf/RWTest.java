/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ponderingpanda.protobuf;

import com.google.protobuf.*;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ralf
 */
public class RWTest {

    public RWTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void writeReadWrite() throws IOException {
        TestAllTypes o = new TestAllTypes();
        o.setOptionalBool(true);
        o.setOptionalBytes(new byte[] { 1,2,3 });
        o.setOptionalDouble(123.456);
        o.setOptionalFixed32(12345);
        o.setOptionalFixed64(1236478687345l);
        o.setOptionalFloat(123.45f);
        o.setOptionalForeignEnum(ForeignEnum.FOREIGN_BAR);
        o.setOptionalForeignMessage(new ForeignMessage());    //TODO
        o.setOptionalInt32(12345);
        o.setOptionalInt64(1236478687345l);
        o.setOptionalNestedEnum(TestAllTypes.NestedEnum.BAR);
        o.setOptionalNestedMessage(new TestAllTypes.NestedMessage()); //TODO
        o.setOptionalSfixed32(12345);
        o.setOptionalSfixed64(123456789123456l);
        o.setOptionalSint32(12345);
        o.setOptionalSint64(1234567891323l);
        o.setOptionalString("hjwehr uiowenrkjashdf waerhj osdjfiaojwerioj sdklf");
        o.setOptionalUint32(123456789);
        o.setOptionalUint64(12345678912345678l);

        byte[] bytes = Util.messageToBytes(o);

        TestAllTypes i = new TestAllTypes();
        Util.messageFromBytes(bytes, i);

        assertEquals(o.getOptionalBool(), i.getOptionalBool());
        assertArrayEquals(o.getOptionalBytes(), i.getOptionalBytes());
        assertEquals(o.getOptionalDouble(), i.getOptionalDouble(), 0);
        assertEquals(o.getOptionalFixed32(), i.getOptionalFixed32());
        assertEquals(o.getOptionalFixed64(), i.getOptionalFixed64());
        assertEquals(o.getOptionalFloat(), i.getOptionalFloat(), 0);
        assertEquals(o.getOptionalForeignEnum(), i.getOptionalForeignEnum());
//        assertEquals(o.getOptionalForeignMessage(), i.getOptionalForeignMessage());   //TODO
        assertEquals(o.getOptionalInt32(), i.getOptionalInt32());
        assertEquals(o.getOptionalInt64(), i.getOptionalInt64());
        assertEquals(o.getOptionalNestedEnum(), i.getOptionalNestedEnum());
//        assertEquals(o.getOptionalNestedMessage(), i.getOptionalNestedMessage()); //TODO
        assertEquals(o.getOptionalSfixed32(), i.getOptionalSfixed32());
        assertEquals(o.getOptionalSfixed64(), i.getOptionalSfixed64());
        assertEquals(o.getOptionalSint32(), i.getOptionalSint32());
        assertEquals(o.getOptionalSint64(), i.getOptionalSint64());
        assertEquals(o.getOptionalString(), i.getOptionalString());
        assertEquals(o.getOptionalUint32(), i.getOptionalUint32());
        assertEquals(o.getOptionalUint64(), i.getOptionalUint64());

        byte[] bytes2 = Util.messageToBytes(i);
        assertArrayEquals(bytes, bytes2);
    }

}