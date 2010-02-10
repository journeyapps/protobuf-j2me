#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
from plugin_pb2 import *
from google.protobuf.descriptor_pb2 import *

request = CodeGeneratorRequest()
request.ParseFromString(sys.stdin.read())
response = CodeGeneratorResponse()

class_template = \
"""
import java.util.Vector;

public class %(name)s {
%(nested_enums)s
%(nested_types)s
  
%(fields)s
  
    public void serialize(CodedOutputStream out) {
%(serializer)s
    }
    
    public void deserialize(CodedInputStream in) {
%(parser)s
    }
}
"""

TYPE_MAP = {
  FieldDescriptorProto.TYPE_DOUBLE: ["double", "Double"],
  FieldDescriptorProto.TYPE_FLOAT: ["float", "Float"],
  FieldDescriptorProto.TYPE_INT64: ["long", "Int64"],
  FieldDescriptorProto.TYPE_SINT64: ["long", "SInt64"],
  FieldDescriptorProto.TYPE_UINT64: ["long", "UInt64"],
  FieldDescriptorProto.TYPE_INT32: ["int", "Int32"],
  FieldDescriptorProto.TYPE_SINT32: ["int", "SInt32"],
  FieldDescriptorProto.TYPE_UINT32: ["int", "UInt32"],
  FieldDescriptorProto.TYPE_FIXED32: ["int", "Fixed32"],
  FieldDescriptorProto.TYPE_FIXED64: ["long", "Fixed64"],
  FieldDescriptorProto.TYPE_BOOL: ["boolean", "Bool"],
  FieldDescriptorProto.TYPE_STRING: ["String", "String"],
  FieldDescriptorProto.TYPE_BYTES: ["byte[]", "Bytes"],  
  }
  
for f in request.proto_file:
  for t in f.message_type:
    file = response.file.add()
    file.name = t.name + ".java"
    
    fields = []
    serializer = []
    parser = []
    for field in t.field:
      if field.type in TYPE_MAP:
        type = TYPE_MAP[field.type][0]
        method = TYPE_MAP[field.type][1]
      else:
        raise "Unknown type"
      name = field.name
      number = field.number
      
      
      if field.label == FieldDescriptorProto.LABEL_REPEATED:
        fields.append("    private Vector %(name)s; // %(number)s" % dict(type=type, name=name, number=number))
      else:
        fields.append("    private %(type)s %(name)s;" % dict(type=type, name=name))
        serializer.append("        out.write%(method)s(%(number)s, %(name)s);" % dict(method=method, name=name, number=number))
    
    file.content = class_template % dict(name=t.name, nested_enums="", nested_types="", fields='\n'.join(fields), serializer='\n'.join(serializer), parser='\n'.join(parser))
    
sys.stdout.write(response.SerializeToString())
sys.stdout.flush()
