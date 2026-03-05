CC = g++
#CC = arm-hisiv300-linux-uclibcgnueabi-g++

DEFINES +=  
INCLUDE += -I ./ConsolePlaybackDemo/
LDFLAGS += 
OUTPUT  += ./ConsolePlaybackDemo/

LDLIBS  += -lpthread -lm -ldl
CFLAGS  += -g -ggdb -fPIC -o0 -Wall
SOURCES := $(wildcard ./ConsolePlaybackDemo/*.cpp)


objects := $(SOURCES:.cpp=.o)

all : $(objects)
	$(CC) -o $(OUTPUT)console_playback_demo  $(objects) $(LDFLAGS) $(LDLIBS) 
	@echo "Build Debug src Finish"

%.o: %.cpp
	$(CC) $(CFLAGS) $(DEFINES) $(INCLUDE) -c $< -o $@

.PHONY : clean
clean :
	rm -f $(OUTPUT)console_playback_demo $(objects)
