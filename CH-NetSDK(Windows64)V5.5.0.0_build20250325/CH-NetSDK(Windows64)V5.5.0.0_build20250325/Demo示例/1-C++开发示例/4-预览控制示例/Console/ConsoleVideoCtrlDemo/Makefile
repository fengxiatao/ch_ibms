CC = g++

DEFINES +=  
INCLUDE += -I ./
LDFLAGS += 
OUTPUT  += ./

DEBUG_DEMO = 1

ifdef DEBUG_DEMO
LDLIBS  += -lpthread -lm -ldl -lrt
CFLAGS  += -g -ggdb -fPIC -o0 -Wall
SOURCES := $(wildcard ./*.cpp)
else
LDLIBS  += -lpthread -lm -ldl -lrt
CFLAGS  += -fPIC
SOURCES := $(wildcard ./*.cpp)
endif

objects := $(SOURCES:.cpp=.o)

all : $(objects)
	$(CC) -o $(OUTPUT)ConsoleVideoCtrlDemo $(objects) $(LDFLAGS) $(LDLIBS) 
ifdef DEBUG_DEMO
	@echo "Build Debug ConsoleVideoCtrlDemo Finished"
else
	@echo "Build Release ConsoleVideoCtrlDemo Finished"
endif

%.o: %.cpp
	$(CC) $(CFLAGS) $(DEFINES) $(INCLUDE) -c $< -o $@

.PHONY : clean
clean :
	rm -f $(OUTPUT)ConsoleVideoCtrlDemo $(objects)