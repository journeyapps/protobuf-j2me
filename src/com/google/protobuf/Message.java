/*
 * Copyright Copyright (c) 2010, Pondering Panda
 * All rights reserved.
 *
 * See COPYING.txt for the complete copyright notice.
 *
 */

package com.google.protobuf;

import java.io.IOException;

/**
 *
 * @author ralf
 */
public interface Message {
    public void serialize(CodedOutputStream out) throws IOException;
    public void deserialize(CodedInputStream in) throws IOException;
    public byte[] toBytes() throws IOException;
}
