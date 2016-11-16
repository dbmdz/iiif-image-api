#!/bin/sh

JAVA_HOME=$(readlink -f `which javac` | sed "s:/bin/javac::")

cd libjpeg-turbo
autoreconf -fiv
JNI_CFLAGS="-I$JAVA_HOME/include -I/usr/include -I$JAVA_HOME/include/linux" ./configure --with-java
make
cp .libs/libturbojpeg.so.0.1.0 /usr/lib/libturbojpeg-jni.so
