JAVA_HOME=$(shell readlink -f `which javac` | sed "s:/bin/javac::")
CC=gcc
CFLAGS=-g -c -fPIC -I$(JAVA_HOME)/include -I/usr/include -I$(JAVA_HOME)/include/linux -I./src/main/native -ljpeg
LDFLAGS=-fPIC -shared -ljpeg

SOURCES_DIR=src/main/native
SHARED_LIB=target/classes/libjpegtran-jni.so

SOURCES=$(wildcard src/main/native/*.c)
OBJECTS=$(SOURCES:.c=.o)

all: $(SOURCES) $(SHARED_LIB)

$(SHARED_LIB): $(OBJECTS)
	mkdir -p target/classes/
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.c.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf $(SHARED_LIB) $(SOURCES_DIR)/*.o

install:
	install -m 0755 target/classes/libjpegtran-jni.so /usr/lib64
