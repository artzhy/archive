#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>
#include "error_handle.h"
#include "tftp.h"

#define DEFAULT_PORT 5000
#define ADDR_SIZE sizeof(struct sockaddr)
#define SLOW_CLIENT 1

#define true 1
#define false 0
#define BUF_SIZE 1024

void prompt();
void print_help_info();
void setup_addr(struct sockaddr_in *server_addr, long ip, int port);
void do_get();
void do_put();

struct sockaddr_in server_addr;
int sock;
int server_addr_size = ADDR_SIZE;

int main(int argc, char *argv[]){
	char ip_input_buff[BUF_SIZE]; 
	int port_input;
	char input_buff[BUF_SIZE];
	int is_connected = false;

	error_handle(sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP));
	memset(&server_addr, 0, sizeof(server_addr));

	while(1){
		prompt();
		fgets(input_buff, BUF_SIZE, stdin);
		if(strcmp(input_buff, "connect\n") == 0){
			printf("type server IP: ");
			scanf("%s", ip_input_buff);
			printf("type server port: ");
			scanf("%d", &port_input);
			setup_addr(&server_addr, inet_addr(ip_input_buff), htons(port_input));
			is_connected = true;
			// clear the unnecessary '\n' at the end of the input
			fgets(input_buff, BUF_SIZE, stdin); 
		} else if(strcmp(input_buff, "put\n") == 0){
			if(is_connected == false){
				printf("You need connect to server first\n");
				continue;
			}
			do_put();
		} else if(strcmp(input_buff, "get\n") == 0){
			if(is_connected == false){
				printf("You need connect to server first\n");
				continue;
			}
			do_get();
		} else if(strcmp(input_buff, "quit\n") == 0){
			printf("quiting");
			break;
		} else if(strcmp(input_buff, "?\n") == 0){
			print_help_info();
		} else if(strcmp(input_buff, "\n") == 0){
			continue;
		} else { // invalid input
			printf("Invalid command. Type ? to get help\n");
			continue;
		}
	}

	close(sock);
	return 0;
}

void prompt(){
	printf("tftp> ");
}

void print_help_info(){
	printf(""
			"conect     connect to remote tftp\n"
			"put        send file\n"
			"get        receive file\n"
			"quit       exit tftp\n"
			"?          print help information\n"
			"NOTE: other tftp commands are not supported yet\n"
			"");
}

void setup_addr(struct sockaddr_in *server_addr, long ip, int port){
	(*server_addr).sin_family = AF_INET;
	(*server_addr).sin_addr.s_addr = ip;
	(*server_addr).sin_port = port;
}

void do_get(){
	char local_filename[MAX_FILENAME_LEN], remote_filename[MAX_FILENAME_LEN];

	// get remove_filename and local_filname
	printf("type remote filename: ");
	fgets(remote_filename, MAX_FILENAME_LEN, stdin);
	remote_filename[strlen(remote_filename)-1] = '\0'; // delete the '\n' at the end 
	printf("type local filename: ");
	fgets(local_filename, MAX_FILENAME_LEN, stdin);
	local_filename[strlen(local_filename)-1] = '\0'; // delete the '\n' at the end 
	if(strcmp(local_filename, "") == 0) // the user just type `enter`
		strcpy(local_filename, remote_filename); // user remote_filename as the local_filename

	printf("remote filename : %s\n", remote_filename);
	printf("local filename : %s\n", local_filename);

	/*
	   1. RRQ            ------> server
	   2. DATA(number 1) <------ server
	   3. ACK(bumber 1)  ------> server
	   4. DATA(number 2) <------ server
	   5. ...........
	   6. DATA(last 0-511bytes) <----- server
	   7. ACK(number n)  ------> server
	 */
	// send req packet
	struct req_node req_node;
	int req_node_len;
	make_req_node(&req_node, &req_node_len, OPCODE_RRQ, remote_filename, MODE_NETASCII);
	if(SLOW_CLIENT)
		sleep(1);
	error_handle(sendto(sock, (byte*)&req_node, req_node_len, 0, 
				(struct sockaddr*)&server_addr, server_addr_size));

	// setup ack_node, data_node and error_node
	struct ack_node ack_node;
	struct data_node data_node;
	struct error_node* error_node_ptr;
	// struct error_node error_node;
	int ack_node_len, data_node_len/*, error_node_len*/;
	int data_len;
	short number;

	// open file
	int file_fd;
	file_fd = open(local_filename, O_CREAT | O_WRONLY | O_TRUNC);

	struct sockaddr serv_work_addr;
	int serv_work_addr_size = sizeof(struct sockaddr);
	while(1){
		error_handle(data_node_len = recvfrom(sock, (byte*)&data_node, sizeof(data_node), 0, 
					&serv_work_addr, &serv_work_addr_size));
		if(get_opcode((byte*)&data_node) == OPCODE_ERR){ // the server responses ERROR
			error_node_ptr = (struct error_node*)&data_node;
			printf("Server responses with ERROR: ");
			printf("%s (error: %d)\n", error_node_ptr->message, 
					ntohs(error_node_ptr->error_number));
			printf("--------------aborted------------\n");
			break;
		}

		data_len = data_node_len - DATA_HEADER_SIZE;
		printf("Received %d bytes. Writing to local file\n", data_len);
		write(file_fd, &(data_node.data), data_len);

		// send ack packet
		make_ack_node(&ack_node, &ack_node_len, data_node.number);
		if(SLOW_CLIENT)
			sleep(1);
		error_handle(sendto(sock, &ack_node, ack_node_len, 0, 
					&serv_work_addr, serv_work_addr_size));

		if(data_len < BLOCK_SIZE) {
			printf("---------- finish -----------\n");
			break;
		}
	}
	close(file_fd);
}

