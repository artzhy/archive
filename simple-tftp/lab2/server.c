#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>
#include "error_handle.h"
#include "tftp.h"

#define BACKLOG 10
#define ADDR_SIZE sizeof(struct sockaddr)

#define true 1
#define false 0
#define BUF_SIZE 1024

struct thread_arg{
	struct sockaddr* _addr;
	int addr_size;
	byte *_node;
	int node_len;
};

void *handle_rrq(void *arg);
void *handle_wrq(void *arg);
void print_packet(void *data, int len){
	int i;
	for(i = 0; i < len; i++)
		printf("%x ", ((byte*)data)[i]);
	printf("\n");
}

void setup_addr(struct sockaddr_in *addr, long ip, int port){
	(*addr).sin_family = AF_INET;
	(*addr).sin_addr.s_addr = ip;
	(*addr).sin_port = port;
}

pthread_mutex_t mutex;

int main(int argc, char *argv[]){
	if(argc != 2){
		printf("Usage : %s <port> \n", argv[0]);
		exit(1);
	}
	struct sockaddr_in server_addr, client_addr;
	int server_sockfd;
	int client_addr_size = ADDR_SIZE;
	int recv_len;
	short opcode;
	char send_buff[BUF_SIZE], recv_buff[BUF_SIZE];

	struct thread_arg thread_arg;
	memset(&thread_arg, 0, sizeof(struct thread_arg));
	int i = 0;
	pthread_t threads[1000];
	pthread_mutex_init(&mutex, NULL);

	error_handle(server_sockfd = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP));
	memset(&server_addr, 0, sizeof(server_addr));
	setup_addr(&server_addr, htonl(INADDR_ANY), htons(atoi(argv[1])));
	error_handle(bind(server_sockfd, (struct sockaddr*)&server_addr, ADDR_SIZE));
	printf("Bind to port %s successfully\n", argv[1]);

	while(1){
		error_handle(recv_len = recvfrom(server_sockfd, recv_buff, BUF_SIZE, 0,
					(struct sockaddr*)&client_addr, &client_addr_size));
		printf("---------receive UDP packet ----------------\n");
		opcode = get_opcode((byte*)recv_buff);
		printf("opcode is %d\n", opcode);
		print_packet(recv_buff, recv_len);

		pthread_mutex_lock(&mutex);
		thread_arg._addr = (struct sockaddr*)&client_addr;
		thread_arg.addr_size = client_addr_size;
		thread_arg._node = (byte*)recv_buff;
		thread_arg.node_len = recv_len;
		
		if(opcode == OPCODE_RRQ){
			pthread_create(&threads[i++], NULL, handle_rrq, &thread_arg);
		} else if(opcode == OPCODE_WRQ){
			pthread_create(&threads[i++], NULL, handle_wrq, &thread_arg);
		} else { // invalid opcode
			printf("Receive packet with opcode %d. It is invalid or not supported yet.\n", opcode);
			pthread_mutex_unlock(&mutex);
		}
	}

	close(server_sockfd);
	return 0;
}

