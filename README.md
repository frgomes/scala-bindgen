## Overview

[![Join the chat at https://gitter.im/frgomes/scala-bindgen](https://badges.gitter.im/frgomes/scala-bindgen.svg)](https://gitter.im/frgomes/scala-bindgen?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/frgomes/scala-bindgen.svg?branch=master)](https://travis-ci.org/frgomes/scala-bindgen)

**This is work in progress, in early inception phase.**

A native binding generator for the [Scala Native] language.


## For the impatient

Install requirements:

    $ source requirements.sh
    
Now you can build and run:

    $ sbt clean nativeLink 'bindgen/run test/test001.h'


## Usage

    $ sbt 'bindgen/run --help'


## Supported toolchains

 * Debian Stretch
 * Ubuntu
 * OSX

 See: https://github.com/frgomes/scala-bindgen/blob/master/requirements.sh


## How it works

In a nutshell, ``scala-bindgen`` lays on the shoulders of the giant [Clang] compiler (from [LLVM] infrastructure) 
for achieving the task of transforming ``C`` header files into ``Scala`` bindings.
For examples on how ``Scala`` bindings look like, please see [stdlib.scala].

1. ``scala-bindgen`` employs ``Clang`` (from LLVM infrastructure) for parsing ``C`` header files;
2. ``Clang`` builds an AST which represents all sources parsed;
3. ``scala-bindgen`` visits the AST and generates Scala bindings.

At the moment ``scala-bindgen`` only generates bindings for ``C`` language.


## Community

 * Have a question? Ask it on [Stack Overflow with tag scala-bindgen].
 * Want to chat? Join our [Gitter chat channel].
 * Found a bug or want to propose a new feature? Open [an issue on Github].

## License

``scala-bindgen`` is distributed under [the Scala license].


[Stack Overflow with tag scala-bindgen]: http://stackoverflow.com/questions/tagged/scala-bindgen
[Gitter chat channel]: https://gitter.im/frgomes/scala-bindgen
[an issue on Github]: https://github.com/frgomes/scala-bindgen/issues
[the Scala license]: https://github.com/frgomes/scala-bindgen/blob/master/LICENSE
[Scala]: http://scala-lang.org
[Scala Native]: http://github.com/scala-native/scala-native
[Clay's bindgen]: http://github.com/jckarter/clay/blob/master/tools/bindgen.clay
[Rust's bindgen]: http://github.com/crabtw/rust-bindgen
[JOGL GlueGen]: https://jogamp.org/gluegen/www
[Clang]: http://clang.llvm.org/
[LLVM]: http://llvm.org
[stdlib.scala]: http://github.com/scala-native/scala-native/blob/master/nativelib/src/main/scala/scala/scalanative/native/stdlib.scala
