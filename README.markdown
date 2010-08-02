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
  - If it is installed, uninstall the protobuf-compiler shipped with Ubuntu. An old version (2.0.3) is shipped with Ubuntu 9.10, but 2.3.0 is required.
  - Download protobuf 2.3.0, and install both the C++ and Python versions:
  
        ./configure
        make
        sudo make install
        cd python
        ./setup.py build
        sudo ./setup.py install
  
  - Install Git, Ant, JUnit4 (optional) and Mako (http://www.makotemplates.org)

        sudo apt-get install git ant junit4 python-mako

  - Clone the latest version of the repository:

        `git clone git://github.com/ponderingpanda/protobuf-j2me.git`

  - In the repository, run (this allows protoc to use the code generator):
  
        sudo ant install-generator

  - Build the library to include in your application (creates dist/protobuf-mobile.jar):
        
         ant jar

# Installation on Windows

If anyone gets the generator to work on Windows, please let me know. It should be possible using something like py2exe.

# Usage

Run `protoc` as usual, using the option `--j2me_out` instead of `--java_out`.

Make sure that you are using protoc version 2.3.0. This can be confirmed with `protoc --version`. The code generator does not work with any earlier versions, and might not work with future versions.

Include the dist/protobuf-mobile.jar and the generated code in your application.

# Current features
  - most basic types
    - unsigned are treated as signed integers (large numbers will be negative instead)
  - labels
    - optional fields
      - support is currently experimental
    - required fields
      - no validation is done
      - an arbitrary default value is used if the field is not set
    - repeated fields
      - packed fields not supported yet
  - global and nested message declarations
  - global and nested enums
    - treated as integer constants
    - not validated
  - imports
    - experimental support, might not work in all cases

# Planned:
  - lots of unit tests
  - optimization of the library
  - better handling of variables clashing with reserved keywords
  - better scope handling
  - default values
  - option to nest everything inside a single class (like the official Java implementation)

# Possibilities (might be added):
  - hasXXX methods
    - add an option to disable this feature, as an optimization
  - an option to use public fields instead of getters & setters

# Features that probably won't be added:
  - groups
  - extensions
  
# Recent changes

2010-08-02
  - Rewrite of the code generator to use the Mako template engine.
  - Dropped support for the external serializer class (the seperate_encoders option)
  - Added an Ant build.xml file:
    - To build the library.
    - To run the unit tests.
    - To install the generator (on unix systems).
  - Added methods for optional nested messages (hasXXX and clearXXX)
  - Added convenience methods to messages to convert from and to byte arrays.
  

2010-07-27
  - Added support for optional fields (primitive fields now have a _hasXXX flag).
