#!/usr/bin/python
# -*- coding: utf-8 -*-

# Copyright Copyright (c) 2010, Pondering Panda
# All rights reserved.
# See COPYING.txt

import sys
from plugin_pb2 import *
from google.protobuf.descriptor_pb2 import *
from exceptions import Exception

request = CodeGeneratorRequest()
request.ParseFromString(sys.stdin.read())
response = CodeGeneratorResponse()

seperate_encoders = True

file_template = """
%(package)s

import java.util.Vector;
import java.io.IOException;

import com.ponderingpanda.protobuf.*;

%(imports)s

public class %(name)s implements Message {
%(contents)s
}
"""

enum_file_template = """
%(package)s

public class %(name)s {
%(contents)s
}
"""

encoders_file_template = """
%(package)s

import java.util.Vector;
import java.io.IOException;

import com.ponderingpanda.protobuf.*;

%(imports)s

public class %(name)s {
%(contents)s
}
"""

nested_template = """
public static class %(name)s implements Message {
%(contents)s
}
"""

enum_nested_template = """
public static class %(name)s {
%(contents)s
}
"""

class_template = """
%(nested_enums)s
%(nested_types)s
%(fields)s
%(accessors)s
%(encoder)s
"""

encoders_template = """
public final void serialize(CodedOutputStream out) throws IOException {
%(serializer)s
}

public final void deserialize(CodedInputStream in) throws IOException {
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

seperate_encoders_template = """
public final void serialize(CodedOutputStream out) throws IOException {
    %(encoder)s.serialize(this, out);
}

public final void deserialize(CodedInputStream in) throws IOException {
    %(encoder)s.deserialize(this, in);
}
"""

seperate_encoder_implementation_template = """
public static void serialize(%(name)s msg, CodedOutputStream out) throws IOException {
%(serializer)s
}

