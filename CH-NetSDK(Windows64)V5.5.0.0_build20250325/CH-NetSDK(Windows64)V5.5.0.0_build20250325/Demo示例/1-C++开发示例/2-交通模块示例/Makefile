CC = g++

DEFINES +=  
INCLUDE += -I ./ItsPicStreamDemo/
LDFLAGS += 
OUTPUT  += ./ItsPicStreamDemo/

DEBUG_DEMO = 1

ifdef DEBUG_DEMO
LDLIBS  += -lpthread -lm -ldl 
CFLAGS  += -g -ggdb -fPIC -o0 -Wall
SOURCES := $(wildcard ./ItsPicStreamDemo/*.cpp)
else
LDLIBS  += -lpthread -lm -ldl
CFLAGS  += -fPIC
SOURCES := $(wildcard ./ItsPicStreamDemo/*.cpp)
endif

objects := $(SOURCES:.cpp=.o)

all : $(objects)
	$(CC) -o $(OUTPUT)ItsPicStreamDemo $(objects) $(LDFLAGS) $(LDLIBS) 
ifdef DEBUG_DEMO
	@echo "Build Debug ItsPicStreamDemo Finished"
else
	@echo "Build Release ItsPicStreamDemo Finished"
endif

%.o: %.cpp
	$(CC) $(CFLAGS) $(DEFINES) $(INCLUDE) -c $< -o $@

.PHONY : clean
clean :
	rm -f $(OUTPUT)ItsPicStreamDemo $(objects)