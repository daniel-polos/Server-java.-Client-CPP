# define some Makefile variables for the compiler and compiler flags
# to use Makefile variables later in the Makefile: $()
CC = g++
CFLAGS  = -g -Wall -Weffc++ -std=c++11
LFLAGS  = -L/usr/lib -lboost_system -pthread

# All Targets
all: BGSclient

# Tool invocations
# Executable "hello" depends on the files hello.o and run.o.
BGSclient: bin/connectionHandler.o bin/EchoClientMain.o
	@echo 'Building target: hello'
	@echo 'Invoking: C++ Linker'
	$(CC) -o bin/BGSclient bin/connectionHandler.o bin/EchoClientMain.o $(LFLAGS)
	@echo 'Finished building target: BGSclient'
	@echo ' '

bin/connectionHandler.o: src/connectionHandler.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/connectionHandler.o src/connectionHandler.cpp

bin/EchoClientMain.o: src/EchoClientMain.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/EchoClientMain.o src/EchoClientMain.cpp

#Clean the build directory
clean:
	rm -f bin/*