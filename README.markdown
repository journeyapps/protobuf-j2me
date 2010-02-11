# protobuf-j2me

This is an alternative Java implementation of [Google Protocol Buffers](http://code.google.com/p/protobuf).

Although it can be used in any Java project, it is aimed specifically at J2ME projects. This means the following:

  - No Java enums.
  - No generics; Vectors are used for repeated fields.
  - No reflection.
  - As little as possible code, both for the library and the generated code.

The generated code is simple enough that most of it can be modified or optimized manually, although the changes will be overwritten when the code is generated again.

The code is licenced under the [New BSD License](http://www.opensource.org/licenses/bsd-license.php). See COPYING.txt for details.

# Installation on Ubuntu

  - Download protobuf, and install both the C++ and Python versions.
  - Install Git
  - `git clone git://github.com/ponderingpanda/protobuf-j2me.git`
  - Create an executable file `/usr/local/bin/protoc-gen-j2me`, containing:
  
        #!/bin/sh
        cd /path/to/protobuf-j2me/generator
        python j2megen.py

# Usage

Run `protoc` as usual, using the option `--j2me_out` instead of `--java_out`.

Include the Java source code and the generated code in your application.

# Current features
  - most basic types
    - signed integers not supported yet
    - unsigned are treated as signed integers
  - labels
    - optional fields
      - default value is used when the field does not exist
    - required fields
      - treated the same as optional fields (never checked that the field exists)
    - repeated fields
      - packed fields not supported yet
  - global and nested message declarations
  - global and nested enums
    - treated integer constants
    - not checked to be a valid value
    - constants are generated in a separate class for each enum (might change in the future)

# Planned:
  - lots of unit tests
  - optimization of the library
  - correct handling of variables clashing with reserved keywords
  - includes
  - correct scope handling
  - default values
  - option to nest everything inside a single class (like the official Java implementation)

# Possibilities (might be added):
  - hasXXX methods
    - if a way is found to do this without adding too much overhead and generated code
    - perhaps an option could be added for this?
  - an option to use public fields instead of getters & setters

# Features that probably won't be added:
  - groups
  - extensions
  
