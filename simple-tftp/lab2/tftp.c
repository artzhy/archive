#include "tftp.h"

void lower(char *src, char *desc, int max_num){
	int i;
	for(i = 0; i < max_num; i++){
		if(src[i] >= 'A' && src[i] <= 'Z')
			desc[i] = src[i] + ('a'-'A');
		else
			desc[i] = src[i];
		if(desc[i] == '\0')
			break;
	}
}

short get_opcode(byte *node){
	short opcode;
	memcpy(&opcode, node, sizeof(opcode));
	opcode = ntohs(opcode); // it is very important
	if(opcode == OPCODE_ACK || opcode == OPCODE_DATA || opcode == OPCODE_RRQ
			|| opcode == OPCODE_WRQ || opcode == OPCODE_ERR)
		return opcode;
	else
		return ERROR;
}

short get_number(byte *node){
	short opcode = get_opcode(node);
	if(opcode == OPCODE_ACK)
		return ntohs(((struct ack_node*)node)->number);
	else if(opcode == OPCODE_DATA)
		return ntohs(((struct data_node*)node)->number);
}

int get_filename_and_mode(byte *node, char *filename, char *mode){
	short opcode = get_opcode(node);
	int i = 2;
	if(opcode == OPCODE_RRQ || opcode == OPCODE_WRQ){
		strcpy(filename, node+i);
		i += strlen(filename) + 1;
		strcpy(mode, node+i);
		return SUCCESS;
	} else {
		return ERROR;
	}
}

int get_data(byte *node, int data_len, byte *buff){
	short opcode = get_opcode(node);
	int i = DATA_HEADER_SIZE;
	if(opcode == OPCODE_DATA){
		memcpy(buff, node+i, data_len);
		return SUCCESS;
	} else {
		return ERROR;
	}
}

int make_ack_node(struct ack_node *node, int *len, short number){
	node->opcode = htons(OPCODE_ACK);
	node->number = htons(number);
	*len = DATA_HEADER_SIZE;
	return SUCCESS;
}

int make_data_node(struct data_node *node, int *len, short number, byte *data){
	node->opcode = htons(OPCODE_DATA);
	node->number = htons(number);
	int i = DATA_HEADER_SIZE;
	byte* b_node = (byte*)node;
	int str_len = strlen((char*)data);
	
	if(str_len >= BLOCK_SIZE){
		memcpy(b_node+i, data, BLOCK_SIZE);
		i += BLOCK_SIZE;
		*len = i;
	} else {
		memcpy(b_node+i, data, str_len);
		i += str_len;
		*len = i;
	}
	return SUCCESS;
}

int make_req_node(struct req_node *node, int *len, int opcode, char *filename, char *mode){
	int i = 0;
	byte* b_node = (byte*)node;
	// initialize opcode field
	if(opcode == OPCODE_RRQ || opcode == OPCODE_WRQ){
		node->opcode = htons(opcode);
		i += 2;
	} else {
		return ERROR;
	}

	// initialize filename field
	int str_len = strlen(filename);
	if(str_len > MAX_FILENAME_LEN){
		return ERROR;
	} else {
		memcpy(b_node+i, filename, str_len);
		i += str_len;
		b_node[i++] = (byte)0; // the 1 byte 0 after filename
	}

	// initialize mode field // only support netascii
	char mode_low[MAX_MODE_LEN];
	lower(mode, mode_low, MAX_MODE_LEN);
	if(strcmp(mode_low, MODE_NETASCII) != 0){
		return ERROR;
	} else {
		str_len = strlen(mode_low);
		memcpy(b_node+i, mode_low, str_len);
		i += str_len;
		b_node[i++] = (byte)0; // the byte-0 after mode field
	}
	*len = i;

	return SUCCESS;
}

int make_error_node(struct error_node *node, int *len, short error_number, char *message){
	int i = 0;
	byte* b_node = (byte*)node;
	node->opcode = htons(OPCODE_ERR);
	node->error_number = htons(error_number);
	i += sizeof(node->opcode) + sizeof(node->error_number); // i += 4

	int str_len = strlen(message);
	if(str_len > MAX_ERROR_MSG_LEN){
		return ERROR;
	} else {
		memcpy(b_node+i, message, str_len);
		i += str_len;
		b_node[i++] = (byte)0;
	}
	*len = i;

	return SUCCESS;
}

