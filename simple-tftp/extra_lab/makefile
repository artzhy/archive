ERR_HLE = error_handle.h

target: server client

server: server.c ${ERR_HLE}
	gcc -o server server.c -pthread

client: client.c ${ERR_HLE}
	gcc -o client client.c -pthread

clean:
	rm client server
