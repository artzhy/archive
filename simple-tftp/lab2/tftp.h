#include <stdio.h>
#include <string.h>
#include <arpa/inet.h>

#ifndef _TFTP_H
#define _TFTP_H

#define OPCODE_RRQ  1
#define OPCODE_WRQ  2
#define OPCODE_DATA 3
#define OPCODE_ACK  4
#define OPCODE_ERR  5

#define ERR_UNDEFINED           0
#define ERR_FILE_NOT_FOUND      1
#define ERR_ACCESS_DENIED       2
#define ERR_DISK_FULL           3
#define ERR_UNEXPECTED_OPCODE   4
#define ERR_UNKNOWN_TRANSFER_ID 5
#define ERR_FILE_ALREADY_EXISTS 6

#define MODE_NETASCII "netascii"
// #define MODE_OCTET "octet" // this two modes are never used
// #define MODE_MAIL "mail"

#define BLOCK_SIZE        512
#define DATA_HEADER_SIZE  4
#define MAX_FILENAME_LEN  256
#define MAX_MODE_LEN      20
#define MAX_ERROR_MSG_LEN 30

#define SUCCESS 1
#define ERROR   0
typedef char byte;
// #define TFTP_TIMEOUT 2 // not support yet

struct req_node{
	short opcode;
	// data field contain finale and mode 
	// 2 bytes for 2 '\0'
	char data[MAX_FILENAME_LEN + MAX_MODE_LEN + 2]; 
};

struct data_node{
	short opcode;
	short number;
	char data[BLOCK_SIZE];
};

struct ack_node{
	short opcode;
	short number;
};

struct error_node{
	short opcode;
	short error_number;
	char message[MAX_ERROR_MSG_LEN];
};

void lower(char *src, char *desc, int max_num);
short get_opcode(byte *node);
short get_number(byte *node);
int get_filename_and_mode(byte *node, char *filename, char *mode);

// the size of buff must be equal to or larger thatn BLOCK_SIZE
int get_data(byte *node, int data_len, byte *buff);
int make_ack_node(struct ack_node *node, int *len, short number);
int make_data_node(struct data_node *node, int *len, short number, byte *data);
int make_req_node(struct req_node *node, int *len, int opcode, char *filename, char *mode);
int make_error_node(struct error_node *node, int *len, short error_number, char *message);

#endif

