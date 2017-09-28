#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>

#define MAXSIZE 4096
#define DEFAULT_PORT 8000

long socket_fd, recv_len, rc;
pthread_t thread;
char recv_buff[MAXSIZE];
char send_buff[MAXSIZE];

void *recv_from_server(void *arg){
	while(1){
		recv_len = recv(socket_fd, recv_buff, MAXSIZE, 0);
		if(recv_len == -1){
			printf("receive error\n");
			exit(1);
		} else if(recv_len == 0){
			printf("Disconnected\n");
			exit(0);
		}
		recv_buff[recv_len] = '\0';
		printf("Received: %s\n", recv_buff);
		fflush(stdout);
	}
}

int main(int argc, char** argv){
	struct sockaddr_in server_addr;

	if(argc != 2){
		printf("Usage: ./client <ipaddress>\n");
		exit(0);
	}

	if( (socket_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0){
		printf("Create socket error: %s(errno: %d)\n", strerror(errno), errno);
		exit(0);
	}

	memset(&server_addr, 0, sizeof(server_addr));
	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(DEFAULT_PORT);
	if( inet_pton(AF_INET, argv[1], &server_addr.sin_addr) <= 0){
		printf("inet_pton error for %s\n", argv[1]);
		exit(0);
	}

	if(connect(socket_fd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0){
		printf("Connect error: %s(errno: %d)\n", strerror(errno), errno);
		exit(0);
	}

	// 创建线程来接受服务器的数据
	long rc;
	rc = pthread_create(&thread, NULL, recv_from_server, NULL);
	if(rc){
		printf("Create thread error, pthread_create returns %ld\n", rc);
		exit(0);
	}

	while(1){ // 主线成负责接受用户的输入
		fgets(send_buff, MAXSIZE, stdin);
		if(strcmp(send_buff, "quit\n") == 0){
			printf("Quiting ... \n");
			break;
		} else {
			rc = send(socket_fd, send_buff, strlen(send_buff), 0);
			if(rc < 0){
				printf("send message error: %s(errno: %d)\n", strerror(errno), errno);
				exit(0);
			}
		}
	}

	close(socket_fd);
	exit(0);
}
