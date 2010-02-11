#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
from plugin_pb2 import *
from google.protobuf.descriptor_pb2 import *
from exceptions import Exception

request = CodeGeneratorRequest()
request.ParseFromString(sys.stdin.read())
response = CodeGeneratorResponse()

file_template = """
import java.util.Vector;

import com.ponderingpanda.protobuf.*;

%(package)s

public class %(name)s {
%(contents)s
}
"""

nested_template = """
public static class %(name)s {
%(contents)s
}
"""

class_template = """
%(nested_enums)s
%(nested_types)s
%(fields)s
%(accessors)s

public final void serialize(CodedOutputStream out) {
%(serializer)s
}

public final void deserialize(CodedInputStream in) {
    while(true) {
        int tag = in.readTag();
        switch(tag) {
            case 0:
                return;
%(parser)s
            default:
                in.skipTag(tag);
        }
    }
}
"""

accessor_template = """
public %(type)s get%(Name)s() {
    return %(name)s;
}

public void set%(Name)s(%(type)s %(name)s) {
    this.%(name)s = %(name)s;
}
"""

repeated_accessor_template = """
public void add%(Name)s(%(type)s value) {
    this.%(name)s.addElement(%(box_s)svalue%(box_e)s);
}

public int get%(Name)sCount() {
    return this.%(name)s.size();
}

public %(type)s get%(Name)s(int index) {
    return %(unbox_s)sthis.%(name)s.elementAt(index)%(unbox_e)s;
}

public Vector get%(Name)sVector() {
    return this.%(name)s;
}
"""

repeated_parser_template = \
"""case %(tag)d:
    this.add%(Name)s(in.read%(method)s());
    break;"""
    
repeated_serializer_template = """
for(int i = 0; i < get%(Name)sCount(); i++) {
    out.write%(method)s(%(number)d, get%(Name)s(i));
}"""

single_parser_template = \
"""case %(tag)d:
    this.%(name)s = in.read%(method)s();
    break;"""
    
single_serializer_template = "out.write%(method)s(%(number)d, %(name)s);"


WIRETYPE_VARINT           = 0;
WIRETYPE_FIXED64          = 1;
WIRETYPE_LENGTH_DELIMITED = 2;
WIRETYPE_FIXED32          = 5;
  
TYPE_MAP = {
  FieldDescriptorProto.TYPE_DOUBLE: ["double", "Double", WIRETYPE_FIXED64],
  FieldDescriptorProto.TYPE_FLOAT: ["float", "Float", WIRETYPE_FIXED32],
  FieldDescriptorProto.TYPE_INT64: ["long", "Int64", WIRETYPE_VARINT],
  FieldDescriptorProto.TYPE_SINT64: ["long", "SInt64", WIRETYPE_VARINT],
  FieldDescriptorProto.TYPE_UINT64: ["long", "UInt64", WIRETYPE_VARINT],
  FieldDescriptorProto.TYPE_INT32: ["int", "Int32", WIRETYPE_VARINT],
  FieldDescriptorProto.TYPE_SINT32: ["int", "SInt32", WIRETYPE_VARINT],
  FieldDescriptorProto.TYPE_UINT32: ["int", "UInt32", WIRETYPE_VARINT],
  FieldDescriptorProto.TYPE_FIXED32: ["int", "Fixed32", WIRETYPE_FIXED32],
  FieldDescriptorProto.TYPE_FIXED64: ["long", "Fixed64", WIRETYPE_FIXED64],
  FieldDescriptorProto.TYPE_BOOL: ["boolean", "Bool", WIRETYPE_VARINT],
  FieldDescriptorProto.TYPE_STRING: ["String", "String", WIRETYPE_LENGTH_DELIMITED],
  FieldDescriptorProto.TYPE_BYTES: ["byte[]", "Bytes", WIRETYPE_LENGTH_DELIMITED],
  FieldDescriptorProto.TYPE_ENUM: ["int", "Int32", WIRETYPE_VARINT],    # enums are treated as integers
  }
  
WRAPPERS = dict(
  int="Integer",
  long="Long",
  double="Double",
  float="Float",
  boolean="Boolean",
)
 
