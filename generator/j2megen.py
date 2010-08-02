# -*- coding: utf-8 -*-
from mako.template import Template
from mako.runtime import Context
from io import StringIO
from plugin_pb2 import *
from google.protobuf.descriptor_pb2 import *
import sys

WIRETYPE_VARINT           = 0
WIRETYPE_FIXED64          = 1
WIRETYPE_LENGTH_DELIMITED = 2
WIRETYPE_FIXED32          = 5

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

class MessageDescription:
  pass

class FieldDescription:
  pass

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
  


def parse_type(t):
  m = MessageDescription()
  m.name = t.name
  m.nested_enums = t.enum_type
  m.nested_types = []
  
  for nested in t.nested_type:
    m.nested_types.append(parse_type(nested))
      
  m.fields = []
  for field in t.field:
    f = FieldDescription()
    if field.type == FieldDescriptorProto.TYPE_MESSAGE:
      if field.type_name.startswith('.'):
        f.type = fully_qualified_name2(field.type_name)
      else:
        f.type = fully_qualified_name(parents, field.type_name)
        
      f.method = "Message"
      f.tag = (field.number << 3) + WIRETYPE_LENGTH_DELIMITED
      f.is_message = True
    elif field.type in TYPE_MAP:
      f.type = TYPE_MAP[field.type][0]
      f.method = TYPE_MAP[field.type][1]
      f.tag = (field.number << 3) + TYPE_MAP[field.type][2]
      f.is_message = False
    else:
      raise Exception("Unknown type: %s" % (field.type))
    
    words = [word.lower() for word in field.name.split('_')]
    f.Name = ''.join([word.title() for word in words])
    f.name = ''.join([words[0]] + [word.title() for word in words[1:]])
    f.number = field.number
    
    f.repeated = (field.label == FieldDescriptorProto.LABEL_REPEATED)
    f.optional = (field.label == FieldDescriptorProto.LABEL_OPTIONAL)
    
    if field.label == FieldDescriptorProto.LABEL_REPEATED:
      f.box_e, f.box_s, f.unbox_e, f.unbox_s = [""]*4
      if f.type in WRAPPERS:
        f.box_s = "new %s(" % WRAPPERS[f.type]
        f.box_e = ")"
        f.unbox_s = "((%s)" % WRAPPERS[f.type]
        f.unbox_e = ").%sValue()" % f.type
      else:
        f.unbox_s = "(%s)" % f.type
      
            
    m.fields.append(f)
  
  return m
    
def find_all_classes(messages, parents=[]):
  for message in messages:
    all_classes.add('.'.join(parents + [message.name]))
    find_all_classes(message.nested_type, parents + [message.name])

message_template = Template(filename='message_template')
enum_template = Template(filename='enum_template')

def render(template, **attrs):
    buf = StringIO()
    ctx = Context(buf, **attrs)
    template.render_context(ctx, **attrs)
    return buf.getvalue()

request = CodeGeneratorRequest()
request.ParseFromString(sys.stdin.read())
response = CodeGeneratorResponse()

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
    
  imports = []
  for d in f.dependency:
    if d in imported_packages:
      imports.append(imported_packages[d])
  
  
  if f.options.HasField("java_package"):
    package = f.options.java_package
  elif f.HasField("package"):
    package = f.package
  else:
    package = None
    
  if package:
    folder = '/'.join(package.split('.')) + '/'
  else:
    folder = ''
    
  
  # First pass: find all classes
  find_all_classes(f.message_type, [])
  
  for t in f.message_type:
    file = response.file.add()
    file.name = folder + t.name + ".java"
    
    result = render(message_template, package=package, imports=imports, root_type=parse_type(t))
    
    file.content = result
    
  for t in f.enum_type:
    file = response.file.add()
    file.name = folder + t.name + ".java"
    
    result = render(enum_template, package=package, enum=t)
    file.content = result
    
    
sys.stdout.write(response.SerializeToString())
sys.stdout.flush()
