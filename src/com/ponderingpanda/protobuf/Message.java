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
public interface Message {
    public void serialize(CodedOutputStream out)throws IOException;
    public void deserialize(CodedInputStream in) throws IOException;
}