def indent(contents, times=1):
  if isinstance(contents, list):
    return ['    '*times + line for line in contents]
  else:
    lines = contents.split('\n')
    return '\n'.join(['    '*times + line for line in lines])
  
def jindent(lines, times=1):
  return indent('\n'.join(lines), times)

def generate_enum(enum):
  values = []
  for value in enum.value:
    values.append("public static final int %(name)s = %(value)d;" % dict(name=value.name, value=value.number))
  return jindent(values, 0)

def generate_class(t):
  fields = []
  accessors = []
  serializer = []
  parser = []
  nested_enums = []
  nested_types = []
  for enum in t.enum_type:
    nested_enums.append(nested_template % dict(name=enum.name, contents=indent(generate_enum(enum))))
      
  for nested in t.nested_type:
    nested_types.append(nested_template % dict(name=nested.name, contents=indent(generate_class(nested))))
    
  for field in t.field:
    if field.type == FieldDescriptorProto.TYPE_MESSAGE:
      type = field.type_name.split('.')[-1]
      method = "Message"
      tag = (field.number << 3) + WIRETYPE_LENGTH_DELIMITED
    elif field.type in TYPE_MAP:
      type = TYPE_MAP[field.type][0]
      method = TYPE_MAP[field.type][1]
      tag = (field.number << 3) + TYPE_MAP[field.type][2]
    else:
      raise Exception("Unknown type: %s" % (field.type))
    
    words = [word.lower() for word in field.name.split('_')]
    Name = ''.join([word.title() for word in words])
    name = ''.join([words[0]] + [word.title() for word in words[1:]])
    number = field.number
    
    
    
    if field.label == FieldDescriptorProto.LABEL_REPEATED:
      fields.append("private Vector %(name)s; // %(number)s" % dict(type=type, name=name, number=number))
      box_e, box_s, unbox_e, unbox_s = [""]*4
      if type in WRAPPERS:
        box_s = "new %s(" % WRAPPERS[type]
        box_e = ")"
        unbox_s = "((%s)" % WRAPPERS[type]
        unbox_e = ").%sValue()" % type
      else:
        unbox_s = "(%s)" % type
        
      accessors.append(repeated_accessor_template % dict(name=name, Name=Name, type=type, box_s=box_s, box_e=box_e, unbox_s=unbox_s, unbox_e=unbox_e))
      serializer.append((number, repeated_serializer_template % dict(method=method, Name=Name, number=number)))
      
      parser.append((number, repeated_parser_template % dict(tag=tag, Name=Name, method=method)))
    
    else:
      fields.append("private %(type)s %(name)s; // %(number)s" % dict(type=type, name=name, number=number))
      accessors.append(accessor_template % dict(name=name, Name=Name, type=type))
      serializer.append((number, single_serializer_template % dict(method=method, name=name, number=number)))
      
      parser.append((number, single_parser_template % dict(tag=tag, name=name, method=method)))
    
    
  parser.sort()
  serializer.sort()
  parser = [p[1] for p in parser]
  serializer = [s[1] for s in serializer]
  
  return class_template % dict(nested_enums=jindent(nested_enums, 0), nested_types=jindent(nested_types, 0), fields=jindent(fields, 0), serializer=jindent(serializer, 1), parser=jindent(parser, 3), accessors=jindent(accessors, 0))
  
for f in request.proto_file:
  if f.options.HasField("java_package"):
    package = f.options.java_package
  elif f.HasField("package"):
    package = f.package
  else:
    package = None
    
  if package:
    pline = "package %s;" % package
    folder = '/'.join(package.split('.')) + '/'
  else:
    pline = ""
    folder = ''
    
  for t in f.message_type:
    file = response.file.add()
    file.name = folder + t.name + ".java"
    
    c = generate_class(t)
    file.content = file_template % {'name': t.name, 'contents': indent(c), 'package': pline}
    
  for t in f.enum_type:
    file = response.file.add()
    file.name = folder + t.name + ".java"
    
    c = generate_enum(t)
    file.content = file_template % {'name': t.name, 'contents': indent(c), 'package': pline}
    
    
sys.stdout.write(response.SerializeToString())
sys.stdout.flush()