void *handle_rrq(void *arg){
	// get addr_size and node_len
	int addr_size = ((struct thread_arg*)arg)->addr_size;
	int node_len  = ((struct thread_arg*)arg)->node_len;
	// copy the addr and node into thread's stack
	struct sockaddr addr = *(((struct thread_arg*)arg)->_addr);
	struct req_node node;
	memcpy(&node, ((struct thread_arg*)arg)->_node, node_len);
	pthread_mutex_unlock(&mutex);

	// get filename and mode from tftp req_node
	char filename[MAX_FILENAME_LEN], mode[MAX_MODE_LEN];
	get_filename_and_mode((byte*)&node, filename, mode);
	printf("filename is %s\n", filename);
	printf("mode is %s\n", mode);

	// open file 
	int file_fd, read_len;
	char data[BLOCK_SIZE+1];
	file_fd = open(filename, O_RDONLY);

	// set up socket and connect to client
	int sock;
	error_handle(sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP));
	error_handle(connect(sock, &addr, addr_size));

	// if file open fail. Send an error packet back to client
	if(file_fd == -1){
		struct error_node error_node;
		int error_node_len;
		printf("Open file error. no such file\n");
		make_error_node(&error_node, &error_node_len, ERR_FILE_NOT_FOUND, "File not found.");
		error_handle(send(sock, &error_node, error_node_len, 0));
		printf("---------Aborted -----------\n");
		return;
	}
	
	// ELSE file exists. Start transfering
	// continously read data from file and send tftp packet
	// until read_len < BLOCK_SIZE
	struct data_node data_node;
	int data_node_len;
	short number = 1;
	while(1){
		read_len = read(file_fd, data, BLOCK_SIZE);
		data[read_len] = '\0';
		printf("read %d bytes from %s.\n", read_len, filename);
		make_data_node(&data_node, &data_node_len, number, data);
		error_handle(send(sock, (byte*)&data_node, data_node_len, 0));
		number++;
		// recv the ACK packet // here we just bypass the verification
		error_handle(recv(sock, &data_node, BLOCK_SIZE, 0));
		if(read_len < BLOCK_SIZE)
			break;
	}
	close(file_fd);
	close(sock);
	printf("------- finished ---------\n");
}

void *handle_wrq(void *arg){
	// get addr_size and node_len
	int addr_size = ((struct thread_arg*)arg)->addr_size;
	int node_len  = ((struct thread_arg*)arg)->node_len;
	// copy the addr and node into thread's stack
	struct sockaddr addr = *(((struct thread_arg*)arg)->_addr);
	struct req_node node;
	memcpy(&node, ((struct thread_arg*)arg)->_node, node_len);
	pthread_mutex_unlock(&mutex);

	// get filename and mode from tftp req_node
	char filename[MAX_FILENAME_LEN], mode[MAX_MODE_LEN];
	get_filename_and_mode((byte*)&node, filename, mode);
	printf("filename is %s\n", filename);
	printf("mode is %s\n", mode);

	// set up socket and connect to client
	int sock;
	error_handle(sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP));
	error_handle(connect(sock, &addr, addr_size));

	// judge if file already exists
	if(access(filename, F_OK) == 0){ // file exists
		struct error_node error_node;
		int error_node_len;
		printf("Open file error. File already exists.\n");
		make_error_node(&error_node, &error_node_len, ERR_FILE_ALREADY_EXISTS, "File already exist.");
		error_handle(send(sock, &error_node, error_node_len, 0));
		printf("---------Aborted -----------\n");
		return;
	}
	
	// open file for writing
	int file_fd, recv_len;
	char data[BLOCK_SIZE+1];
	data[BLOCK_SIZE] = '\0';
	file_fd = open(filename, O_CREAT | O_WRONLY);

	// setup tftp nodes
	struct data_node data_node;
	int data_node_len, data_len;
	struct ack_node ack_node;
	int ack_node_len;
	// send the initial ack packet
	make_ack_node(&ack_node, &ack_node_len, 0);
	error_handle(send(sock, (byte*)&ack_node, ack_node_len, 0));
	short number = 1;
	while(1){
		// recv data node
		error_handle(recv_len = recv(sock, &data_node, sizeof(data_node), 0));
		data_len = recv_len - DATA_HEADER_SIZE;
		write(file_fd, &(data_node.data), data_len);
		printf("write %d bytes to %s.\n", data_len, filename);
		// send ack packet
		make_ack_node(&ack_node, &ack_node_len, number);
		error_handle(send(sock, (byte*)&ack_node, ack_node_len, 0));
		number ++;

		if(data_len < BLOCK_SIZE)
			break;
	}
	close(file_fd);
	close(sock);
	printf("------- finished ---------\n");
}
	