void do_put(){
	char local_filename[MAX_FILENAME_LEN], remote_filename[MAX_FILENAME_LEN];

	printf("type local filename: ");
	fgets(local_filename, MAX_FILENAME_LEN, stdin);
	local_filename[strlen(local_filename)-1] = '\0';
	printf("type remove filename: ");
	fgets(remote_filename, MAX_FILENAME_LEN, stdin);
	remote_filename[strlen(remote_filename)-1] = '\0';
	if(strcmp(remote_filename, "") == 0) // the user just type `enter`
		strcpy(remote_filename, local_filename);

	printf("local filename : %s\n", local_filename);
	printf("remote filename : %s\n", remote_filename);

	// check if local file exists
	if(access(local_filename, F_OK) != 0){ // file not exists
		printf("File %s not exists\n", local_filename);
		printf("-----------------aborted------------------\n");
		return;
	}
	// open file
	int file_fd;
	error_handle(file_fd = open(local_filename, O_RDONLY));

	/*
	   1. WRQ            --------> server
	   2. ACK(number 0)  <-------- server
	   3. DATA(number 1) --------> server
	   4. ACK(number 1)  <-------- server
	   5. ............
	   6. DATA(last 0-511 bytes) ------> server
	   7. ACK(number n)  <-------- server
	 */

	// setup tftp nodes and serv_work_addr
	struct req_node req_node;
	int req_node_len;
	int ack_node_len;
	struct ack_node ack_node;
	struct sockaddr serv_work_addr;
	int serv_work_addr_size = sizeof(struct sockaddr);
	struct data_node data_node;
	struct error_node* error_node_ptr;
	int data_node_len, data_len;

	// send req packet
	make_req_node(&req_node, &req_node_len, OPCODE_WRQ, remote_filename, MODE_NETASCII);
	if(SLOW_CLIENT)
		sleep(1);
	error_handle(sendto(sock, (byte*)&req_node, req_node_len, 0,
				(struct sockaddr*)&server_addr, server_addr_size));
	// wait for ack packet with number 0
	error_handle(recvfrom(sock, (byte*)&data_node, sizeof(struct data_node), 0,
				&serv_work_addr, &serv_work_addr_size));
	if(get_opcode((byte*)&data_node) == OPCODE_ERR){ // the server response ERROR
		error_node_ptr = (struct error_node*)&data_node;
		printf("Server responses with ERROR: ");
		printf("%s (error: %d)\n", error_node_ptr->message, 
				ntohs(error_node_ptr->error_number));
		printf("--------------aborted------------\n");
		return;
	}

	if(get_opcode((byte*)&data_node) == OPCODE_ACK && 
			get_number((byte*)&data_node) != 0) {
		printf("Server responses NOT with ACK (block number 0)\n");
		printf("opcode is %d\n", get_opcode((byte*)&data_node));
		printf("ack number is %d\n", get_number((byte*)&data_node));
		printf("-----------------aborted------------------\n");
		close(file_fd);
		return;
	}

	// else // OK. go ahed.
	int number = 1;
	while(1){
		// read data from file and send them
		data_node.opcode = htons(OPCODE_DATA);
		data_node.number = htons(number);
		data_len = read(file_fd, (byte*)&(data_node.data), BLOCK_SIZE);
		printf("read %d bytes from file and sending to server\n", data_len);
		if(SLOW_CLIENT)
			sleep(1);
		error_handle(sendto(sock, (byte*)&data_node, data_len+DATA_HEADER_SIZE, 0,
					&serv_work_addr, serv_work_addr_size));
		// receive ACK packet
		error_handle(recvfrom(sock, (byte*)&ack_node, sizeof(ack_node), 0,
					&serv_work_addr, &serv_work_addr_size));
		// bypass the verification of block number
		number ++;

		if(data_len < BLOCK_SIZE){
			printf("----------------finish----------------\n");
			break;
		}
	}
	close(file_fd);
}

