CC = g++

DEFINES +=  
INCLUDE += -I ./
LDFLAGS += 
OUTPUT  += ./

DEBUG_DEMO = 1

ifdef DEBUG_DEMO
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
	$(CC) -o $(OUTPUT)console_face_demo $(objects) $(LDFLAGS) $(LDLIBS) 
ifdef DEBUG_DEMO
	@echo "Build Debug console_face_demo Finished"
else
	@echo "Build Release console_face_demo Finished"
endif

%.o: %.cpp
	$(CC) $(CFLAGS) $(DEFINES) $(INCLUDE) -c $< -o $@

.PHONY : clean
clean :
	rm -f $(OUTPUT)console_face_demo $(objects)