public static void deserialize(%(name)s msg, CodedInputStream in) throws IOException {
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

public void set%(Name)sVector(Vector value) {
    this.%(name)s = value;
}
"""

repeated_parser_template = """case %(tag)d: {
    %(message)s.add%(Name)s(in.read%(method)s());
    break; }"""

repeated_message_parser_template = """case %(tag)d: {
    %(type)s message = new %(type)s();
    in.readMessage(message);
    %(message)s.add%(Name)s(message);
    break; }"""
    
repeated_serializer_template = """
for(int i = 0; i < %(message)s.get%(Name)sCount(); i++) {
    out.write%(method)s(%(number)d, %(message)s.get%(Name)s(i));
}"""

single_parser_template = """case %(tag)d: {
    %(message)s.%(name)s = in.read%(method)s();
    break; }"""
    
single_message_parser_template = """case %(tag)d: {
    %(message)s.%(name)s = new %(type)s();
    in.readMessage(%(message)s.%(name)s);
    break; }"""
    
single_serializer_template = "out.write%(method)s(%(number)d, %(message)s.%(name)s);"


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
  FieldDescriptorProto.TYPE_SFIXED32: ["int", "Fixed32", WIRETYPE_FIXED32],
  FieldDescriptorProto.TYPE_SFIXED64: ["long", "Fixed64", WIRETYPE_FIXED64],
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

all_classes = set()

def fully_qualified_name(parents, classname):
  while len(parents) > 0:
    name = '.'.join(parents + [classname])
    if name in all_classes:
      return name
    parents = parents[:-1]
  return classname

def fully_qualified_name2(name):
  all = name.split('.')[1:]
  while len(all) > 1:
    name = '.'.join(all)
    if name in all_classes:
      return name
    all = all[1:]
  return all[0]
  
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

def generate_class(t, parents):
  fields = []
  accessors = []
  serializer = []
  parser = []
  nested_enums = []
  nested_types = []
  nested_encoder_implementations = []
  
  if seperate_encoders:
    message_identifier = "msg"
  else:
    message_identifier = "this"
    
  for enum in t.enum_type:
    #nested_enums.append(enum_nested_template % dict(name=enum.name, contents=indent(generate_enum(enum))))
    nested_enums.append(generate_enum(enum))
      
  for nested in t.nested_type:
    contents, nested_encoder_implementation = generate_class(nested, parents + [t.name])
    nested_types.append(nested_template % dict(name=nested.name, contents=indent(contents)))
    nested_encoder_implementations += nested_encoder_implementation
    
  for field in t.field:
    if field.type == FieldDescriptorProto.TYPE_MESSAGE:
      if field.type_name.startswith('.'):
        type = fully_qualified_name2(field.type_name)
      else:
        type = fully_qualified_name(parents, field.type_name)
        
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
      fields.append("protected Vector %(name)s = new Vector(); // %(number)s, %(type)s" % dict(type=type, name=name, number=number))
      box_e, box_s, unbox_e, unbox_s = [""]*4
      if type in WRAPPERS:
        box_s = "new %s(" % WRAPPERS[type]
        box_e = ")"
        unbox_s = "((%s)" % WRAPPERS[type]
        unbox_e = ").%sValue()" % type
      else:
        unbox_s = "(%s)" % type
        
      accessors.append(repeated_accessor_template % dict(name=name, Name=Name, type=type, box_s=box_s, box_e=box_e, unbox_s=unbox_s, unbox_e=unbox_e))
      serializer.append((number, repeated_serializer_template % dict(message=message_identifier, method=method, Name=Name, number=number)))
      
      if field.type == FieldDescriptorProto.TYPE_MESSAGE:
        parser.append((number, repeated_message_parser_template % dict(message=message_identifier, tag=tag, Name=Name, type=type)))
      else:
        parser.append((number, repeated_parser_template % dict(message=message_identifier, tag=tag, Name=Name, method=method)))
    
    else:
      fields.append("protected %(type)s %(name)s; // %(number)s" % dict(type=type, name=name, number=number))
      accessors.append(accessor_template % dict(name=name, Name=Name, type=type))
      serializer.append((number, single_serializer_template % dict(message=message_identifier, method=method, name=name, number=number)))
      
      if field.type == FieldDescriptorProto.TYPE_MESSAGE:
        parser.append((number, single_message_parser_template % dict(message=message_identifier, tag=tag, name=name, type=type)))
      else:
        parser.append((number, single_parser_template % dict(message=message_identifier, tag=tag, name=name, method=method)))
    
    
  parser.sort()
  serializer.sort()
  parser = [p[1] for p in parser]
  serializer = [s[1] for s in serializer]
  
  if seperate_encoders:
    encoders = seperate_encoders_template % dict(encoder="ProtobufEncoders")
    nested_encoder_implementations.append(seperate_encoder_implementation_template % dict(name='.'.join(parents + [t.name]), serializer=jindent(serializer, 1), parser=jindent(parser, 3)))
  else:
    encoders = encoders_template % dict(serializer=jindent(serializer, 1), parser=jindent(parser, 3))
    nested_encoder_implementations = []
  
  contents = class_template % dict(nested_enums=jindent(nested_enums, 0), nested_types=jindent(nested_types, 0), fields=jindent(fields, 0), accessors=jindent(accessors, 0), encoder=encoders)
  return contents, nested_encoder_implementations
  
  
def find_all_classes(messages, parents=[]):
  for message in messages:
    all_classes.add('.'.join(parents + [message.name]))
    find_all_classes(message.nested_type, parents + [message.name])

files_to_generate = set(request.file_to_generate)
in_imported_file = True
imported_packages = {}

for f in request.proto_file:
  # Do not generate anything for imported files
  if not f.HasField("name"):
    # Continue previous file
    if in_imported_file:
      continue
  else:
    if f.name in files_to_generate:
      in_imported_file = False
    else:
      in_imported_file = True
      if f.options.HasField("java_package"):
        imported_packages[f.name] = f.options.java_package
      continue
    
  imports = ""
  for d in f.dependency:
    imports += "import %s.*;\n" % (imported_packages[d])
  
  
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
    
  encoders = []
  
  # First pass: find all classes
  find_all_classes(f.message_type, [])
  
  for t in f.message_type:
    file = response.file.add()
    file.name = folder + t.name + ".java"
    
    c, nested_encoder_implementations = generate_class(t, [])
    encoders += nested_encoder_implementations
    file.content = file_template % {'name': t.name, 'contents': indent(c), 'package': pline, 'imports': imports}
    
  for t in f.enum_type:
    file = response.file.add()
    file.name = folder + t.name + ".java"
    
    c = generate_enum(t)
    file.content = enum_file_template % {'name': t.name, 'contents': indent(c), 'package': pline, 'imports': imports}
    
  if seperate_encoders:
    file = response.file.add()
    file.name = folder + "ProtobufEncoders.java"
    file.content = encoders_file_template % dict(name="ProtobufEncoders", contents=jindent(encoders), package=pline, imports=imports)
    
sys.stdout.write(response.SerializeToString())
sys.stdout.flush()
