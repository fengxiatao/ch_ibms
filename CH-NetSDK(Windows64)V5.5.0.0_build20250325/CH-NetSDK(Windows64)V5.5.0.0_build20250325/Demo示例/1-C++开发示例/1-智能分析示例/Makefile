CC = g++

DEFINES +=  
INCLUDE += -I ./VcaPicStreamDemo/
LDFLAGS += 
OUTPUT  += ./VcaPicStreamDemo/

DEBUG_DEMO = 1

ifdef DEBUG_DEMO
LDLIBS  += -lpthread -lm -ldl 
CFLAGS  += -g -ggdb -fPIC -o0 -Wall
SOURCES := $(wildcard ./VcaPicStreamDemo/*.cpp)
else
LDLIBS  += -lpthread -lm -ldl
CFLAGS  += -fPIC
SOURCES := $(wildcard ./VcaPicStreamDemo/*.cpp)
endif

objects := $(SOURCES:.cpp=.o)

all : $(objects)
	$(CC) -o $(OUTPUT)VcaPicStreamDemo $(objects) $(LDFLAGS) $(LDLIBS) 
ifdef DEBUG_DEMO
	@echo "Build Debug VcaPicStreamDemo Finished"
else
	@echo "Build Release VcaPicStreamDemo Finished"
endif

%.o: %.cpp
	$(CC) $(CFLAGS) $(DEFINES) $(INCLUDE) -c $< -o $@

.PHONY : clean
clean :
	rm -f $(OUTPUT)VcaPicStreamDemo $(objects)