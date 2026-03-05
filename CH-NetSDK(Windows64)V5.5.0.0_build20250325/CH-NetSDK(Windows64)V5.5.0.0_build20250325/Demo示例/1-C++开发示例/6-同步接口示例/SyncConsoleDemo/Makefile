CC = g++
PUB_VER = DEBUG
#PUB_VER = RELEASE

INCLUDE += -I ./
OUTFILE = ./sync_console_demo

ifeq ($(PUB_VER), DEBUG)
	LDLIBS  += -lpthread -lm -ldl
	CFLAGS  += -g -ggdb -fPIC -o0 -Wall
	SOURCES := $(wildcard ./*.cpp)
else
	LDLIBS  += -lpthread -lm -ldl
	CFLAGS  += -fPIC
	SOURCES := $(wildcard ./*.cpp)
endif

objects := $(SOURCES:.cpp=.o)

all : $(objects)
	$(CC) -o $(OUTFILE) $(objects) $(LDLIBS) 
	
ifeq ($(PUB_VER), DEBUG)
	@echo "Build Debug sync_console_demo Finished"
else
	@echo "Build Release sync_console_demo Finished"
endif

%.o: %.cpp
	$(CC) $(CFLAGS) $(INCLUDE) -c $< -o $@

clean :
	rm -f $(OUTFILE) $(objects)