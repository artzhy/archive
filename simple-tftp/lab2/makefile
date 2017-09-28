target: server client

server: server.o tftp.o tftp.h error_handle.h
	gcc -o server server.o tftp.o -g -pthread

client: client.o tftp.o tftp.h error_handle.h
	gcc -o client client.o tftp.o -g

server.o: server.c
	gcc -c server.c -g -lpthread

client.o: client.c
	gcc -c client.c -g

tftp.o: tftp.c
	gcc -c tftp.c -g

clean:
	rm server client *.o

move: server client
	cp server tmp_s/
	cp client tmp_c/
