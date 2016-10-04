JAVA_HOME=$(shell readlink -f `which javac` | sed "s:/bin/javac::")
JNI_CFLAGS=-I$(JAVA_HOME)/include -I/usr/include -I$(JAVA_HOME)/include/linux
EPEG_CFLAGS=-g $(JNI_CFLAGS)  -I./src/main/native -ljpeg
EPEG_LDFLAGS=-fPIC -shared -ljpeg

EPEG_LIB=target/classes/libepeg-jni.so
TURBOJPEG_LIB=target/classes/libturbojpeg-jni.so

EPEG_SOURCES=src/main/native/epeg-jni.c src/main/native/epeg/epeg_main.c
EPEG_OBJECTS=$(EPEG_SOURCES:.c=.o)

all: $(EPEG_SOURCES) $(EPEG_LIB) $(TURBOJPEG_LIB)

$(TURBOJPEG_LIB):
	mkdir -p build
	cd build; JNI_CFLAGS="$(JNI_CFLAGS)" ../src/main/native/libjpeg-turbo/configure --with-java
	make -C build
	cp build/.libs/libturbojpeg.so.0.1.0 $(TURBOJPEG_LIB)
	rm -rf build

$(EPEG_LIB):
	$(CC) $(EPEG_CFLAGS) $(EPEG_SOURCES) $(EPEG_LDFLAGS) -o $@

%.o : %.c
	$(CC) $(EPEG_CFLAGS) $< -o $@

clean:
	rm -rf $(TURBOJPEG_LIB) $(EPEG_LIB) $(EPEG_OBJECTS)

install:
	install -m 0755 target/classes/libepeg-jni.so /usr/lib64
	install -m 0755 target/classes/libturbojpeg-jni.so /usr/lib64
