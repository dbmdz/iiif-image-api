JAVA_HOME=$(shell readlink -f `which javac` | sed "s:/bin/javac::")
JNI_CFLAGS=-I$(JAVA_HOME)/include -I/usr/include -I$(JAVA_HOME)/include/linux
TURBOJPEG_LIB=target/classes/libturbojpeg-jni.so

all: $(TURBOJPEG_LIB)

$(TURBOJPEG_LIB):
	cd src/main/native/libjpeg-turbo; autoreconf -fiv
	mkdir -p build
	cd build; JNI_CFLAGS="$(JNI_CFLAGS)" ../src/main/native/libjpeg-turbo/configure --with-java
	make -C build
	cp build/.libs/libturbojpeg.so.0.1.0 $(TURBOJPEG_LIB)
	rm -rf build

clean:
	rm -rf $(TURBOJPEG_LIB)

install:
	install -m 0755 target/classes/libturbojpeg-jni.so /usr/lib64
