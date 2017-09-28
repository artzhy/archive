#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <pthread.h>
#include "error_handle.h"

#define BUF_SIZE 100
#define NAME_SIZE 20

void *send_msg(void *arg);
void *recv_msg(void *arg);

char name[NAME_SIZE] = "[DEFAULT]";
char msg[BUF_SIZE];

int main(int argc, char *argv[]){
	int sock;
	struct sockaddr_in serv_adr;
	pthread_t snd_thread, rcv_thread;
	void * thread_return;

	if(argc != 4){
		printf("Usage : %s <IP> <port> <name> \n", argv[0]);
		exit(1);
	}

	sprintf(name, "[%s]", argv[3]);
	sock = socket(PF_INET, SOCK_STREAM, 0);

	memset(&serv_adr, 0, sizeof(serv_adr));
	serv_adr.sin_family = AF_INET;
	serv_adr.sin_addr.s_addr = inet_addr(argv[1]);
	serv_adr.sin_port = htons(atoi(argv[2]));

	error_handle(connect(sock, (struct sockaddr*)&serv_adr, sizeof(serv_adr)));

	pthread_create(&snd_thread, NULL, send_msg, (void*)&sock);
	pthread_create(&snd_thread, NULL, recv_msg, (void*)&sock);
	pthread_join(snd_thread, &thread_return);
	pthread_join(rcv_thread, &thread_return);
	close(sock);

	return 0;
}

void *send_msg(void *arg) { // send thread main
	int sock = *((int*)arg);;
	char name_msg[NAME_SIZE+BUF_SIZE];

	while(1){
		fgets(msg, BUF_SIZE, stdin);
		if(strcmp(msg, "q\n") == 0 || strcmp(msg, "Q\n") == 0){
			close(sock);
			exit(0);
		}
		sprintf(name_msg, "%s %s", name, msg);
		// write(serv_sock, name_msg, strlen(name_msg));
		send(sock, name_msg, strlen(name_msg), 0);
	}
	return NULL;
}

void *recv_msg(void *arg){
	int sock = *((int*)arg);
	char name_msg[NAME_SIZE+BUF_SIZE];
	int str_len;
	while(1){
		str_len = recv(sock, name_msg, NAME_SIZE+BUF_SIZE-1, 0);
		if(str_len == -1)
			return (void*)-1;
		name_msg[str_len] = '\0';
		fputs(name_msg, stdout);
		fflush(stdout);
	}
	return NULL;
}


