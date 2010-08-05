/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ponderingpanda.protobuf;

import com.google.protobuf.ForeignEnum;
import com.google.protobuf.ForeignMessage;
import com.google.protobuf.TestAllTypes;
import org.junit.*;

import java.io.IOException;

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

    private void assertHasAllOptional(TestAllTypes o, boolean has) {
        assertEquals(has, o.hasOptionalBool());
        assertEquals(has, o.hasOptionalDouble());
        assertEquals(has, o.hasOptionalBytes());
        assertEquals(has, o.hasOptionalFixed32());
        assertEquals(has, o.hasOptionalFixed64());
        assertEquals(has, o.hasOptionalFloat());
        assertEquals(has, o.hasOptionalForeignEnum());
        assertEquals(has, o.hasOptionalForeignMessage());
        if(has)
            assertEquals(has, o.getOptionalForeignMessage().hasC());
        assertEquals(has, o.hasOptionalInt32());
        assertEquals(has, o.hasOptionalInt64());
        assertEquals(has, o.hasOptionalNestedEnum());
        assertEquals(has, o.hasOptionalNestedMessage());
        if(has)
            assertEquals(has, o.getOptionalNestedMessage().hasBb());
        assertEquals(has, o.hasOptionalSfixed32());
        assertEquals(has, o.hasOptionalSfixed64());
        assertEquals(has, o.hasOptionalSint32());
        assertEquals(has, o.hasOptionalSint64());
        assertEquals(has, o.hasOptionalString());
        assertEquals(has, o.hasOptionalUint32());
        assertEquals(has, o.hasOptionalUint64());
    }

    private void assertRepeatedSame(TestAllTypes o, TestAllTypes i) {
        assertEquals(o.getRepeatedBoolVector(), i.getRepeatedBoolVector());
        // TODO: test actual byte values
        assertEquals(o.getRepeatedBytesVector().size(), i.getRepeatedBytesVector().size());
        assertEquals(o.getRepeatedCordVector(), i.getRepeatedCordVector());
        assertEquals(o.getRepeatedDoubleVector(), i.getRepeatedDoubleVector());
        assertEquals(o.getRepeatedFixed32Vector(), i.getRepeatedFixed32Vector());
        assertEquals(o.getRepeatedFixed64Vector(), i.getRepeatedFixed64Vector());
        assertEquals(o.getRepeatedFloatVector(), i.getRepeatedFloatVector());
        assertEquals(o.getRepeatedForeignEnumVector(), i.getRepeatedForeignEnumVector());
        // TODO: test message contents
        assertEquals(o.getRepeatedForeignMessageVector().size(), i.getRepeatedForeignMessageVector().size());
        assertEquals(o.getRepeatedInt32Vector(), i.getRepeatedInt32Vector());
        assertEquals(o.getRepeatedInt64Vector(), i.getRepeatedInt64Vector());
        assertEquals(o.getRepeatedNestedEnumVector(), i.getRepeatedNestedEnumVector());
        // TODO: test message contents
        assertEquals(o.getRepeatedNestedMessageVector().size(), i.getRepeatedNestedMessageVector().size());
        assertEquals(o.getRepeatedSfixed32Vector(), i.getRepeatedSfixed32Vector());
        assertEquals(o.getRepeatedSfixed64Vector(), i.getRepeatedSfixed64Vector());
        assertEquals(o.getRepeatedSint32Vector(), i.getRepeatedSint32Vector());
        assertEquals(o.getRepeatedSint64Vector(), i.getRepeatedSint64Vector());
        assertEquals(o.getRepeatedStringVector(), i.getRepeatedStringVector());
        assertEquals(o.getRepeatedUint32Vector(), i.getRepeatedUint32Vector());
        assertEquals(o.getRepeatedUint64Vector(), i.getRepeatedUint64Vector());
    }

    @Test
    public void testNoRepeatedFields() {
        TestAllTypes i = new TestAllTypes();
        ProtoUtil.messageFromBytes(new byte[0], i);

        assertRepeatedSame(new TestAllTypes(), i);
    }

    @Test
    public void testAllRepeatedFields() {
        TestAllTypes o = new TestAllTypes();
        o.addRepeatedBool(true);
        o.addRepeatedBool(false);
        o.addRepeatedBool(false);
        o.addRepeatedBytes("123".getBytes());
        o.addRepeatedBytes("345".getBytes());
        o.addRepeatedCord("fgdfg");
        o.addRepeatedCord("$k34jfSD");
        o.addRepeatedDouble(3.14);
        o.addRepeatedDouble(3.145);
        o.addRepeatedDouble(3.1467);
        o.addRepeatedFixed32(370890423);
        o.addRepeatedFixed32(234890423);
        o.addRepeatedFixed64(370894042384098234l);
        o.addRepeatedFixed64(370554342384098234l);
        o.addRepeatedFloat(3.1543f);
        o.addRepeatedFloat(-43.33f);
        o.addRepeatedForeignEnum(ForeignEnum.FOREIGN_BAR);
        o.addRepeatedForeignEnum(ForeignEnum.FOREIGN_FOO);
        o.addRepeatedForeignEnum(ForeignEnum.FOREIGN_BAZ);
        o.addRepeatedForeignMessage(new ForeignMessage());
        o.addRepeatedForeignMessage(new ForeignMessage());
        o.addRepeatedForeignMessage(new ForeignMessage());
        o.addRepeatedInt32(445890423);
        o.addRepeatedInt32(44589523);
        o.addRepeatedInt64(2345344466456l);
        o.addRepeatedInt64(-234789251126l);
        o.addRepeatedNestedEnum(TestAllTypes.BAR);
        o.addRepeatedNestedEnum(TestAllTypes.BAZ);
        o.addRepeatedNestedEnum(TestAllTypes.FOO);
        o.addRepeatedNestedMessage(new TestAllTypes.NestedMessage());
        o.addRepeatedNestedMessage(new TestAllTypes.NestedMessage());
        o.addRepeatedSfixed32(42353456);
        o.addRepeatedSfixed32(-445353456);
        o.addRepeatedSfixed64(-442353442356l);
        o.addRepeatedSfixed64(54432353442356l);
        o.addRepeatedSint32(314324);
        o.addRepeatedSint64(3142344324l);
        o.addRepeatedString("One");
        o.addRepeatedString("Two");
        o.addRepeatedString("3");
        o.addRepeatedStringPiece("four");
        o.addRepeatedUint32(234234);
        o.addRepeatedUint64(234234234l);
        byte[] bytes = ProtoUtil.messageToBytes(o);

        TestAllTypes i = new TestAllTypes();
        ProtoUtil.messageFromBytes(bytes, i);

        assertRepeatedSame(o, i);
    }

    @Test
    public void testNoOptionalFields() {
        // No fields set
        TestAllTypes o = new TestAllTypes();
        byte[] bytes = ProtoUtil.messageToBytes(o);
        assertEquals(0, bytes.length);
        assertHasAllOptional(o, false);

        TestAllTypes i = new TestAllTypes();
        ProtoUtil.messageFromBytes(bytes, i);
        assertHasAllOptional(i, false);
    }

    @Test
    public void testAllOptionalFields() throws IOException {
        // All fields set
        TestAllTypes o = new TestAllTypes();
        o.setOptionalBool(true);
        o.setOptionalBytes(new byte[] { 1,2,3 });
        o.setOptionalDouble(123.456);
        o.setOptionalFixed32(12345);
        o.setOptionalFixed64(1236478687345l);
        o.setOptionalFloat(123.45f);
        o.setOptionalForeignEnum(ForeignEnum.FOREIGN_BAR);
        ForeignMessage foreign = new ForeignMessage();
        foreign.setC(64562);
        o.setOptionalForeignMessage(foreign);
        o.setOptionalInt32(12345);
        o.setOptionalInt64(1236478687345l);
        o.setOptionalNestedEnum(TestAllTypes.BAR);
        TestAllTypes.NestedMessage nested = new TestAllTypes.NestedMessage();
        nested.setBb(889345);
        o.setOptionalNestedMessage(nested);
        o.setOptionalSfixed32(12345);
        o.setOptionalSfixed64(123456789123456l);
        o.setOptionalSint32(12345);
        o.setOptionalSint64(1234567891323l);
        o.setOptionalString("hjwehr uiowenrkjashdf waerhj osdjfiaojwerioj sdklf");
        o.setOptionalUint32(123456789);
        o.setOptionalUint64(12345678912345678l);

        assertHasAllOptional(o, true);

        byte[] bytes = ProtoUtil.messageToBytes(o);

        TestAllTypes i = new TestAllTypes();
        ProtoUtil.messageFromBytes(bytes, i);

        assertHasAllOptional(i, true);
        
        assertEquals(o.getOptionalBool(), i.getOptionalBool());
        assertArrayEquals(o.getOptionalBytes(), i.getOptionalBytes());
        assertEquals(o.getOptionalDouble(), i.getOptionalDouble(), 0);
        assertEquals(o.getOptionalFixed32(), i.getOptionalFixed32());
        assertEquals(o.getOptionalFixed64(), i.getOptionalFixed64());
        assertEquals(o.getOptionalFloat(), i.getOptionalFloat(), 0);
        assertEquals(o.getOptionalForeignEnum(), i.getOptionalForeignEnum());
        assertEquals(o.getOptionalForeignMessage().getC(), i.getOptionalForeignMessage().getC());
        assertEquals(o.getOptionalInt32(), i.getOptionalInt32());
        assertEquals(o.getOptionalInt64(), i.getOptionalInt64());
        assertEquals(o.getOptionalNestedEnum(), i.getOptionalNestedEnum());
        assertEquals(o.getOptionalNestedMessage().getBb(), i.getOptionalNestedMessage().getBb());
        assertEquals(o.getOptionalSfixed32(), i.getOptionalSfixed32());
        assertEquals(o.getOptionalSfixed64(), i.getOptionalSfixed64());
        assertEquals(o.getOptionalSint32(), i.getOptionalSint32());
        assertEquals(o.getOptionalSint64(), i.getOptionalSint64());
        assertEquals(o.getOptionalString(), i.getOptionalString());
        assertEquals(o.getOptionalUint32(), i.getOptionalUint32());
        assertEquals(o.getOptionalUint64(), i.getOptionalUint64());

        byte[] bytes2 = ProtoUtil.messageToBytes(i);
        assertArrayEquals(bytes, bytes2);
    }

}